package ca.bc.gov.vaxcheck.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import ca.bc.gov.vaxcheck.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val PREFERENCE_NAME = BuildConfig.APPLICATION_ID + "_preferences"

class DataStoreRepo(var context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences>
                by preferencesDataStore(name = PREFERENCE_NAME)
    }

    suspend fun saveToDataStore(key: String, value: Boolean) {
        val dataStoreKey = booleanPreferencesKey(key)
        context.dataStore.edit { settings ->
            settings[dataStoreKey] = value
        }
    }

    fun readFromDataStore(key: String): Flow<Boolean> {
        val dataStoreKey = booleanPreferencesKey(key)
        val boolPreferences: Flow<Boolean> = context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[dataStoreKey] ?: false
            }
        return boolPreferences
    }
}