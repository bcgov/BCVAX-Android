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
import ca.bc.gov.shcdecoder.repository.impl.PreferenceRepositoryImpl
import ca.bc.gov.shcdecoder.rule.RulesManager
import ca.bc.gov.shcdecoder.rule.impl.RulesManagerImpl
import ca.bc.gov.shcdecoder.utils.addDays
import ca.bc.gov.shcdecoder.utils.inclusiveAfter
import ca.bc.gov.shcdecoder.utils.toDate
import ca.bc.gov.shcdecoder.validator.JWKSValidator
import ca.bc.gov.shcdecoder.validator.impl.JWKSValidatorImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Date

class SHCVerifierImpl(
    val context: Context,
    shcConfig: SHCConfig
) : SHCVerifier {

    private val shcParser: SHCParser = SHCParserImpl()
    private val jwksValidator: JWKSValidator = JWKSValidatorImpl()
    private val fileManager: FileManager = FileManagerImpl(context)
    private val keyManager: KeyManager = KeyManagerImpl(shcConfig, fileManager)
    private val ruleManager: RulesManager = RulesManagerImpl(shcConfig, fileManager)
    private val preferenceRepository: PreferenceRepository = PreferenceRepositoryImpl(context)
    private val cacheManager: CacheManager = CacheManagerImpl(shcConfig, preferenceRepository, fileManager)

    override val config = shcConfig

    companion object {
        private const val TAG = "SHCVerifierImpl"
        const val IMMUNIZATION = "Immunization"
        const val PATIENT = "Patient"

        private const val CONDITION = "Condition"
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

        val status = getImmunizationStatus(entries, shcData.payload.exp, rule)

        return ImmunizationRecord(name.first, name.second, status)
    }

    private fun isShcExpired(expDateInSeconds: Double?): Boolean {
        return expDateInSeconds?.times(1000)?.toLong()?.let {
            return expDateInSeconds > 0 && Date().after(Date(it))
        } ?: false
    }

    private fun getImmunizationStatus(entries: List<Entry>, expDateInSeconds: Double?, rule: Rule): ImmunizationStatus {
        var mrnType = 0
        var nrvvType = 0
        var winacType = 0
        var minInterval = 0
        var lastVaxDate: Date? = null

        if (isShcExpired(expDateInSeconds)) {
            return ImmunizationStatus.INVALID_QR_CODE
        }

        entries
            .filter { it.resource.resourceType.contains(CONDITION) }
            .forEach { entry ->
                val onsetDateMillis = entry.resource.onsetDateTime?.toDate()?.time ?: Long.MIN_VALUE
                val abatementDateMillis = entry.resource.abatementDateTime?.toDate()?.time ?: Long.MAX_VALUE

                val isDateValid = Date().time in onsetDateMillis..abatementDateMillis

                val isValidSystem = entry.resource.code?.coding?.any { coding -> coding.system.contains(config.deferralsDomain) } ?: false

                if (isDateValid && isValidSystem) {
                    return ImmunizationStatus.FULLY_IMMUNIZED
                }
            }

        entries
            .filter { it.resource.resourceType.contains(IMMUNIZATION) }
            .sortedBy { it.resource.occurrenceDateTime }
            .forEach { entry ->
                val vaxCode = entry.resource.vaccineCode?.coding?.firstOrNull()?.code

                val ruleSet = rule.vaccinationRules.singleOrNull { vaccineRule ->
                    vaxCode?.toInt() == vaccineRule.cvxCode
                }

                val hasMinDaysPassed = hasPassedMinDaysRequiredBetweenDoses(
                    entry.resource.occurrenceDateTime?.toDate(),
                    lastVaxDate,
                    minInterval
                )

                if (hasMinDaysPassed) {
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
                }
                val vaxDate = entry.resource.occurrenceDateTime?.toDate()
                lastVaxDate = vaxDate
                minInterval = ruleSet?.minDays ?: 0
                val enoughDoses = mrnType >= rule.ruRequired ||
                    nrvvType >= rule.ruRequired ||
                    winacType >= rule.ruRequired
                val enoughMixedDoses = rule.mixTypesAllowed &&
                    (mrnType + nrvvType + winacType >= rule.mixTypesRuRequired)
                if (enoughDoses || enoughMixedDoses) {
                    return if (!rule.intervalRequired ||
                        (
                            rule.intervalRequired &&
                                intervalPassed(
                                        vaxDate, rule.daysSinceLastInterval
                                    )
                            )
                    ) {
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

    private fun hasPassedMinDaysRequiredBetweenDoses(
        currentDoseTime: Date?,
        lastDoseTime: Date?,
        daysSinceLastInterval: Int
    ): Boolean {

        if (lastDoseTime == null) {
            return true
        }

        val expectedDate = lastDoseTime.addDays(daysSinceLastInterval)

        return currentDoseTime?.inclusiveAfter(expectedDate) == true
    }

    private fun intervalPassed(date: Date?, daysSinceLastInterval: Int): Boolean {
        val expectedDate = date?.addDays(daysSinceLastInterval)
        return (Date().after(expectedDate))
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
