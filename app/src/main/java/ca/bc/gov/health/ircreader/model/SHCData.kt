package ca.bc.gov.health.ircreader.model

/**
 * [SHCData] holds data decoded from SMART HEALTH CARD.
 *
 * @author Pinakin Kansara
 */
data class SHCData(
    val header: SHCHeader,
    val payload: SHCPayload,
    val signature: String
)