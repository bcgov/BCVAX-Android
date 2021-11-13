package ca.bc.gov.shcdecoder.key.impl

import ca.bc.gov.shcdecoder.SHCConfig
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.cache.impl.CacheManagerImpl
import ca.bc.gov.shcdecoder.key.KeyManager
import ca.bc.gov.shcdecoder.model.JwksKey
import io.jsonwebtoken.SignatureAlgorithm
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.util.*

class KeyManagerImpl(
    private val shcConfig: SHCConfig,
    private val fileManager: FileManager
) : KeyManager {

    override suspend fun getPublicKey(iss: String, kid: String): ECPublicKey? {

        try {

            val issuers = fileManager.getIssuers(shcConfig.issuerEndPoint)
            val issuerUrls = issuers.map { issuer -> issuer.iss }

            if (issuerUrls.contains(iss)) {
                val keyUrl = if (iss.endsWith(CacheManagerImpl.SUFFIX_JWKS_JSON)) {
                    iss
                } else {
                    "${iss}${CacheManagerImpl.SUFFIX_JWKS_JSON}"
                }
                val keys = fileManager.getKeys(keyUrl)
                val publicKeys = keys.filter { jwksKey -> jwksKey.kid == kid }
                    .map { jwksKey -> derivePublicKey(jwksKey) }
                return publicKeys.firstOrNull() ?: getDefaultPublicKey(iss, kid)
            }
            return getDefaultPublicKey(iss, kid)
        }catch (e: Exception){
            return getDefaultPublicKey(iss, kid)
        }
    }

    private fun getDefaultPublicKey(iss: String, kid: String): ECPublicKey? {
       val publicKey =  shcConfig.defaultKeys.filter { defaultJWKSKeys ->
            defaultJWKSKeys.iss == iss
        }.map { defaultJWKSKeys ->
            val keys = defaultJWKSKeys.jwksKeys.filter { jwksKey -> jwksKey.kid == kid }
                .map { jwksKey ->
                    derivePublicKey(jwksKey)
                }
            return@map keys.firstOrNull()
        }

        return publicKey.firstOrNull()
    }

    private fun derivePublicKey(jwksKey: JwksKey): ECPublicKey {
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
}