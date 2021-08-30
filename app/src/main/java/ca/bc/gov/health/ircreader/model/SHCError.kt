package ca.bc.gov.health.ircreader.model

/**
 * [SHCError] holds error code & message
 *
 * @author Pinakin Kansara
 */
data class SHCError(
    val errorCode: Int,
    val message: String
)
