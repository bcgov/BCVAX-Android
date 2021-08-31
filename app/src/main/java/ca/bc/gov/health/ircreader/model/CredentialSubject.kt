package ca.bc.gov.health.ircreader.model

/**
 * [CredentialSubject]
 *
 * @author Pinakin Kansara
 */
data class CredentialSubject(
    val fhirVersion: String,
    val fhirBundle: FhirBundle
)
