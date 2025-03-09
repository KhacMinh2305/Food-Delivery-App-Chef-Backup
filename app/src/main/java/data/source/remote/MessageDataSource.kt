package data.source.remote
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import data.model.ChatRoom
import data.model.ChatRoomMember
import data.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDataSource @Inject constructor(
    private val authDataSource: AuthenticationDataSource
) {

    enum class AttendingMode {
        NOT_IN_MESSAGE, IN_LIST_ROOM, IN_ROOM
    }

    private val firebaseDb = Firebase.firestore

    private val chatRooms = mutableListOf<ChatRoom>()

    private val coroutineScope = CoroutineScope(Dispatchers.Default + Job())

    private val observingTime = System.currentTimeMillis()

    private var attendingRoomId = ""
    private var attendingMode = AttendingMode.IN_LIST_ROOM

    private fun convertRawDataIntoRooms(id : String, rawData : Map<String, Any>) : ChatRoom {
        val rawChef = (rawData["members"] as Map<String, Any>)["chef"] as Map<String, Any>
        val rawUser = (rawData["members"] as Map<String, Any>)["user"] as Map<String, Any>
        val chef = ChatRoomMember((rawChef["id"] as Long).toInt(), rawChef["name"] as String, rawChef["image"] as String,
            rawChef["state"] as Boolean, rawChef["latest_active"] as Long, (rawChef["un_seen_amount"] as Long).toInt())
        val user = ChatRoomMember((rawUser["id"] as Long).toInt(), rawUser["name"] as String, rawUser["image"] as String,
            rawUser["state"] as Boolean, rawUser["latest_active"] as Long, (rawUser["un_seen_amount"] as Long).toInt())
        val latestMessageMap = rawData["latest_message"] as Map<String, Any>
        val room = ChatRoom(id, chef, user, latestMessageMap["content"] as String, latestMessageMap["time"] as Long,
            user.id, (latestMessageMap["role"] as Long).toInt())
        return room
    }

    private fun onNewRoomAdded(rawData : QueryDocumentSnapshot) {
        if((rawData.data["start_at"] as Long) > observingTime) {
            val chatRoom = convertRawDataIntoRooms(rawData.id, rawData.data)
            chatRooms.add(chatRoom)
        }
    }

    private fun onRoomsChanged(rawData : QueryDocumentSnapshot) {
        if(rawData.id == attendingRoomId) return
        val rawChef = (rawData["members"] as Map<String, Any>)["chef"] as Map<String, Any>
        val unSeenAmount = (rawChef["un_seen_amount"] as Long).toInt()
        val unSeenMessage = (rawData["latest_message"] as Map<String, Any>)["content"] as String
        val time = (rawData["latest_message"] as Map<String, Any>)["time"] as Long
        val rawUser = (rawData["members"] as Map<String, Any>)["user"] as Map<String, Any>
        val userOnline = rawUser["state"] as Boolean
        chatRooms.find { it.roomId == rawData.id }?.apply {
            senderId = user.id
            role = 0
            latestMessage = unSeenMessage
            latestMessageTime = time
            chef.unSeenAmount = unSeenAmount
            user.isOnline = userOnline
        }
    }

    private var startObserving = false
    fun startObserveMessages() {
        firebaseDb.collection("chat")
            .whereEqualTo("members.chef.id", authDataSource.chefId)
            .addSnapshotListener { snapshots, error ->
                if(!startObserving) {
                    startObserving = true
                    return@addSnapshotListener
                }
                if(error!= null) {
                    Log.e("MessageDataSource_startObserveMessages", error.toString())
                    return@addSnapshotListener
                }
                coroutineScope.launch {
                    for(snapshot in snapshots!!.documentChanges) {
                        when(snapshot.type) {
                            DocumentChange.Type.ADDED -> {
                                onNewRoomAdded(snapshot.document)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                onRoomsChanged(snapshot.document)
                            }
                            DocumentChange.Type.REMOVED -> {}
                        }
                    }
                    if(attendingMode == AttendingMode.NOT_IN_MESSAGE) {
                        // TODO : Emit notification
                        return@launch
                    }
                    chatRooms.sortByDescending { it.latestMessageTime }
                    chatRoomSource?.invalidate()
                }
            }
    }

    private var chatRoomSource : ChattingRoomPagingSource? = null
    fun getChattingRoomPager() : Pager<Int, ChatRoom> {
        return Pager(
            config = PagingConfig(10, 5, true, 20)
        ) {
            ChattingRoomPagingSource(firebaseDb.collection("chat")
                .whereEqualTo("members.chef.id", authDataSource.chefId!!), chatRooms).apply {
                chatRoomSource = this
            }
        }
    }

    // TODO : For update active state, use WorkManager.
    fun updateOnOffline() {
        coroutineScope.launch(Dispatchers.IO) {
            firebaseDb.collection("chat").whereEqualTo("members.chef.id", authDataSource.chefId).get().await()
            firebaseDb.runBatch {
                val leavingTime = System.currentTimeMillis()
                chatRooms.forEach { room ->
                    firebaseDb.collection("chat").document(room.roomId).update(
                        mapOf("members.chef.latest_active" to leavingTime,
                            "members.chef.state" to false)
                    )
                }
            }.await()
        }
    }

    private var messageSource : MessagePagingSource? = null
    fun getMessagePager(roomId: String) : Pager<Long, Message> {
        val messageQuery = firebaseDb.collection("chat").document(roomId).collection("messages").orderBy("time", Query.Direction.DESCENDING)
        val keyQuery = firebaseDb.collection("chat").document(roomId).get()
        return Pager(
            config = PagingConfig(20, 8, true, 30)
        ) {
            MessagePagingSource(messageQuery, keyQuery).apply {
                messageSource = this
            }
        }
    }

    fun startObserveRoomMessages(roomId : String) {
        var startObserveRoom = false
        firebaseDb.collection("chat").document(roomId).collection("messages").addSnapshotListener { snapshots, error ->
            if(!startObserveRoom) {
                startObserveRoom = true
                return@addSnapshotListener
            }
            if(error != null) {
                Log.e("MessageDataSource_startObserveRoomMessages", error.toString())
                return@addSnapshotListener
            }
            messageSource?.invalidateData()
        }
    }

    fun sendMessage(content : String, roomId : String) : Task<Void> {
        val time = System.currentTimeMillis()
        val messageMap = mapOf(
            "content" to content,
            "seen" to false,
            "sender" to mapOf(
                "id" to authDataSource.chefId!!,
                "name" to "Pizza Hub",
                "role" to 1
            ),
            "time" to time
        )
        return firebaseDb.runBatch {
            firebaseDb.collection("chat").document(roomId).collection("messages")
                .document("${Long.MAX_VALUE - time}").set(messageMap)
            firebaseDb.collection("chat").document(roomId).update(
                mapOf("latest_message" to mapOf(
                    "content" to content,
                    "id" to authDataSource.chefId!!,
                    "name" to "Pizza Hub",
                    "role" to 1,
                    "time" to time
                ))
            )
        }
    }
}