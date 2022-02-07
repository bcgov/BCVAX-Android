package ca.bc.gov.shcdecoder.revocations

import java.util.Date

interface RevocationManager {
    suspend fun getRevocations(iss: String, kid: String): List<Pair<String, Date?>>
}