package ca.bc.gov.health.ircreader.barcodeanalyzer

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * [BarcodeAnalyzer]
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
                        val rawValue = barcode?.rawValue
                        rawValue?.let {
                            Log.d("Barcode", it)
                            listener.onScanned(it)
                        }
                    }

                    isScanning = false
                    imageProxy.close()
                }
                .addOnFailureListener {

                    isScanning = false
                    imageProxy.close()
                    listener.onFailure()
                }
        }
    }
}
