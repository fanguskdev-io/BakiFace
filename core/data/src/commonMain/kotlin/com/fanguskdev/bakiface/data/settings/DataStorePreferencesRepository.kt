package com.fanguskdev.bakiface.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.settings.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okio.IOException
import okio.Path.Companion.toPath

/** The name of the preferences file, in the app's private files (ADR 0007). DataStore needs this extension. */
const val PREFERENCES_FILE_NAME = "bakiface.preferences_pb"

/** The preferences [DataStore] at [path]. One per file and per process: the DI container keeps it single. */
fun createPreferencesDataStore(path: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { path.toPath() })

/**
 * The preferences in a DataStore file on the phone (ADR 0007). A file that cannot be read counts as empty preferences:
 * the app starts with the face as it is, instead of failing.
 */
class DataStorePreferencesRepository(private val dataStore: DataStore<Preferences>) : PreferencesRepository {
    override val selectedEffectId: Flow<EffectId?> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences -> preferences[SELECTED_EFFECT]?.let(::EffectId) }
        .distinctUntilChanged()

    override suspend fun setSelectedEffectId(id: EffectId?) {
        dataStore.edit { preferences ->
            if (id == null) preferences.remove(SELECTED_EFFECT) else preferences[SELECTED_EFFECT] = id.value
        }
    }

    private companion object {
        val SELECTED_EFFECT = stringPreferencesKey("selected_effect")
    }
}
