package ui.viewmodel
import android.util.Log
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
class AppViewModel @Inject constructor(
    private val authRepository : AuthenticationRepository
) : ViewModel() {

    private val _navigationState = Channel<Boolean>()
    val navigationState : Flow<Boolean> = _navigationState.receiveAsFlow()

    fun checkUserLoggedIn() {
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = authRepository.checkUserLoggedIn()) {
                is Result.Success -> {
                    val chefId = result.data as Int?
                    if(chefId != null) {
                        _navigationState.send(false)
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

}