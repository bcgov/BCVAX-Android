package ca.bc.gov.shcdecoder.model

/**
 *
 * @author Jose Naranjo
 */
data class Exemptions(
    val issuer: String?,
    val codingSystems: List<String?>?,
    val codes: List<String?>?
)
