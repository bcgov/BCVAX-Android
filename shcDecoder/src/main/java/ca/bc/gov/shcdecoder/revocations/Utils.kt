package ca.bc.gov.shcdecoder.revocations

import ca.bc.gov.shcdecoder.cache.impl.CacheManagerImpl

private const val REVOCATION_JSON_PATH = "/.well-known/crl/"

fun getRevocationsUrl(iss: String, kid: String): String {
    return iss.removeSuffix(CacheManagerImpl.SUFFIX_ISSUER_JSON).let { formattedIss ->
        "$formattedIss${REVOCATION_JSON_PATH}$kid.json"
    }
}