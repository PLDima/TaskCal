package inc.dimasic.taskcal.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_DATASTORE_NAME = "taskcal_preferences"

private val Context.preferencesDataStore: DataStore<Preferences> by
    preferencesDataStore(name = PREFERENCES_DATASTORE_NAME)

/**
 * Thin wrapper around Jetpack [DataStore] Preferences giving repositories a single place to
 * read and write key/value app preferences, without each repository holding its own DataStore.
 */
@Singleton
class PreferencesDataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun <T> observe(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        context.preferencesDataStore.data.map { it[key] ?: defaultValue }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.preferencesDataStore.edit { it[key] = value }
    }

    suspend fun <T> remove(key: Preferences.Key<T>) {
        context.preferencesDataStore.edit { it.remove(key) }
    }
}
