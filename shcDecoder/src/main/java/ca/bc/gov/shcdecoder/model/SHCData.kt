package ca.bc.gov.shcdecoder.model

import ca.bc.gov.shcdecoder.SHCVerifierImpl
import ca.bc.gov.shcdecoder.model.helper.Patient

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

fun SHCData.getPatient(): Patient {
    var firstName: String? = null
    var lastName: String? = null
    var dateOfBirth: String? = null
    val entries = payload.vc.credentialSubject.fhirBundle.entry
    val record = entries.filter { entry ->
        entry.resource.resourceType.contains(SHCVerifierImpl.PATIENT)
    }.forEach { entry ->
        val name = entry.resource.name?.firstOrNull()

        if (name != null) {
            if (!name.given.isNullOrEmpty() && name.given.joinToString(" ").isNotBlank()) {
                firstName = name.given.joinToString(" ")
            }
            if (!name.family.isNullOrBlank()) {

                lastName = name.family
            }
        }
        dateOfBirth = entry.resource.birthDate
    }
    return Patient(firstName, lastName, dateOfBirth)
}
