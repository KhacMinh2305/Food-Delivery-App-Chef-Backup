package data.repo
import data.source.remote.MessageDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDataSource : MessageDataSource
) {

    fun getChattingRoomPager() = messageDataSource.getChattingRoomPager()

    fun startObserveMessages() {
        messageDataSource.startObserveMessages()
    }

    fun startObserveRoomMessages(roomId : String) {
        messageDataSource.startObserveRoomMessages(roomId)
    }

    suspend fun updateOnOffline() = messageDataSource.updateOnOffline()

    fun getMessagePager(roomId: String) = messageDataSource.getMessagePager(roomId)

    fun sendMessage(content : String, roomId : String) = messageDataSource.sendMessage(content, roomId)
}