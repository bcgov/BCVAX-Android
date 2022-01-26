package ca.bc.gov.shcdecoder.validator

import java.security.interfaces.ECPublicKey

interface JWKSValidator {

    suspend fun validate(
        key: ECPublicKey,
        unSignedJWKSPayload: String,
        signature: String
    ): Boolean
}
