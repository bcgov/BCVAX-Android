package ca.bc.gov.shcdecoder.parser.impl

import ca.bc.gov.shcdecoder.SHCDecoderException
import ca.bc.gov.shcdecoder.model.SHCData
import ca.bc.gov.shcdecoder.model.SHCHeader
import ca.bc.gov.shcdecoder.model.SHCPayload
import ca.bc.gov.shcdecoder.parser.SHCParser
import com.google.gson.Gson
import java.util.*
import java.util.zip.Inflater

class SHCParserImpl : SHCParser {

    override suspend fun parse(shcUri: String): SHCData {

        val jwksParts = getJWKSParts(shcUri)

        val headerString = Base64.getUrlDecoder().decode(jwksParts[0]).decodeToString()
        val deflatedPayload = Base64.getUrlDecoder().decode(jwksParts[1])
        val signatureString = jwksParts[2]

        val shcHeader: SHCHeader = Gson().fromJson(headerString, SHCHeader::class.java)

        val inflatedPayload = inflate(deflatedPayload)
        val shcPayload: SHCPayload = Gson().fromJson(inflatedPayload, SHCPayload::class.java)

        return SHCData(shcHeader, shcPayload, signatureString)
    }

    override suspend fun getSignedJWKSPayload(shcUri: String): String {
        return getEncodedBase64(shcUri)
    }

    override suspend fun getUnSignedJWKSPayload(shcUri: String): String {
        val jwksParts = getJWKSParts(shcUri)
        return jwksParts[0] + "." + jwksParts[1]
    }

    override suspend fun getJWKSignature(shcUri: String): String {
        return getJWKSParts(shcUri)[2]
    }

    private fun getJWKSParts(shcUri: String): List<String> {
        val signedJWKS = getEncodedBase64(shcUri)
        val jwksParts = signedJWKS.split('.')
        if (jwksParts.isNullOrEmpty() || jwksParts.size != 3) {
            throw SHCDecoderException(
                SHCDecoderException.ID_INVALID_PAYLOAD_DATA_FORMAT,
                SHCDecoderException.MESSAGE_INVALID_PAYLOAD_DATA_FORMAT
            )
        }
        return jwksParts
    }

    private fun getEncodedBase64(shcUri: String): String {
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

    private fun inflate(deflatedBase64: ByteArray): String {
        val deCompressor = Inflater(true)
        deCompressor.setInput(deflatedBase64)
        val result = ByteArray(10000)
        val resultLength = deCompressor.inflate(result)
        deCompressor.end()
        return String(result, 0, resultLength, Charsets.UTF_8)
    }
}