package ca.bc.gov.shcdecoder

import ca.bc.gov.shcdecoder.model.ImmunizationRecord

interface SHCVerifier {

    val config: SHCConfig

    suspend fun hasValidSignature(shcUri: String): Boolean

    suspend fun getImmunizationRecord(shcUri: String): ImmunizationRecord
}
