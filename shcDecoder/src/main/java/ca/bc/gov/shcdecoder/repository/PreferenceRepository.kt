package ca.bc.gov.shcdecoder.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [PreferenceRepository]
 *
 * @author Pinakin Kansara
 */
private val Context.dataStore by preferencesDataStore("shc_decoder")

class PreferenceRepository(
    private val context: Context
) {

    companion object {
        val CACHED_TIME_STAMP = longPreferencesKey("CACHED_TIME_STAMP")
    }

    val timeStamp: Flow<Long> = context.dataStore.data.map { preference ->
        preference[CACHED_TIME_STAMP] ?: 0
    }

    suspend fun setTimeStamp(timeStamp: Long) = context.dataStore.edit { preference ->
        preference[CACHED_TIME_STAMP] = timeStamp
    }
}
