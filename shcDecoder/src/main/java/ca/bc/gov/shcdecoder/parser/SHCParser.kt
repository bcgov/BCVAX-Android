package ca.bc.gov.shcdecoder.parser

import ca.bc.gov.shcdecoder.model.SHCData

interface SHCParser {

    suspend fun parse(shcUri: String): SHCData

    suspend fun getSignedJWKSPayload(shcUri: String): String

    suspend fun getUnSignedJWKSPayload(shcUri: String): String

    suspend fun getJWKSignature(shcUri: String): String
}
