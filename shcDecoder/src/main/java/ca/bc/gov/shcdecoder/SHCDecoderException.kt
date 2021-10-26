package ca.bc.gov.shcdecoder

/**
 * [SHCDecoderException]
 *
 * @auther Pinakin Kansara
 */
class SHCDecoderException(
    val errCode: Int,
    message: String? = null
) : Exception(message) {

    companion object{
        const val ID_INVALID_PAYLOAD_DATA_FORMAT = 1001
        const val ID_INVALID_SIGNATURE_KEY = 2001
        const val ID_SIGNATURE_KEY_NOT_FOUND = 3001
        const val ID_INVALID_RUL_SET= 4001

        const val MESSAGE_SIGNATURE_KEY_NOT_FOUND = "SIGNATURE KEY NOT FOUND"
        const val MESSAGE_INVALID_SIGNATURE_KEY = "SIGNATURE KEY ARE NOT VALID"
        const val MESSAGE_INVALID_PAYLOAD_DATA_FORMAT = "SHC PAYLOAD IS NOT VALID"
        const val MESSAGE_INVALID_PAYLOAD_EXCEPTION = "ERROR PROCESSING SHC PAYLOAD"
        const val MESSAGE_INVALID_RULE_SET="RUL SET IS NOT VALID"

    }
}
