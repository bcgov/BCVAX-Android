package ca.bc.gov.shcdecoder.cache.impl

import android.util.Log
import ca.bc.gov.shcdecoder.SHCConfig
import ca.bc.gov.shcdecoder.cache.CacheManager
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.repository.PreferenceRepository
import kotlinx.coroutines.flow.first
import java.util.*

internal class CacheManagerImpl(
    private val shcConfig: SHCConfig,
    private val preferenceRepository: PreferenceRepository,
    private val fileManager: FileManager
) : CacheManager {

    companion object {
        const val SUFFIX_JWKS_JSON = "/.well-known/jwks.json"
        const val SUFFIX_ISSUER_JSON = "issuers.json"
        private const val TAG = "CacheManagerImpl"
    }

    override suspend fun fetch() {
        if (isCacheExpired()) {
            try {
                fileManager.downloadFile(shcConfig.issuerEndPoint)
                val issuers = fileManager.getIssuers(shcConfig.issuerEndPoint)
                issuers.forEach { issuer ->
                    val keyUrl = if (issuer.iss.endsWith(SUFFIX_JWKS_JSON)) {
                        issuer.iss
                    } else {
                        "${issuer.iss}${SUFFIX_JWKS_JSON}"
                    }
                    fileManager.downloadFile(keyUrl)
                }

                fileManager.downloadFile(shcConfig.rulesEndPoint)
                preferenceRepository.setTimeStamp(Calendar.getInstance().timeInMillis)
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
    }

    private suspend fun isCacheExpired(): Boolean {
        val currentTime = Calendar.getInstance()
        val timeInMillis = preferenceRepository.timeStamp.first()
        val previousTime = Calendar.getInstance()
        previousTime.timeInMillis = timeInMillis + shcConfig.cacheExpiryTimeInMilli
        return (currentTime >= previousTime)
    }
}