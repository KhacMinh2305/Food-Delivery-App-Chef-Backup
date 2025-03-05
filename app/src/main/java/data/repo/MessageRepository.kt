package data.repo
import data.source.remote.MessageDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDataSource : MessageDataSource
) {

    fun getChattingRoomPager() = messageDataSource.getChattingRoomPager()

    fun getRoomEmitter() = messageDataSource.getRoomEmitter()

    fun startObserveMessages() {
        messageDataSource.startObserveMessages()
    }
}