package ca.bc.gov.shcdecoder.cache.impl

import android.content.Context
import android.util.Log
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.model.Issuer
import ca.bc.gov.shcdecoder.model.Jwks
import ca.bc.gov.shcdecoder.model.JwksKey
import ca.bc.gov.shcdecoder.model.Rule
import ca.bc.gov.shcdecoder.model.TrustedIssuersResponse
import ca.bc.gov.shcdecoder.model.ValidationRuleResponse
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class FileManagerImpl(
    private val context: Context
) : FileManager {

    private val downloadDir: File = File(context.filesDir, "Decoder")

    companion object {
        private const val TAG = "FileManagerImpl"
    }

    init {
        if (!downloadDir.exists()) {
            downloadDir.mkdir()
        }
    }

    override suspend fun downloadFile(url: String) {
        try {
            val fileName = getFileNameFromUrl(url)

            val tempFileName = "temp_$fileName"

            val destFile = File(downloadDir, tempFileName)

            if (destFile.exists()) {
                destFile.delete()
            }

            URL(url).openStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val actualFile = File(downloadDir, fileName)

            destFile.copyTo(actualFile, overwrite = true)

            destFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }

    }

    override suspend fun getIssuers(url: String): List<Issuer> {
        val jsonString = getJsonStringFromFile(url)
        val issuerResponse = Gson().fromJson(jsonString, TrustedIssuersResponse::class.java)
        return issuerResponse.trustedIssuers
    }

    override suspend fun getKeys(url: String): List<JwksKey> {
        val jsonString = getJsonStringFromFile(url)
        val keyResponse = Gson().fromJson(jsonString, Jwks::class.java)
        return keyResponse.keys
    }

    override suspend fun getRule(url: String): List<Rule> {
        val jsonString = getJsonStringFromFile(url)
        val validationRulesResponse =
            Gson().fromJson(jsonString, ValidationRuleResponse::class.java)
        return validationRulesResponse.ruleSet
    }

    private fun getJsonStringFromFile(url: String): String {
        val fileName = getFileNameFromUrl(url)
        val file = File(downloadDir, fileName)
        val bufferedReader = file.bufferedReader()
        return bufferedReader.use { it.readText() }
    }

    private fun getFileNameFromUrl(url: String) = url.removePrefix("https://")
        .replace("/", "~")
}