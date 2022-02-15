package ca.bc.gov.shcdecoder.model

/**
 * [JwksKey]
 *
 * @author Pinakin Kansara
 */
data class JwksKey(

    val kty: String,

    val kid: String,

    val use: String,

    val alg: String,

    val crv: String,

    val x: String,

    val y: String,

    val x5c: List<String>,

    val ctr: Long?
)
