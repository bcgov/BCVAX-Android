package ca.bc.gov.vaxcheck.barcodeanalyzer

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * [BarcodeAnalyzer]
 *
 * @author Pinakin Kansara
 */
class BarcodeAnalyzer(private val listener: ScanningResultListener) : ImageAnalysis.Analyzer {

    private var isScanning: Boolean = false

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && !isScanning) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            val scanner = BarcodeScanning.getClient()

            isScanning = true
            scanner.process(image)
                .addOnSuccessListener { barcodes ->

                    barcodes.firstOrNull().let { barcode ->
                        if (barcode != null && barcode.format != Barcode.FORMAT_QR_CODE) {
                            listener.onFailure()
                            isScanning = false
                            return@let
                        }
                        val rawValue = barcode?.rawValue
                        rawValue?.let {
                            listener.onScanned(it)
                            isScanning = false
                        }
                    }
                    isScanning = false
                }
                .addOnFailureListener {
                    listener.onFailure()
                    isScanning = false
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
