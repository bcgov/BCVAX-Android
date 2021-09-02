package ca.bc.gov.vaxcheck.utils

/**
 * [SHCDecoderException]
 *
 * @auther Pinakin Kansara
 */
class SHCDecoderException(
    val errCode: Int,
    message: String? = null
) : Exception(message)
