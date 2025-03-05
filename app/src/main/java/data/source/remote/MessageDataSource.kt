package data.source.remote
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import data.model.ChatRoom
import data.model.ChatRoomMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDataSource @Inject constructor(
    private val authDataSource: AuthenticationDataSource
) {

    private val firebaseDb = Firebase.firestore

    private val chatRooms = mutableListOf<ChatRoom>()

    private val coroutineScope = CoroutineScope(Dispatchers.Default + Job())

    private val roomEmitter = Channel<Unit>()

    fun getRoomEmitter() = roomEmitter.receiveAsFlow()

    private val observingTime = System.currentTimeMillis()



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
        val rawChef = (rawData["members"] as Map<String, Any>)["chef"] as Map<String, Any>
        val unSeenAmount = (rawChef["un_seen_amount"] as Long).toInt()
        val unSeenMessage = (rawData["latest_message"] as Map<String, Any>)["content"] as String
        val time = (rawData["latest_message"] as Map<String, Any>)["time"] as Long
        val changedRoom = chatRooms.find { it.roomId == rawData.id }
        changedRoom?.apply {
            senderId = user.id
            role = 0
            latestMessage = unSeenMessage
            latestMessageTime = time
            chef.unSeenAmount = unSeenAmount
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
                    roomEmitter.send(Unit)
                }
            }
    }

    private var roomSource : ChattingRoomPagingSource? = null
    fun getChattingRoomPager() : Pager<Long, ChatRoom> {
        return Pager(
            config = PagingConfig(10, 5, true, 20)
        ) {
            ChattingRoomPagingSource(firebaseDb.collection("chat")
                .whereEqualTo("members.chef.id", authDataSource.chefId!!), chatRooms).apply {
                roomSource = this
            }
        }
    }
}


/*Cau truc thu muc :

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> CHATTING FEATURE BRAIN_STORMING <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< :
1. Cap nhat o day xuat phat tu backend -> frontend
Bien phap : Ta nhan thay khi cap nhat o 1 noi , thi cac noi con lai se khong duoc cap nhat
=> Ta co the dat 1 bien flag (nhu 1 cong tac), Khi vao man hinh nao thi doi gia tri bien nay tuong ung de chi cap nhat cho man hinh nay.
Vi du , khong o 2 man nay , thi dat la -1 => chi hien thi thong bao (in-app) khi co nguoi nhan tin. Khi o man hinh dstn thi cap nhat lai
gia tri cua phong chat thay doi (viewmodel-cache) de cap nhat len view, con khi o man hinh nhan tin thi khong lam gi. Truong hop nay ta
se su dung 3 emitter (Channel) de gui gia tri tuong ung cho moi truong hop

2. Man hinh nhan tin co icon da xem hoac chua xem => moi tin nhan can phai co 1 trang thai. Khi nguoi gui gui tin nhan, neu nguoi nhan tin
da xem tin nhan thi luc nay o phia nguoi nhan tin gui request cap nhat gia tri moi tin nhan thong thuong. Luc nay nguoi gui se nhan duoc tin
hieu doan chat duoc gui => Cap nhat trang thai icon. Truong hop nay , chung ta chi can kiem tra neu nhu thay doi thuoc ve doan chat ma nguoi
gui tin dang nhan tin thi moi can emit value cap nhat. Tat ca cac emitter nay deu khai bao trong repo , neu de o viewmodel => memory leaks

3. Co mot ngoai le o day do la khi nguoi gui dang nhan tin voi 1 nguoi , thi nguoi khac co the gui tin nhan cho nguoi gui. Luc nay co the truc
tiep de emitter cua man dstn emit nhung tin nhan khong thuoc ve doan chat ma user dang nhan tin.

*4. Van phai hien thi so luong tin nhan chua doc cua moi doan chat khi vao man hinh dstn tu man hinh chinh => Thiet ke cau truc hop ly sao cho
co the truy xuat du lieu co the lay duoc . Trong truong hop nay thi can luu lai so luong tin nhan ma nguoi nhan chua doc. Trong truong hop nay
ta can xac dinh xem ai moi la chu the cap nhat tin nhan. Ta nhan thay , nguoi nhan se luon nhan duoc tin nhan khi doi tuong nay online , do do
neu ho khong xem => ho se la nguoi phai cap nhat tin nhan , do nguoi gui luc nay khong biet nguoi nhan dang o trang thai nao trong app. Can phai
co 1 gia tri de xac dinh xem khi nao nguoi nhan online, neu nhu nguoi nhan khong online (luc nay nguoi gui da biet) thi nguoi gui se cap nhat gia
tri tin nhan chua doc cho nguoi nhan. Vay nen , ta can phai them 1 truong gia tri "isActive" cho moi member trong doan chat. Luc nay , cung can
phai them so luong tin nhan chua doc cho tung member de cap nhat.

5.Trong truong hop khi nguoi gui cap nhat tin nhan chua doc luc nguoi nhan chua doc , va luc nay nguoi nhan online , thi ta cung phai cam 1 bien
de quan ly trang thai khi nao nguoi nhan online va offline de nguoi gui dua vao do co the xac dinh ban than la nguoi cap nhat hay nguoi nhan se
cap nhat.

6. Tu viec brainstorming => Co rat nhieu flags, states, emitters ma MessageRepo can phai quan ly => Nen tao ra 1 class chua nhung quan ly nay giu cho Repo nhe


V2 :
chat[
    room<id>{
        members{
            user {
                id : Int,
                name : String,
                image : String,
                state : Int // online or offline
                latest_active : Long
                un_seen_amount : Int
            }
            chef {
                id : Int,
                name : String,
                image : String,
                state : Int // online or offline
                latest_active : Long
                un_seen_amount : Int
            }
        }
        latest_message{
            id : Int,
            name : String,
            role : Int (0 is user , 1 is chef)
            content : String,
            time : Long
        }
        messages[
            message<id> { // Long
                sender {
                    id : Int,
                    name : String,
                    role : Int
                }
                content : String,
                time : Long,
                seen : Boolean
            }
        ]
    }
]
 * */