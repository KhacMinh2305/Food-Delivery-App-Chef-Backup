package data.source.remote
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import data.model.ChatRoom
import data.model.ChatRoomMember
import kotlinx.coroutines.tasks.await

class ChattingRoomPagingSource(private val query : Query, private val cachingSource : MutableList<ChatRoom>) : PagingSource<Int, ChatRoom>() {

    override fun getRefreshKey(state: PagingState<Int, ChatRoom>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1) ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

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

    private fun copyData(left : Int, right : Int) : List<ChatRoom> {
        val data = mutableListOf<ChatRoom>()
        for(i in left until right) {
            val item = cachingSource[i]
            val newChef = ChatRoomMember(item.chef.id, item.chef.name, item.chef.image, item.chef.isOnline, item.chef.latestActive, item.chef.unSeenAmount)
            val newUser = ChatRoomMember(item.user.id, item.user.name, item.user.image, item.user.isOnline, item.user.latestActive, item.user.unSeenAmount)
            val newChatRoom = ChatRoom(item.roomId, newChef, newUser, item.latestMessage, item.latestMessageTime, item.senderId, item.role)
            data.add(newChatRoom)
        }
        return data.toList()
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatRoom> {
        val loadKey = params.key?: 1
        if(cachingSource.isEmpty()) {
            val rooms = query.get().await().map { doc ->
                convertRawDataIntoRooms(doc.id, doc.data)
            }
            cachingSource.addAll(rooms)
            cachingSource.sortByDescending { it.latestMessageTime }
        }
        val totalPage = cachingSource.size / 10 + (if(cachingSource.size % 10 > 0) 1 else 0)
        val left = 10 * (loadKey - 1)
        val right = (left + 10).coerceAtMost(cachingSource.size)
        val data = copyData(left, right)
        val nextKey = if(loadKey < totalPage) loadKey + 1 else null
        return LoadResult.Page(data, null, nextKey, LoadResult.Page.COUNT_UNDEFINED, LoadResult.Page.COUNT_UNDEFINED)
    }
}