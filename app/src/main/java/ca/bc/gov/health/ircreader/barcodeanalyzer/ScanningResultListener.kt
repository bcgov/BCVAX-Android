package ca.bc.gov.health.ircreader.barcodeanalyzer

interface ScanningResultListener {
    fun onScanned(result: String)
    fun onFailure()
}
