package ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.model.Result
import data.repo.AuthenticationRepository
import data.repo.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository : AuthenticationRepository,
    private val messageRepository : MessageRepository
) : ViewModel() {

    private val _navigationState = Channel<Boolean>()
    val navigationState : Flow<Boolean> = _navigationState.receiveAsFlow()

    private val _bottomNavBarVisibilityState = Channel<Boolean>()
    val bottomNavBarVisibilityState : Flow<Boolean> = _bottomNavBarVisibilityState.receiveAsFlow()

    fun checkUserLoggedIn() {
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = authRepository.checkUserLoggedIn()) {
                is Result.Success -> {
                    val chefId = result.data as Int?
                    if(chefId != null) {
                        _navigationState.send(false)
                        messageRepository.startObserveMessages()
                        return@launch
                    }
                    _navigationState.send(true)
                }
                else -> {
                    _navigationState.send(true)
                }
            }
        }
    }

    fun startObserveMessage() {
        messageRepository.startObserveMessages()
    }

    fun changeBottomNavBarVisibility(mode : Boolean) {
        viewModelScope.launch {
            _bottomNavBarVisibilityState.send(mode)
        }
    }
}