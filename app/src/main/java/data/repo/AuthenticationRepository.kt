package data.repo
import data.model.Result
import data.source.remote.AuthenticationDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepository @Inject constructor(
    private val authDataSource : AuthenticationDataSource
) {

    private var _restaurantId : Int? = null
    val restaurantId get() = _restaurantId

    suspend fun checkUserLoggedIn() : Result {
        _restaurantId = authDataSource.checkUserLoggedIn()
        return if(_restaurantId != null) Result.Success(_restaurantId) else Result.Failure("Account not found")
    }

    suspend fun login(account : String, password : String) : Result {
        val chefId = authDataSource.login(account, password)
        return if(chefId != null) Result.Success(chefId) else Result.Failure("Account not found")
    }

    suspend fun logout() : Result {
        val loggedOut = authDataSource.logout()
        return if(loggedOut) Result.Success(true) else Result.Failure("Logout failed")
    }
}