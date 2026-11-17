package inc.dimasic.taskcal.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
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

    /** Every stored preference, re-emitted whenever any of them changes. */
    val preferences: Flow<Preferences> = context.preferencesDataStore.data

    fun <T> observe(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        preferences.map { it[key] ?: defaultValue }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        edit { it[key] = value }
    }

    suspend fun <T> remove(key: Preferences.Key<T>) {
        edit { it.remove(key) }
    }

    /** Applies several changes as one write, so observers see them land together. */
    suspend fun edit(transform: (MutablePreferences) -> Unit) {
        context.preferencesDataStore.edit(transform)
    }
}
