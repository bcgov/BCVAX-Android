package ca.bc.gov.shcdecoder.cache.impl

import ca.bc.gov.shcdecoder.SHCConfig
import ca.bc.gov.shcdecoder.cache.CacheManager
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.model.Issuer
import ca.bc.gov.shcdecoder.model.JwksKey
import ca.bc.gov.shcdecoder.repository.PreferenceRepository
import ca.bc.gov.shcdecoder.revocations.getRevocationsUrl
import kotlinx.coroutines.flow.first
import java.util.Calendar

internal class CacheManagerImpl(
    private val shcConfig: SHCConfig,
    private val preferenceRepository: PreferenceRepository,
    private val fileManager: FileManager
) : CacheManager {

    companion object {
        const val SUFFIX_JWKS_JSON = "/.well-known/jwks.json"
        const val SUFFIX_ISSUER_JSON = "issuers.json"
    }

    override suspend fun fetch() {
        if (isCacheExpired()) {
            fileManager.downloadFile(shcConfig.rulesEndPoint)

            val issuers = fetchIssuers()

            issuers.forEach { issuer ->
                val keys = fetchKeys(issuer)
                fetchRevocations(issuer, keys)
            }

            preferenceRepository.setTimeStamp(Calendar.getInstance().timeInMillis)
        }
    }

    private suspend fun fetchIssuers(): List<Issuer> {
        fileManager.downloadFile(shcConfig.issuerEndPoint)
        return fileManager.getIssuers(shcConfig.issuerEndPoint)
    }

    private suspend fun fetchKeys(issuer: Issuer): List<JwksKey> {
        val keyUrl = if (issuer.iss.endsWith(SUFFIX_JWKS_JSON)) {
            issuer.iss
        } else {
            "${issuer.iss}$SUFFIX_JWKS_JSON"
        }

        fileManager.downloadFile(keyUrl)
        return fileManager.getKeys(keyUrl)
    }

    private suspend fun fetchRevocations(issuer: Issuer, keys: List<JwksKey>) {
        keys.forEach { key ->
            val revocationsURL = getRevocationsUrl(issuer.iss, key.kid)

            if (fileManager.exists(revocationsURL)) {
                val ctr = fileManager.getRevocationsCtr(revocationsURL)
                if (ctr != key.ctr) {
                    fileManager.downloadFile(revocationsURL)
                }
            } else {
                fileManager.downloadFile(revocationsURL)
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
