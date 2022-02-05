package ca.bc.gov.shcdecoder.model

data class RevocationsResponse(
    val kid: String,
    val method: String?,
    val ctr: String?,
    val rids: List<String>
)