package data.source.remote
import data.storage.other.KeyStore
import test.data.SampleData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationDataSource @Inject constructor(
    private val keyStore : KeyStore
) {

    private var _chefId : Int? = null
    val chefId get() = _chefId

    suspend fun checkUserLoggedIn() : Int? {
        val id = keyStore.getChefId()
        _chefId = id
        return _chefId
    }

    suspend fun login(account : String, password : String) : Int? {
        val foundAccount = SampleData.accounts.find { it.account == account && it.password  == password}
        foundAccount?.let {
            keyStore.updateAccount(it.account)
            keyStore.updatePassword(it.password)
            keyStore.updateChefId(it.restaurantId)
            _chefId = it.restaurantId
            return _chefId
        }
        return null
    }

    suspend fun logout() : Boolean {
        val loggedOut = keyStore.updateAccount("") && keyStore.updatePassword("") && keyStore.updateChefId(-1)
        return loggedOut
    }
}