package ca.bc.gov.vaxcheck.di

import android.content.Context
import ca.bc.gov.shcdecoder.SHCConfig
import ca.bc.gov.shcdecoder.SHCVerifier
import ca.bc.gov.shcdecoder.SHCVerifierImpl
import ca.bc.gov.shcdecoder.model.DefaultJWKSKeys
import ca.bc.gov.shcdecoder.model.Jwks
import ca.bc.gov.shcdecoder.model.Rule
import ca.bc.gov.shcdecoder.model.TrustedIssuersResponse
import ca.bc.gov.shcdecoder.model.ValidationRuleResponse
import ca.bc.gov.vaxcheck.BuildConfig
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.utils.readJsonFromAsset
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [DecoderModule]
 *
 *
 * @author Pinakin Kansara
 */
@Module
@InstallIn(SingletonComponent::class)
class DecoderModule {

    @Provides
    fun providesDefaultRule(@ApplicationContext context: Context): List<Rule> {
        val jsonString = context.readJsonFromAsset(context.getString(R.string.default_rules_json))
        val ruleSet = Gson().fromJson(jsonString, ValidationRuleResponse::class.java)
        return ruleSet.ruleSet
    }

    @Provides
    fun providesDefaultJWKSKeys(@ApplicationContext context: Context): List<DefaultJWKSKeys> {

        val issuersJsonString =
            context.readJsonFromAsset(context.getString(R.string.default_issuer_json))
        val issuersResponse = Gson().fromJson(issuersJsonString, TrustedIssuersResponse::class.java)
        val defaultKeys = mutableListOf<DefaultJWKSKeys>()
        issuersResponse.trustedIssuers.forEach { issuer ->
            val url = if (issuer.iss.endsWith("/.well-known/jwks.json")) {
                issuer.iss
            } else {
                "${issuer.iss}/.well-known/jwks.json"
            }

            try {
                val fileName = url.removePrefix("https://").replace("/", "~")
                val jwksKeysJson = context.readJsonFromAsset(fileName)
                val jwks = Gson().fromJson(jwksKeysJson, Jwks::class.java)
                defaultKeys.add(DefaultJWKSKeys(issuer.iss, jwks.keys))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return defaultKeys.toList()
    }

    @Provides
    fun provideShcConfig(
        @ApplicationContext context: Context,
        defaultJWKSKeys: List<DefaultJWKSKeys>,
        rules: List<Rule>
    ) =
        SHCConfig(
            context.getString(R.string.issuer_url),
            context.getString(R.string.rules_url),
            defaultJWKSKeys,
            rules,
            if (BuildConfig.FLAVOR == "prod") PROD_EXPIRY_TIME else TEST_EXPIRY_TIME
        )

    @Provides
    @Singleton
    fun providesSHCVerifier(
        @ApplicationContext context: Context,
        shcConfig: SHCConfig
    ): SHCVerifier = SHCVerifierImpl(
        context,
        shcConfig
    )

    companion object {
        private const val PROD_EXPIRY_TIME = 21600000L // 6 Hours
        private const val TEST_EXPIRY_TIME = 120000L // 2 Minutes
    }
}
