package ca.bc.gov.shcdecoder.model

/**
 * [Vc]
 *
 * @author Pinakin Kansara
 */

data class Vc(
    val rid: String?,
    val type: List<String>,
    val credentialSubject: CredentialSubject
)
