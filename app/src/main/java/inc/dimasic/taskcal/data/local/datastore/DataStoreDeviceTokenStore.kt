package inc.dimasic.taskcal.data.local.datastore

import androidx.datastore.preferences.core.stringPreferencesKey
import inc.dimasic.taskcal.data.remote.DeviceTokenStore
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val DEVICE_TOKEN_KEY = stringPreferencesKey("device_token")

/**
 * [DeviceTokenStore] backed by DataStore Preferences. Two UUIDs joined give a 73 character token,
 * inside the 32 to 256 characters the server accepts.
 */
@Singleton
class DataStoreDeviceTokenStore @Inject constructor(
    private val preferences: PreferencesDataStoreManager,
) : DeviceTokenStore {

    private val mutex = Mutex()

    @Volatile
    private var cached: String? = null

    override suspend fun token(): String =
        cached ?: mutex.withLock { cached ?: loadOrCreate().also { cached = it } }

    private suspend fun loadOrCreate(): String {
        preferences.preferences.first()[DEVICE_TOKEN_KEY]?.let { return it }
        val created = "${UUID.randomUUID()}-${UUID.randomUUID()}"
        preferences.set(DEVICE_TOKEN_KEY, created)
        return created
    }
}