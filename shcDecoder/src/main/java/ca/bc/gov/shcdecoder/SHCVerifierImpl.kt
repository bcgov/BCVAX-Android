package ca.bc.gov.shcdecoder

import android.content.Context
import android.util.Log
import ca.bc.gov.shcdecoder.cache.CacheManager
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.cache.impl.CacheManagerImpl
import ca.bc.gov.shcdecoder.cache.impl.FileManagerImpl
import ca.bc.gov.shcdecoder.key.KeyManager
import ca.bc.gov.shcdecoder.key.impl.KeyManagerImpl
import ca.bc.gov.shcdecoder.model.Entry
import ca.bc.gov.shcdecoder.model.ImmunizationRecord
import ca.bc.gov.shcdecoder.model.ImmunizationStatus
import ca.bc.gov.shcdecoder.model.Rule
import ca.bc.gov.shcdecoder.parser.SHCParser
import ca.bc.gov.shcdecoder.parser.impl.SHCParserImpl
import ca.bc.gov.shcdecoder.repository.PreferenceRepository
import ca.bc.gov.shcdecoder.rule.RulesManager
import ca.bc.gov.shcdecoder.rule.impl.RulesManagerImpl
import ca.bc.gov.shcdecoder.utils.toDate
import ca.bc.gov.shcdecoder.validator.JWKSValidator
import ca.bc.gov.shcdecoder.validator.impl.JWKSValidatorImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class SHCVerifierImpl(
    val context: Context,
    shcConfig: SHCConfig
) : SHCVerifier {

    private val shcParser: SHCParser = SHCParserImpl()
    private val jwksValidator: JWKSValidator = JWKSValidatorImpl()
    private val fileManager: FileManager = FileManagerImpl(context)
    private val keyManager: KeyManager = KeyManagerImpl(shcConfig, fileManager)
    private val ruleManager: RulesManager = RulesManagerImpl(shcConfig, fileManager)
    private val preferenceRepository = PreferenceRepository(context)
    private val cacheManager: CacheManager =
        CacheManagerImpl(shcConfig, preferenceRepository, fileManager)

    override val config = shcConfig

    companion object {
        private const val TAG = "SHCVerifierImpl"
        const val IMMUNIZATION = "Immunization"
        const val PATIENT = "Patient"
    }

    init {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            try {
                cacheManager.fetch()
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
    }

    override suspend fun hasValidSignature(shcUri: String): Boolean {

        val unSignedJWKSPayload = shcParser.getUnSignedJWKSPayload(shcUri)
        val jwkSignature = shcParser.getJWKSignature(shcUri)

        val shcData = shcParser.parse(shcUri)
        val key = keyManager.getPublicKey(shcData.payload.iss, shcData.header.kid)
            ?: throw SHCDecoderException(
                SHCDecoderException.ID_SIGNATURE_KEY_NOT_FOUND,
                SHCDecoderException.MESSAGE_SIGNATURE_KEY_NOT_FOUND
            )

        return jwksValidator.validate(key, unSignedJWKSPayload, jwkSignature)
    }

    override suspend fun getImmunizationRecord(shcUri: String): ImmunizationRecord {

        val shcData = shcParser.parse(shcUri)
        val entries = shcData.payload.vc.credentialSubject.fhirBundle.entry
        val name = getName(entries)

        val rule = ruleManager.getRule(shcData.payload.iss)
            ?: throw SHCDecoderException(
                SHCDecoderException.ID_INVALID_RUL_SET,
                SHCDecoderException.MESSAGE_INVALID_RULE_SET
            )
        val status = getImmunizationStatus(entries, rule)

        return ImmunizationRecord(name.first, name.second, status)
    }

    private fun getImmunizationStatus(entries: List<Entry>, rule: Rule): ImmunizationStatus {
        var mrnType = 0
        var nrvvType = 0
        var winacType = 0

        entries
            .filter { it.resource.resourceType.contains(IMMUNIZATION) }
            .sortedBy { it.resource.occurrenceDateTime }
            .forEach { entry ->
                val vaxCode = entry.resource.vaccineCode?.coding?.firstOrNull()?.code

                val ruleSet = rule.vaccinationRules.singleOrNull { vaccineRule ->
                    vaxCode?.toInt() == vaccineRule.cvxCode
                }

                when (ruleSet?.type) {
                    1 -> {
                        mrnType += ruleSet.ru
                    }

                    2 -> {
                        nrvvType += ruleSet.ru
                    }

                    3 -> {
                        winacType += ruleSet.ru
                    }
                }

                val vaxDate = entry.resource.occurrenceDateTime?.toDate()
                val enoughDoses = mrnType >= rule.ruRequired
                        || nrvvType >= rule.ruRequired
                        || winacType >= rule.ruRequired
                val enoughMixedDoses = rule.mixTypesAllowed
                        && (mrnType + nrvvType + winacType >= rule.mixTypesRuRequired)
                if (enoughDoses || enoughMixedDoses) {
                    return if (intervalPassed(vaxDate, rule)) {
                        ImmunizationStatus.FULLY_IMMUNIZED
                    } else {
                        ImmunizationStatus.PARTIALLY_IMMUNIZED
                    }
                }
            }

        return if (mrnType + nrvvType + winacType > 0) {
            ImmunizationStatus.PARTIALLY_IMMUNIZED
        } else {
            ImmunizationStatus.INVALID_QR_CODE
        }
    }

    private fun intervalPassed(date: Date?, rule: Rule): Boolean {
        return if (!rule.intervalRequired) {
            true
        } else {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_YEAR, rule.daysSinceLastInterval)
            return (Calendar.getInstance().timeInMillis >= calendar.timeInMillis)
        }
    }

    private fun getName(entries: List<Entry>): Pair<String, String?> {
        val record = entries.filter { entry ->
            entry.resource.resourceType.contains(PATIENT)
        }.map { entry ->
            val name = entry.resource.name?.firstOrNull()
            val sb = StringBuilder()
            if (name != null) {
                if (name.given.joinToString(" ").isNotBlank()) {
                    sb.append(name.given.joinToString(" "))
                }
                if (!name.family.isNullOrBlank()) {
                    sb.append(" ${name.family}")
                }
                Pair(sb.toString(), entry.resource.birthDate)
            } else {
                Pair("Name not found!", entry.resource.birthDate)
            }
        }
        return record.first()
    }
}