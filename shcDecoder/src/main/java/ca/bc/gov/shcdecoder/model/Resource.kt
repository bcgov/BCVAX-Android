package ca.bc.gov.shcdecoder.model

import com.google.gson.annotations.SerializedName

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
    val vaccineCode: ResourceCode? = null,
    val code: ResourceCode? = null,
    val patient: Patient? = null,
    val occurrenceDateTime: String? = null,
    val performer: List<Performer>? = null,
    val lotNumber: String? = null,
    val onsetDateTime: String? = null,
    val abatementDateTime: String? = null
)
