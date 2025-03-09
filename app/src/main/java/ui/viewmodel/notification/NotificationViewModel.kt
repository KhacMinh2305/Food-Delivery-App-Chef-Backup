package ui.viewmodel.notification
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import data.model.ChatRoom
import data.repo.MessageRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val messageRepo : MessageRepository
) : ViewModel() {

    private val _tabPositionState = MutableLiveData<Int>(0)
    val tabPositionState : MutableLiveData<Int> = _tabPositionState

    val chattingRoomsState : Flow<PagingData<ChatRoom>> = messageRepo.getChattingRoomPager().flow.cachedIn(viewModelScope)

    private val _messageState = Channel<String>()
    private val messageState : Flow<String> = _messageState.receiveAsFlow()

    fun changeTabPosition(position : Int) {
        _tabPositionState.value = position
    }

}