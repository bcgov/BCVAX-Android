package ca.bc.gov.shcdecoder


import ca.bc.gov.shcdecoder.model.Entry
import ca.bc.gov.shcdecoder.model.ImmunizationRecord
import ca.bc.gov.shcdecoder.model.ImmunizationStatus
import ca.bc.gov.shcdecoder.model.Jwks
import ca.bc.gov.shcdecoder.model.JwksKey
import ca.bc.gov.shcdecoder.model.Rule
import ca.bc.gov.shcdecoder.model.SHCData
import ca.bc.gov.shcdecoder.model.SHCHeader
import ca.bc.gov.shcdecoder.model.SHCPayload
import ca.bc.gov.shcdecoder.utils.toDate
import com.google.gson.Gson
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator
import io.jsonwebtoken.io.Decoders
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.util.*
import java.util.zip.DataFormatException
import java.util.zip.Inflater

/**
 * [SHCDecoder] Helper class to decode SMART HEALTH CARD record retrieved from QR.
 * @see {https://spec.smarthealth.cards/} SMART HEALTH CARD framework for mode detail.
 * @see {https://spec.smarthealth.cards/examples/} for examples
 *
 * decode() method is used to perform decoding of Health Data from SMART HEALTH CARD.
 *
 * @author Pinakin Kansara
 */
class SHCDecoder(
    private val shcUri: String
) {


    companion object {
        const val TAG = "SHCDecoder"

        const val IMMUNIZATION = "Immunization"
        const val PATIENT = "Patient"
        const val JANSSEN_SNOWMED =
            "28951000087107" // TODO: 03/09/21 This will be removed in future
        const val JANSSEN_CVX = "212"
    }

    private val algorithm = SignatureAlgorithm.ES256


    fun getIss(): String {
        val signedJwks = shcUriToBase64(shcUri)

        val shcData = decodeBase64EncodedSHCPayload(signedJwks)

        val shcPayload = shcData.payload

        return shcPayload.iss
    }

    fun validateSignature(jwks: Jwks, shcUri: String): Boolean {
        val key = getPublicKey(jwks.keys.first())
        val signedJwks = shcUriToBase64(shcUri)
        return isValidSignature(key, signedJwks)
    }

    private fun immunizationStatus(entries: List<Entry>, rule: Rule): ImmunizationStatus {
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

    /**
     * Helper method to fetch Immunization status.
     *
     * @param shcUri String
     * @param rule Rule
     * @return [ImmunizationRecord] patient name & Immunization status.
     */
    fun determineImmunizationStatus(shcUri: String, rule: Rule): ImmunizationRecord {
        val signedJwks = shcUriToBase64(shcUri)

        val shcData = decodeBase64EncodedSHCPayload(signedJwks)

        val entries = shcData.payload.vc.credentialSubject.fhirBundle.entry

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

        val status = immunizationStatus(entries, rule)

        return ImmunizationRecord(
            record.first().first, record.first().second, status
        )
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

    /**
     * Helper method to fetch Immunization status.
     *
     * @param shcUri String
     * @return [ImmunizationRecord] patient name & Immunization status
     */
    fun determineImmunizationStatus(shcUri: String): ImmunizationRecord {
        val signedJwks = shcUriToBase64(shcUri)

        val shcData = decodeBase64EncodedSHCPayload(signedJwks)

        val entries = shcData.payload.vc.credentialSubject.fhirBundle.entry

        val record = entries.filter { entry ->
            entry.resource.resourceType.contains(PATIENT)
        }.map { entry ->
            val name = entry.resource.name?.firstOrNull()
            if (name != null) {
                Pair("${name.given.joinToString(" ")} ${name.family}", entry.resource.birthDate)
            } else {
                Pair("Name not found!", entry.resource.birthDate)
            }
        }

        var vaccines = 0
        var oneDoseVaccines = 0

        entries.forEach { entry ->

            if (entry.resource.resourceType.contains(IMMUNIZATION)) {
                val vaxCode = entry.resource.vaccineCode?.coding?.firstOrNull()?.code
                vaxCode?.let { code ->
                    if (code.contentEquals(JANSSEN_CVX) || code.contentEquals(
                            JANSSEN_SNOWMED
                        )
                    ) {
                        oneDoseVaccines++
                    } else {
                        vaccines++
                    }
                }
            }
        }

        val status = when {
            oneDoseVaccines > 0 || vaccines > 1 -> {
                ImmunizationStatus.FULLY_IMMUNIZED
            }
            vaccines > 0 -> {
                ImmunizationStatus.PARTIALLY_IMMUNIZED
            }
            else -> {
                ImmunizationStatus.INVALID_QR_CODE
            }
        }

        return ImmunizationRecord(
            record.first().first, record.first().second, status
        )
    }

    /**
     * Helper method to get [ECPublicKey] for JWS verification.
     *
     * @param jwksKey JwksKey
     * @return [ECPublicKey] used for signature verification
     */
    private fun getPublicKey(jwksKey: JwksKey): ECPublicKey {
        val name = "secp256r1"

        val params = ECNamedCurveTable.getParameterSpec(name)
        val spec =
            ECNamedCurveSpec(name, params.curve, params.g, params.n, params.h, params.seed)

        val parsedX = BigInteger(1, Base64.getUrlDecoder().decode(jwksKey.x))
        val parsedY = BigInteger(1, Base64.getUrlDecoder().decode(jwksKey.y))
        val point = ECPoint(parsedX, parsedY)
        val key = KeyFactory
            .getInstance("EC")
            .generatePublic(ECPublicKeySpec(point, spec)) as ECPublicKey
        algorithm.assertValidVerificationKey(key)
        return key
    }

    /**
     * Helper method to verify that payload is signed or forged.
     *
     * @param [key] EcPublicKey and [jwkSigned] String
     * @return true is payload is signed with valid signature else false
     */
    private fun isValidSignature(key: ECPublicKey, jwkSigned: String): Boolean {
        val jwksParts = jwkSigned.split('.')
        if (jwksParts.isNullOrEmpty() || jwksParts.size != 3) {
            throw SHCDecoderException(
                SHCDecoderException.ID_INVALID_PAYLOAD_DATA_FORMAT,
                SHCDecoderException.MESSAGE_INVALID_PAYLOAD_DATA_FORMAT
            )
        }
        val jwsWithoutSig = jwksParts[0] + "." + jwksParts[1]
        val jwsSig = jwksParts[2]

        val validator = DefaultJwtSignatureValidator(algorithm, key, Decoders.BASE64URL)
        return validator.isValid(jwsWithoutSig, jwsSig)
    }

    /**
     * Helper method to decode base64 payload
     * and convert to [SHCData]
     *
     * @param payLoad String encoded SHC DATA
     * @return [SHCData] shcData contains SMART HEALTH CARD DATA
     */
    private fun decodeBase64EncodedSHCPayload(payLoad: String): SHCData {
        val payLoads = payLoad.split('.')
        if (payLoads.isNullOrEmpty() || payLoads.size != 3) {
            throw SHCDecoderException(
                SHCDecoderException.ID_INVALID_PAYLOAD_DATA_FORMAT,
                SHCDecoderException.MESSAGE_INVALID_PAYLOAD_DATA_FORMAT
            )
        }

        val shcHeader = decodeSHCHeader(payLoads[0])
        val shcPayload = decodeSHCPayload(payLoads[1])
        val shcSignature = decodeSHCSignature(payLoads[2])

        return SHCData(shcHeader, shcPayload, shcSignature)
    }

    /**
     * Helper method to decode base64 payload
     * Convert JSON to [SHCHeader]
     *
     * @param payLoad String encoded SHC header
     * @return [SHCHeader] shcHeader
     */
    private fun decodeSHCHeader(payLoad: String): SHCHeader {
        val decodedHeaderString = Base64.getUrlDecoder().decode(payLoad).decodeToString()
        return Gson().fromJson(decodedHeaderString, SHCHeader::class.java)
    }

    /**
     * Helper method to decode base64 payload
     * Convert JSON to [SHCPayload]
     * @param payLoad String encoded SHC Payload
     * @return [SHCPayload] shcPayload
     */
    private fun decodeSHCPayload(payLoad: String): SHCPayload {
        val decodedPayLoadString = Base64.getUrlDecoder().decode(payLoad)
        val decodedSHCPayLoadString = inflate(decodedPayLoadString)
        return Gson().fromJson(decodedSHCPayLoadString, SHCPayload::class.java)
    }

    /**
     * Helper method to decode base 64 payload
     * Convert JSON to [String]
     *
     * @param payLoad String encoded SHC Signature
     * @return [String] shcSignature
     */
    private fun decodeSHCSignature(payLoad: String): String {
        // TODO: decide signature is required to be decoded
        // return Base64.getUrlDecoder().decode(payLoad).decodeToString()
        return payLoad
    }

    /**
     * Helper method to decompress SHC JSON data.
     *
     * @param deflatedBase64 ZIP compressed base64Encoded String.
     * @return deflated JSON String of SHC DATA.
     * @throws [DataFormatException] data format exception
     */
    private fun inflate(deflatedBase64: ByteArray): String {
        val deCompressor = Inflater(true)
        deCompressor.setInput(deflatedBase64)
        val result = ByteArray(10000)
        val resultLength = deCompressor.inflate(result)
        deCompressor.end()
        return String(result, 0, resultLength, Charsets.UTF_8)
    }

    /**
     * [shcUriToBase64] is a helper method which convert SHC URI String to
     * encodedBase64 String.
     *
     * @param shcUri String SHC encoded.
     * @return encodedBase64 String
     */
    private fun shcUriToBase64(shcUri: String): String {
        // REMOVE SHC PREFIX
        val encodedBase64 = shcUri.removePrefix("shc:/")

        val size = (encodedBase64.length - 1)

        // GET LIST WITH PAIRS OF DIGITS
        val pairs = encodedBase64.zipWithNext().slice(0..size step 2)

        val base64Builder = StringBuilder()

        // PERFORM ASCII ENCODING
        pairs.forEach { pair ->
            val x = "${pair.first}${pair.second}".toIntOrNull()
            x.let {
                base64Builder.append(it?.plus(45)?.toChar())
            }
        }

        // RETURN BASE64 ENCODED STRING
        return base64Builder.toString()
    }
}
