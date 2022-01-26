package ca.bc.gov.shcdecoder.key

import java.security.interfaces.ECPublicKey

interface KeyManager {

    suspend fun getPublicKey(iss: String, kid: String): ECPublicKey?
}
