package ca.bc.gov.shcdecoder.validator.impl

import ca.bc.gov.shcdecoder.validator.JWKSValidator
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator
import io.jsonwebtoken.io.Decoders
import java.security.interfaces.ECPublicKey

class JWKSValidatorImpl : JWKSValidator {


    override suspend fun validate(
        key: ECPublicKey,
        unSignedJWKSPayload: String,
        signature: String
    ): Boolean {
        val validator =
            DefaultJwtSignatureValidator(SignatureAlgorithm.ES256, key, Decoders.BASE64URL)
        return validator.isValid(unSignedJWKSPayload,signature)
    }
}