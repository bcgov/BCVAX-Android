package ca.bc.gov.health.ircreader.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ca.bc.gov.health.ircreader.R
import ca.bc.gov.health.ircreader.barcodeanalyzer.BarcodeAnalyzer
import ca.bc.gov.health.ircreader.barcodeanalyzer.ScanningResultListener
import ca.bc.gov.health.ircreader.utils.SHCDecoder
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors


/**
 * [BarcodeScannerFragment]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class BarcodeScannerFragment : Fragment(R.layout.fragment_barcode_scanner) {

    private lateinit var cameraProviderFeature: ListenableFuture<ProcessCameraProvider>

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private lateinit var scannerPreview: PreviewView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scannerPreview = view.findViewById(R.id.scanner_preview)
        cameraProviderFeature = ProcessCameraProvider.getInstance(requireContext())

        checkCameraPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }


    /**
     * Check if permission for required feature is Granted or not.
     */
    private fun checkCameraPermission() {

        val requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->

            if (isGranted) {
                cameraProvider()
            } else {
                //TODO: Permission not Granted close the app
            }

        }

        when {

            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                cameraProvider()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {

                //TODO: Show educational screen.
            }

            else -> {
                requestPermission.launch(Manifest.permission.CAMERA)
            }
        }

    }

    private fun cameraProvider() {
        cameraProviderFeature.addListener({
            val cameraProvider = cameraProviderFeature.get()
            startCamera(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startCamera(cameraProvider: ProcessCameraProvider) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview = Preview.Builder().apply {
            setTargetResolution(Size(scannerPreview.width, scannerPreview.height))
        }.build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(scannerPreview.width, scannerPreview.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(
            cameraExecutor,
            BarcodeAnalyzer(object : ScanningResultListener {
                override fun onScanned(result: String) {
                    SHCDecoder().decode(result, onSuccess = {
                        println("SHC ${it.payload}")
                    }, onError = {
                        println("SHC ${it.message}")
                    })
                }

                override fun onFailure() {

                }
            })
        )

        cameraProvider.unbindAll()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        preview.setSurfaceProvider(scannerPreview.surfaceProvider)


    }
}