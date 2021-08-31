package ca.bc.gov.health.ircreader.utils

import ca.bc.gov.health.ircreader.model.SHCData
import ca.bc.gov.health.ircreader.model.SHCHeader
import ca.bc.gov.health.ircreader.model.SHCPayload
import com.google.gson.Gson
import java.util.Base64
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
    }

    /**
     * @return
     */
    fun decode(
        shcUri: String,
        onSuccess: (shcData: SHCData) -> Unit,
        onError: (error: SHCDecoderException) -> Unit
    ) {

        try {
            val base64EncodedSHCPayload = shcUriToBase64(shcUri)
            onSuccess(decodeBase64EncodedSHCPayload(base64EncodedSHCPayload))
        } catch (e: DataFormatException) {
            onError(SHCDecoderException(INVALID_PAYLOAD_DATA_FORMAT, e.message))
        } catch (e: SHCDecoderException) {
            onError(e)
        } catch (e: Exception) {
            onError(SHCDecoderException(INVALID_PAYLOAD, e.message))
        }
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
