package ca.bc.gov.shcdecoder.model


/**
 * [Resource]
 *
 * @author Pinakin Kansara
 */

data class Resource(
    val resourceType: String,
    val name: List<Name>? = null,
    val birthDate: String? = null,
    val status: String? = null,
    val vaccineCode: VaccineCode? = null,
    val patient: Patient? = null,
    val occurrenceDateTime: String? = null,
    val performer: List<Performer>? = null,
    val lotNumber: String? = null
)
