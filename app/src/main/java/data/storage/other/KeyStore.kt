package data.storage.other
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import system.constant.Constant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyStore @Inject constructor(
    @ApplicationContext private val context : Context
) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constant.SECRET_KEY_FILE_NAME)

    private val accountKey = stringPreferencesKey(Constant.ACCOUNT_KEY)
    private val passwordKey = stringPreferencesKey(Constant.PASSWORD_KEY)
    private val chefIdKey = intPreferencesKey(Constant.CHEF_ID)

    suspend fun getAccount() : String? = getStringValue(accountKey)
    suspend fun getPassword() : String? = getStringValue(passwordKey)
    suspend fun getChefId() : Int? = getIntValue(chefIdKey)

    suspend fun updateAccount(account : String) : Boolean = updateStringKey(accountKey, account)
    suspend fun updatePassword(password : String) : Boolean = updateStringKey(passwordKey, password)
    suspend fun updateChefId(chefId : Int) : Boolean = updateIntKey(chefIdKey, chefId)

    private suspend fun getStringValue(stringKey : Preferences. Key<String>) : String? {
        try {
            val value = context.dataStore.data.firstOrNull()?.get(stringKey)
            return value
        } catch (e : Exception) {
            Log.e("KeyStore_getStringValue", e.toString())
            return null
        }
    }

    private suspend fun getIntValue(stringKey : Preferences. Key<Int>) : Int? {
        try {
            val value = context.dataStore.data.firstOrNull()?.get(stringKey)
            return value
        } catch (e : Exception) {
            Log.e("KeyStore_getIntValue", e.toString())
            return null
        }
    }

    private suspend fun updateStringKey(stringKey : Preferences. Key<String>, value : String) : Boolean {
        try {
            context.dataStore.edit {
                it[stringKey] = value
            }
            return true
        } catch (e : Exception) {
            Log.e("KeyStore_updateStringKey", e.toString())
            return false
        }
    }

    private suspend fun updateIntKey(stringKey : Preferences. Key<Int>, value : Int) : Boolean {
        try {
            context.dataStore.edit {
                it[stringKey] = value
            }
            return true
        } catch (e : Exception) {
            Log.e("KeyStore_updateStringKey", e.toString())
            return false
        }
    }
}