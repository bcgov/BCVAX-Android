package ca.bc.gov.vaxcheck.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.barcodeanalyzer.BarcodeAnalyzer
import ca.bc.gov.vaxcheck.barcodeanalyzer.ScanningResultListener
import ca.bc.gov.vaxcheck.databinding.FragmentBarcodeScannerBinding
import ca.bc.gov.vaxcheck.utils.viewBindings
import ca.bc.gov.vaxcheck.viewmodel.SharedViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var requestPermission: ActivityResultLauncher<String>

    private lateinit var cameraProvider: ProcessCameraProvider

    private lateinit var imageAnalysis: ImageAnalysis

    private lateinit var camera: Camera

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->

            if (isGranted) {
                setUpCamera()
            } else {
                //TODO: Add Optional screen that explain why your app require permission
                //For now closing the app
                findNavController().popBackStack()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.isOnBoardingShown(ON_BOARDING_SHOWN)
            .observe(viewLifecycleOwner, { isOnBoardingShown ->
                if (!isOnBoardingShown) {
                    val startDestination = findNavController().graph.startDestination
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    findNavController().navigate(R.id.onBoardingFragment, null, navOptions)
                } else {
                    cameraExecutor = Executors.newSingleThreadExecutor()

                    checkCameraPermission()

                    binding.overlay.post {
                        binding.overlay.setViewFinder()
                    }
                }
            })
    }

    override fun onDestroyView() {

        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }

        super.onDestroyView()
    }

    /**
     * Check if permission for required feature is Granted or not.
     */
    private fun checkCameraPermission() {

        when {

            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                setUpCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRationalDialog()
            }

            else -> {
                requestPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showRationalDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.bc_permission_required_title))
            .setMessage(getString(R.string.bc_permission_message))
            .setNegativeButton(getString(R.string.exit)) { dialog, which ->
                findNavController().popBackStack()
                dialog.dismiss()
            }
            .show()
    }

    private fun setUpCamera() {

        val cameraProviderFeature = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFeature.addListener({

            cameraProvider = cameraProviderFeature.get()

            bindBarcodeScannerUseCase()

            enableFlashControl()

        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private fun bindBarcodeScannerUseCase() {

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val resolution = Size(
            binding.scannerPreview.width,
            binding.scannerPreview.height
        )
        val preview = Preview.Builder()
            .apply {
                setTargetResolution(resolution)
            }.build()

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(resolution)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor, BarcodeAnalyzer(this))


        cameraProvider.unbindAll()

        camera = cameraProvider.bindToLifecycle(
            viewLifecycleOwner, cameraSelector, preview, imageAnalysis
        )

        preview.setSurfaceProvider(binding.scannerPreview.surfaceProvider)

    }

    private fun enableFlashControl() {
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

    override fun onScanned(shcUri: String) {

        //Since camera is constantly analysing
        //Its good to clear analyzer to avoid duplicate dialogs
        //When barcode is not supported
        imageAnalysis.clearAnalyzer()

        val action = BarcodeScannerFragmentDirections
            .actionBarcodeScannerFragmentToBarcodeScanResultFragment(shcUri)

        findNavController().navigate(action)

    }

    override fun onFailure() {

        //Since camera is constantly analysing
        //Its good to clear analyzer to avoid duplicate dialogs
        //When barcode is not supported
        imageAnalysis.clearAnalyzer()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.bc_invalid_barcode_title))
            .setMessage(getString(R.string.bc_invalid_barcode_message))
            .setPositiveButton(getString(R.string.scan_again)) { dialog, which ->

                //Attach analyzer again to start analysis.
                imageAnalysis.setAnalyzer(cameraExecutor, BarcodeAnalyzer(this))

                dialog.dismiss()
            }
            .show()
    }

    companion object {
        const val ON_BOARDING_SHOWN = "ON_BOARDING_SHOWN"
    }
}
