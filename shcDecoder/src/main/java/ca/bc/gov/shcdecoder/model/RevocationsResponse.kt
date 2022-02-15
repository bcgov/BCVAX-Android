package ca.bc.gov.shcdecoder.model

data class RevocationsResponse(
    val kid: String,
    val method: String?,
    val ctr: Long?,
    val rids: List<String>
)