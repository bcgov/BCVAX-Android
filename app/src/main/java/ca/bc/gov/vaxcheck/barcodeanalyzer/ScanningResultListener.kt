package ca.bc.gov.vaxcheck.barcodeanalyzer

/**
 * [ScanningResultListener]
 *
 * @author Pinakin Kansara
 */
interface ScanningResultListener {
    /**
     * Called upon successful barcode scan.
     *
     * @param shcUri String
     */
    fun onScanned(shcUri: String)

    /**
     * Called upon error in barcode scan.
     */
    fun onFailure()
}
