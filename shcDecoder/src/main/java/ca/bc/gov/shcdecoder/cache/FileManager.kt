package ca.bc.gov.shcdecoder.cache

import ca.bc.gov.shcdecoder.model.Issuer
import ca.bc.gov.shcdecoder.model.JwksKey
import ca.bc.gov.shcdecoder.model.Rule

interface FileManager {

    suspend fun downloadFile(url: String)

    suspend fun getIssuers(url: String): List<Issuer>

    suspend fun getKeys(url: String): List<JwksKey>

    suspend fun getRule(url: String): List<Rule>
}
