package ca.bc.gov.vaxcheck.utils

import ca.bc.gov.vaxcheck.model.ImmunizationStatus
import ca.bc.gov.vaxcheck.model.Jwks
import ca.bc.gov.vaxcheck.model.JwksKey
import ca.bc.gov.vaxcheck.model.SHCData
import ca.bc.gov.vaxcheck.model.SHCHeader
import ca.bc.gov.vaxcheck.model.SHCPayload
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
class SHCDecoder {

    companion object {
        const val INVALID_PAYLOAD = 100
        const val INVALID_PAYLOAD_DATA_FORMAT = 1001
        const val INVALID_PAYLOAD_EXCEPTION_MESSAGE = "ERROR PROCESSING SHC PAYLOAD"
        const val INVALID_SIGNATURE_KEY = 2001

        const val IMMUNIZATION = "Immunization"
        const val PATIENT = "Patient"
        const val JANSSEN_SNOWMED =
            "28951000087107" // TODO: 03/09/21 This will be removed in future
        const val JANSSEN_CVX = "212"
    }

    private val algorithm = SignatureAlgorithm.ES256

    /**
     * Performs signature verification on the provided shcUri
     * and provides ImmunizationStatus.
     *
     * @param [shcUri] from barcode and [jwks] from json.
     * @return Immunization status from given SHCData or exception if signature or payload is not valid.
     */
    fun getImmunizationStatus(shcUri: String, jwks: String): Pair<String, ImmunizationStatus> {

        if (jwks.isBlank() || jwks.isEmpty()) {
            throw SHCDecoderException(INVALID_SIGNATURE_KEY, "JWKS should not be empty or blank")
        }

        val jwks = Gson().fromJson(jwks, Jwks::class.java)

        if (jwks == null || jwks.keys.isNullOrEmpty()) {
            throw SHCDecoderException(INVALID_SIGNATURE_KEY, "JWKS Key not found.")
        }

        val jwkSigned = shcUriToBase64(shcUri)

        val key = getPublicKey(jwks.keys.first())

        if (!isValidSignature(key, jwkSigned)) {
            throw SHCDecoderException(INVALID_SIGNATURE_KEY, "Signing keys are not valid")
        }

        val shcData = decodeBase64EncodedSHCPayload(jwkSigned)

        return determineImmunizationStatus(shcData)
    }

    /**
     * Helper method to fetch Immunization status.
     *
     * @param shcData SHCData
     * @return [Pair] patient name & Immunization status
     */
    private fun determineImmunizationStatus(shcData: SHCData): Pair<String, ImmunizationStatus> {
        val entries = shcData.payload.vc.credentialSubject.fhirBundle.entry

        val names = entries.filter { entry ->
            entry.resource.resourceType.contains(PATIENT)
        }.map { entry ->
            val name = entry.resource.name?.firstOrNull()
            if(name != null){
                "${name.given.joinToString(" ")} ${name.family}"
            }else {
                "Name not found!"
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
                ImmunizationStatus.NO_RECORD
            }
        }

        return Pair(names.first(), status)
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
            throw SHCDecoderException(INVALID_PAYLOAD, INVALID_PAYLOAD_EXCEPTION_MESSAGE)
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
            throw SHCDecoderException(INVALID_PAYLOAD, INVALID_PAYLOAD_EXCEPTION_MESSAGE)
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
