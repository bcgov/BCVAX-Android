package ca.bc.gov.shcdecoder.cache.impl

import android.content.Context
import android.util.Log
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.model.Issuer
import ca.bc.gov.shcdecoder.model.Jwks
import ca.bc.gov.shcdecoder.model.JwksKey
import ca.bc.gov.shcdecoder.model.RevocationsResponse
import ca.bc.gov.shcdecoder.model.Rule
import ca.bc.gov.shcdecoder.model.TrustedIssuersResponse
import ca.bc.gov.shcdecoder.model.ValidationRuleResponse
import ca.bc.gov.shcdecoder.utils.epochToDate
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Date

class FileManagerImpl(
    context: Context
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
        return issuerResponse?.trustedIssuers.orEmpty()
    }

    override suspend fun getKeys(url: String): List<JwksKey> {
        val jsonString = getJsonStringFromFile(url)
        val keyResponse = Gson().fromJson(jsonString, Jwks::class.java)
        return keyResponse?.keys.orEmpty()
    }

    override suspend fun getRule(url: String): List<Rule> {
        val jsonString = getJsonStringFromFile(url)
        val validationRulesResponse =
            Gson().fromJson(jsonString, ValidationRuleResponse::class.java)
        return validationRulesResponse?.ruleSet.orEmpty()
    }

    override suspend fun getRevocations(url: String): List<Pair<String, Date?>> {
        val jsonString = getJsonStringFromFile(url)
        val revocationsResponse = Gson().fromJson(jsonString, RevocationsResponse::class.java)
        return revocationsResponse?.rids?.map { rid ->
            if (rid.contains(".") && (rid.startsWith(".").not() && rid.endsWith(".").not())) {
                val ridSplit = rid.split(".")
                ridSplit.first() to ridSplit[1].epochToDate()
            } else {
                rid to null
            }
        }.orEmpty()
    }

    private fun getJsonStringFromFile(url: String): String {
        return try {
            val fileName = getFileNameFromUrl(url)
            val file = File(downloadDir, fileName)
            val bufferedReader = file.bufferedReader()
            bufferedReader.use { it.readText() }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return String()
        }
    }

    private fun getFileNameFromUrl(url: String) = url.removePrefix("https://")
        .replace("/", "~")
}
