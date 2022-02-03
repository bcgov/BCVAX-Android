package ca.bc.gov.shcdecoder.model

data class DefaultJWKSKeys(
    val iss: String,
    val jwksKeys: List<JwksKey>
)
