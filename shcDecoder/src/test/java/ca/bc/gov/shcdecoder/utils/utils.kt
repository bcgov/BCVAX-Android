package ca.bc.gov.shcdecoder.utils

import ca.bc.gov.shcdecoder.model.JwksKey
import io.jsonwebtoken.SignatureAlgorithm
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.util.Base64

fun derivePublicKey(jwksKey: JwksKey): ECPublicKey {
    val name = "secp256r1"

    val params = ECNamedCurveTable.getParameterSpec(name)
    val spec =
        ECNamedCurveSpec(name, params.curve, params.g, params.n, params.h, params.seed)

    val parsedX = BigInteger(1, Base64.getUrlDecoder().decode(jwksKey.x))
    val parsedY = BigInteger(1, Base64.getUrlDecoder().decode(jwksKey.y))
    val point = ECPoint(parsedX, parsedY)
    val key = KeyFactory
        .getInstance("EC")
        .generatePublic(ECPublicKeySpec(point, spec)) as ECPublicKey
    SignatureAlgorithm.ES256.assertValidVerificationKey(key)
    return key
}