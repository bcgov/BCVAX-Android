package ca.bc.gov.shcdecoder

import ca.bc.gov.shcdecoder.model.SHCData
import ca.bc.gov.shcdecoder.model.VaccinationStatus

interface SHCVerifier {

    val config: SHCConfig

    suspend fun hasValidSignature(shcUri: String): Boolean

    suspend fun getStatus(shcUri: String): Pair<VaccinationStatus, SHCData>
}
