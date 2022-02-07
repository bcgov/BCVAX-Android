package ca.bc.gov.shcdecoder.revocations.impl

import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.revocations.RevocationManager
import ca.bc.gov.shcdecoder.revocations.getRevocationsUrl
import java.util.Date

class RevocationManagerImpl(
    private val fileManager: FileManager
): RevocationManager {

    override suspend fun getRevocations(iss: String, kid: String): List<Pair<String, Date?>>{
        val url = getRevocationsUrl(iss, kid)
        return fileManager.getRevocations(url)
    }

}