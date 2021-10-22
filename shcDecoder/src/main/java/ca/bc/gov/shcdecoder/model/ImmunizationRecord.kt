package ca.bc.gov.shcdecoder.model

/**
 * [ImmunizationRecord]
 *
 * @author Pinakin Kansara
 */
data class ImmunizationRecord(
    val name: String,
    val birthDate: String?,
    val status: ImmunizationStatus
)
