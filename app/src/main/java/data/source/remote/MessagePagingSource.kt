package data.source.remote
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import data.model.Message
import kotlinx.coroutines.tasks.await

class MessagePagingSource(private val query : Query, private val keyQuery : Task<DocumentSnapshot>) : PagingSource<Long, Message>() {

    companion object {
        private var userImage : String? = null
        private var chefImage : String? = null
    }

    override fun getRefreshKey(state: PagingState<Long, Message>): Long? {
        return state.anchorPosition?.let {
            state.closestItemToPosition(it)?.sendingTime
        }
    }

    private fun convertRawDataIntoMessages(querySnapshot : QuerySnapshot) : List<Message> {
        val messages = querySnapshot.documents.map { document ->
            val data = document.data as Map<String, Any>
            val sender = data["sender"] as Map<String, Any>
            val senderRole = (sender["role"] as Long).toInt()
            val senderImage = if(senderRole == 0) userImage else chefImage
            Message((sender["id"] as Long).toInt(), sender["name"] as String, senderImage ?: "", senderRole,
                data["content"] as String, data["time"] as Long, data["seen"] as Boolean)
        }
        return messages
    }

    private var mostRecentLoadTime = 0L
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Message> {
        var loadTime = params.key
        if(loadTime == null) {
            val room = keyQuery.await().data as Map<String, Any>
            loadTime = (room["latest_message"] as Map<String, Any>)["time"] as Long
            chefImage = ((room["members"] as Map<String, Any>)["chef"] as Map<String, Any>)["image"] as String
            userImage = ((room["members"] as Map<String, Any>)["user"] as Map<String, Any>)["image"] as String
        }
        try {
            val rawMessage = if(mostRecentLoadTime == 0L) {
                query.limit(20).get().await()
            } else {
                if(loadTime < mostRecentLoadTime) {
                    query.startAfter(loadTime).limit(20).get().await() // load older messages
                } else {
                    query.endBefore(loadTime).limit(20).get().await() // load later messages
                }
            }
            mostRecentLoadTime = loadTime
            val data = convertRawDataIntoMessages(rawMessage!!).sortedBy { it.sendingTime }
            val prevKey = if(data.size < 20) null else data.first().sendingTime
            val nextKey = if(data.size < 20) null else data.last().sendingTime
            return LoadResult.Page(data, prevKey, nextKey, LoadResult.Page.COUNT_UNDEFINED, LoadResult.Page.COUNT_UNDEFINED)
        } catch (e : Exception) {
            return LoadResult.Page(emptyList(), null, null, LoadResult.Page.COUNT_UNDEFINED, LoadResult.Page.COUNT_UNDEFINED)
        }
    }

    fun invalidateData() {
        mostRecentLoadTime = 0L
        invalidate()
    }
}