package ca.bc.gov.shcdecoder.model

/**
 * [SHCPayload] holds SMART HEALTH CARD DATA.
 *
 * @author Pinakin Kansara
 */

data class SHCPayload(
    val iss: String,
    val nbf: Double,
    val vc: Vc
)
