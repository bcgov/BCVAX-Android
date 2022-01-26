package ca.bc.gov.shcdecoder

import ca.bc.gov.shcdecoder.model.ImmunizationRecord
import ca.bc.gov.shcdecoder.model.SHCData
import ca.bc.gov.shcdecoder.model.VaccinationStatus

interface SHCVerifier {

    val config: SHCConfig

    suspend fun hasValidSignature(shcUri: String): Boolean

    @Deprecated(
        message = "This method will get removed in 2.0",
        replaceWith = ReplaceWith("getStatus(shcUri = )"),
        level = DeprecationLevel.ERROR
    )
    suspend fun getImmunizationRecord(shcUri: String): ImmunizationRecord

    suspend fun getStatus(shcUri: String): Pair<VaccinationStatus, SHCData>
}
