package ca.bc.gov.shcdecoder.repository

import android.content.Context
import ca.bc.gov.shcdecoder.FileUtils
import ca.bc.gov.shcdecoder.R
import ca.bc.gov.shcdecoder.model.Jwks
import ca.bc.gov.shcdecoder.model.Rule
import ca.bc.gov.shcdecoder.model.TrustedIssuersResponse
import ca.bc.gov.shcdecoder.model.ValidationRuleResponse
import ca.bc.gov.shcdecoder.utils.readJsonFromAsset
import com.google.gson.Gson

class KeysRepository(
    private val context: Context
) {
    private val fileUtils: FileUtils = FileUtils(context)

    fun getKeyForIssuer(iss: String): Jwks? {

        val keys = fileUtils.getKeyForIssuer(iss)

        if (keys != null) {
            return keys
        }


        val issuerJsonString =
            context.readJsonFromAsset(getFileNameFromUrl(context.getString(R.string.issuer_url)))

        val trustedIssuerResponse =
            Gson().fromJson(issuerJsonString, TrustedIssuersResponse::class.java)

        val issuer = trustedIssuerResponse.trustedIssuers.singleOrNull { issuer ->
            iss == issuer.iss
        }

        if (issuer != null) {
            val url = "${iss}/.well-known/jwks.json"
            val jwksJsonString = context.readJsonFromAsset(getFileNameFromUrl(url))
            return Gson().fromJson(jwksJsonString, Jwks::class.java)
        }
        return null
    }

    fun getRuleSetForIssuer(iss: String): Rule? {

        var rule: Rule? = null
        try {
            rule = fileUtils.getRuleSetForIssuer(iss)
        } catch (e: Exception) {

        }

        if (rule != null) {
            return rule
        }

        val ruleJsonString =
            context.readJsonFromAsset(getFileNameFromUrl(context.getString(R.string.rules_url)))

        val rulesResponse = Gson().fromJson(ruleJsonString, ValidationRuleResponse::class.java)

        var ruleSet: Rule? = null

        rulesResponse.ruleSet.forEach { rule ->

            if (rule.ruleTarget.endsWith("issuers.json")) {
                val issuerJsonString =
                    context.readJsonFromAsset(getFileNameFromUrl(context.getString(R.string.issuer_url)))

                val trustedIssuerResponse =
                    Gson().fromJson(issuerJsonString, TrustedIssuersResponse::class.java)

                val issuer = trustedIssuerResponse.trustedIssuers.singleOrNull { issuer ->
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

    private fun getFileNameFromUrl(url: String): String {
        val fileName = url.removePrefix("https://")
        return fileName.replace("/", "~")
    }
}