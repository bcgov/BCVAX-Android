package ca.bc.gov.shcdecoder.model

/**
 * [SHCHeader] holds data retrieved from SMART HEALTH CARD.
 *
 * @author Pinakin Kansara
 */

data class SHCHeader(
    val zip: String,
    val alg: String,
    val kid: String
)
