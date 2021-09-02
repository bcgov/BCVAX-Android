package ca.bc.gov.vaxcheck.barcodeanalyzer

import ca.bc.gov.vaxcheck.model.SHCData

interface ScanningResultListener {
    fun onScanned(shcData: SHCData)
    fun onFailure()
}
