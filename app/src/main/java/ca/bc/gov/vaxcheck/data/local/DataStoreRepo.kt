package ca.bc.gov.vaxcheck.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import ca.bc.gov.vaxcheck.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

const val PREFERENCE_NAME = BuildConfig.APPLICATION_ID + "_preferences"

/**
 * [DataStoreRepo]
 *
 * @author amit metri
 */
class DataStoreRepo @Inject constructor(private val dataStore: DataStore<Preferences>) {

    val isOnBoardingShown: Flow<Boolean> = dataStore.data.map { preference ->
        preference[ON_BOARDING_SHOWN] ?: false
    }

    suspend fun writeFirstLaunch() = dataStore.edit { preference ->
        preference[ON_BOARDING_SHOWN] = true
    }

    //Constants used for preferences needs to be added here
    companion object {
        val ON_BOARDING_SHOWN = booleanPreferencesKey("ON_BOARDING_SHOWN")
    }
}
