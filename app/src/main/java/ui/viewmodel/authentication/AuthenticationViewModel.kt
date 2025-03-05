package ui.viewmodel.authentication
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.model.Result
import data.repo.AuthenticationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authRepository : AuthenticationRepository
) : ViewModel() {

    private val _navigationState = Channel<Unit>()
    val navigationState : Flow<Unit> = _navigationState.receiveAsFlow()

    private val _messageState = Channel<String>()
    val messageState : Flow<String> = _messageState.receiveAsFlow()

    fun login(account : String, password : String) {
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = authRepository.login(account, password)) {
                is Result.Success -> {
                    val chefId = result.data as Int
                    _navigationState.send(Unit)
                }
                else -> {
                    _messageState.send("Login failed ! Account info is wrong")
                }
            }
        }
    }

}