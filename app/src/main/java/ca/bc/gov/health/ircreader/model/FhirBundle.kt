package ca.bc.gov.health.ircreader.model

/**
 * [FhirBundle]
 *
 * @author Pinakin Kansara
 */
data class FhirBundle(
    val resourceType: String,
    val type: String,
    val entry: List<Entry>
)
