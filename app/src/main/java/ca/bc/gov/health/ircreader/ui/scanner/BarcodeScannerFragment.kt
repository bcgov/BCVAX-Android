package ca.bc.gov.health.ircreader.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ca.bc.gov.health.ircreader.R
import ca.bc.gov.health.ircreader.barcodeanalyzer.BarcodeAnalyzer
import ca.bc.gov.health.ircreader.barcodeanalyzer.ScanningResultListener
import ca.bc.gov.health.ircreader.databinding.FragmentBarcodeScannerBinding
import ca.bc.gov.health.ircreader.utils.viewBindings
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * [BarcodeScannerFragment]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class BarcodeScannerFragment : Fragment(R.layout.fragment_barcode_scanner), ScanningResultListener {

    private val binding by viewBindings(FragmentBarcodeScannerBinding::bind)

    private lateinit var cameraProviderFeature: ListenableFuture<ProcessCameraProvider>

    private lateinit var cameraExecutor: ExecutorService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProviderFeature = ProcessCameraProvider.getInstance(requireContext())

        cameraExecutor = Executors.newSingleThreadExecutor()

        checkCameraPermission()

        binding.overlay.post {
            binding.overlay.setViewFinder()
        }

        isRedirectionEnabled = true
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
                // TODO: Permission not Granted close the app
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

                // TODO: Show educational screen.
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

            val preview = getPreview()

            val imageAnalysis = getImageAnalyzer()

            imageAnalysis.setAnalyzer(cameraExecutor, BarcodeAnalyzer(this))

            cameraProvider.unbindAll()

            val camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis
            )

            setFlash(camera)

            preview.setSurfaceProvider(binding.scannerPreview.surfaceProvider)
        }

        private fun setFlash(camera: Camera) {
            if (camera.cameraInfo.hasFlashUnit()) {
                binding.checkboxFlashLight.visibility = View.VISIBLE

                binding.checkboxFlashLight.setOnCheckedChangeListener { buttonView, isChecked ->

                    if (buttonView.isPressed) {
                        camera.cameraControl.enableTorch(isChecked)
                    }
                }

                camera.cameraInfo.torchState.observe(viewLifecycleOwner) {
                    it?.let { torchState ->
                        binding.checkboxFlashLight.isChecked = torchState == TorchState.ON
                    }
                }
            }
        }

        private fun getImageAnalyzer() = ImageAnalysis.Builder()
            .setTargetResolution(Size(binding.scannerPreview.width, binding.scannerPreview.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        private fun getPreview() = Preview.Builder().apply {
            setTargetResolution(Size(binding.scannerPreview.width, binding.scannerPreview.height))
        }.build()

        override fun onScanned(result: String) {
            val action =
                BarcodeScannerFragmentDirections
                    .actionBarcodeScannerFragmentToBarcodeScanResultFragment(
                        result
                    )
            findNavController().navigate(action)
        }

        override fun onFailure() {
            TODO("Not yet implemented")
        }
    }
