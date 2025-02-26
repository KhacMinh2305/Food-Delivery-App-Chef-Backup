package ui.viewmodel.authentication
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.model.Result
import data.repo.AuthenticationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authRepository : AuthenticationRepository
) : ViewModel() {

    init {
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = authRepository.checkUserLoggedIn()) {
                is Result.Success -> {
                    val chefId = result.data as Int?
                    if(chefId != null) {
                        Log.d("TAG", "Cho vao man home")
                        return@launch
                    }
                    Log.d("TAG", "Khong co du lieu , bat dang nhap")
                }
                else -> {
                    Log.d("TAG", "Khong co data")
                }
            }
        }
    }

    fun login(account : String, password : String) {
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = authRepository.login(account, password)) {
                is Result.Success -> {
                    val chefId = result.data as Int
                    Log.d("TAG", "Login thanh cong, chef Id : $chefId")
                }
                else -> {
                    Log.d("TAG", "Login that bai, Tai khoan khong ton tai")
                }
            }
        }
    }

}