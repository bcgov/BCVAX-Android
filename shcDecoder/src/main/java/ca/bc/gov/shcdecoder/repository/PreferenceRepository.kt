package ca.bc.gov.shcdecoder.repository

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    val timeStamp: Flow<Long>
    suspend fun setTimeStamp(timeStamp: Long): Preferences
}