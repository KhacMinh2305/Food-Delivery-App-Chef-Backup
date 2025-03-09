package ui.view.fragment.notification
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import data.model.Message
import data.repo.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ChattingViewModel @Inject constructor(
    private val messageRepo : MessageRepository
) : ViewModel() {

    private lateinit var messageFlow : Flow<PagingData<Message>>

    fun initialize(roomId : String) {
        messageFlow = messageRepo.getMessagePager(roomId)
    }

}