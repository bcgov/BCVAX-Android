package ca.bc.gov.health.ircreader.utils

/**
 * [SHCDecoderException]
 *
 * @auther Pinakin Kansara
 */
class SHCDecoderException(
    val errCode: Int,
    message: String? = null
) : Exception(message)