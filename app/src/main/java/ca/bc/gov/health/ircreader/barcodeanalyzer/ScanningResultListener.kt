package ca.bc.gov.health.ircreader.barcodeanalyzer

import ca.bc.gov.health.ircreader.model.SHCData

interface ScanningResultListener {
    fun onScanned(shcData: SHCData)
    fun onFailure()
}
