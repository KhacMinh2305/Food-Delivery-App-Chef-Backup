package ui.viewmodel.notification
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import data.model.Message
import data.repo.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ChattingViewModel @Inject constructor(
    private val messageRepo : MessageRepository,

) : ViewModel() {

    lateinit var messageFlow : Flow<PagingData<Message>>

    fun initialize(roomId : String) {
        messageRepo.startObserveRoomMessages(roomId)
        messageFlow = messageRepo.getMessagePager(roomId).flow.cachedIn(viewModelScope)
    }

    fun sendMessage(roomId: String, message: String) {
        messageRepo.sendMessage(message, roomId).addOnSuccessListener {
            Log.d("TAG", "Gui tin nhan thanh cong !")
        }.addOnFailureListener { e ->
            Log.d("TAG", e.toString())
        }
    }
}