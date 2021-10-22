package ca.bc.gov.shcdecoder

import android.content.Context
import ca.bc.gov.shcdecoder.model.Jwks
import ca.bc.gov.shcdecoder.model.Rule
import ca.bc.gov.shcdecoder.model.TrustedIssuersResponse
import ca.bc.gov.shcdecoder.model.ValidationRuleResponse
import ca.bc.gov.shcdecoder.repository.PreferenceRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*

class FileUtils(
    private val context: Context
) {

    private val preferenceRepository = PreferenceRepository(context)

    companion object {
        const val SUFFIX_JWKS_JSON = "/.well-known/jwks.json"
        const val SUFFIX_ISSUER_JSON = "issuers.json"
    }

    init {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            try {
                downloadAndCacheFiles()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun downloadAndCacheFiles() {

        if (isIntervalPassed()) {
            deleteCachedFiles()
        }


        //ISSUER END POINT
        val url = context.getString(R.string.issuer_url)

        val file = getFile(url)

        if (!file.exists()) {
            downloadFile(url)
        }

        val trustedIssuersResponse = getTrustedIssuers(file)

        //FETCH AND STORE KEYS FOR ALL ISSUERS
        trustedIssuersResponse.trustedIssuers.forEach { issuer ->
            val keyUrl = if (issuer.iss.endsWith(SUFFIX_JWKS_JSON)) {
                issuer.iss
            } else {
                "${issuer.iss}$SUFFIX_JWKS_JSON"
            }
            val issuerKeyFile = getFile(keyUrl)
            if (!issuerKeyFile.exists()) {
                downloadFile(keyUrl)
            }
        }

        val rulesUrl = context.getString(R.string.rules_url)

        val rulesFile = getFile(rulesUrl)

        if (!rulesFile.exists()) {
            downloadFile(rulesUrl)
        }


    }

    suspend fun downloadFile(url: String) {
        val destFile = File(getDownloadDir(), determineFileName(url))
        if (destFile.exists()) {
            destFile.delete()
        }
        URL(url).openStream().use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        preferenceRepository.setCachedTimeStamp(Calendar.getInstance().timeInMillis)
    }

    fun getFile(url: String): File {
        return File(getDownloadDir(), determineFileName(url))
    }


    fun getKeyForIssuer(iss: String): Jwks? {

        val url = context.getString(R.string.issuer_url)

        val file = getFile(url)

        if (!file.exists()) {
            return null
        }

        val issuer = getTrustedIssuers(file).trustedIssuers.singleOrNull { issuer ->
            iss == issuer.iss
        }

        if (issuer != null) {
            return getKeyForUrl(issuer.iss)
        }
        return null
    }

    fun getRuleSetForIssuer(iss: String): Rule? {

        val url = context.getString(R.string.rules_url)

        val file = getFile(url)

        if (!file.exists()) {
            return null
        }
        val rulesResponse = getRulesForIssuer(file)

        var ruleSet: Rule? = null

        rulesResponse.ruleSet.forEach { rule ->

            if (rule.ruleTarget.endsWith(SUFFIX_ISSUER_JSON)) {
                val issuerFile = getFile(rule.ruleTarget)
                if (!issuerFile.exists()) {
                    return@forEach
                }
                val issuer = getTrustedIssuers(issuerFile).trustedIssuers.singleOrNull { issuer ->
                    iss == issuer.iss
                }
                if (issuer != null) {
                    ruleSet = rule
                    return@forEach
                } else {
                    return@forEach
                }
            }

            if (rule.ruleTarget.startsWith(iss)) {
                ruleSet = rule
                return@forEach
            }
        }

        return ruleSet
    }

    private fun getRulesForIssuer(file: File): ValidationRuleResponse {
        val bufferedReader = file.bufferedReader()
        val jsonString = bufferedReader.use { it.readText() }
        return Gson().fromJson(jsonString, ValidationRuleResponse::class.java)
    }

    private fun getKeyForUrl(iss: String): Jwks {
        val url: String = if (iss.endsWith(SUFFIX_JWKS_JSON)) {
            iss
        } else {
            "${iss}$SUFFIX_JWKS_JSON"
        }

        val bufferedReader = getFile(url).bufferedReader()
        val jsonString = bufferedReader.use { it.readText() }
        return Gson().fromJson(jsonString, Jwks::class.java)
    }

    private fun getTrustedIssuers(file: File): TrustedIssuersResponse {
        val bufferedReader = file.bufferedReader()
        val jsonString = bufferedReader.use { it.readText() }
        return Gson().fromJson(jsonString, TrustedIssuersResponse::class.java)
    }

    private fun determineFileName(url: String): String {
        val fileName = url.removePrefix("https://")
        return fileName.replace("/", "~")
    }

    private fun getDownloadDir(): File {
        val dir = File(context.filesDir, "Decoder")
        if (!dir.exists()) {
            dir.mkdir()
        }
        return dir
    }

    private fun deleteCachedFiles() {
        val dir = File(context.filesDir, "Decoder")
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }

    private suspend fun isIntervalPassed(): Boolean {

        val currentTime = Calendar.getInstance()
        val timeInMillis = preferenceRepository.cachedTimeStamp.first()
        val previousTime = Calendar.getInstance()
        previousTime.timeInMillis = timeInMillis
        previousTime.add(Calendar.HOUR_OF_DAY, 24)
        return (currentTime >= previousTime)
    }
}