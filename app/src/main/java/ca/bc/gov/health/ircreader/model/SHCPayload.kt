package ca.bc.gov.health.ircreader.model

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
