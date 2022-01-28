package ca.bc.gov.vaxcheck.ui.scanner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import ca.bc.gov.shcdecoder.model.VaccinationStatus
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.barcodeanalyzer.BarcodeAnalyzer
import ca.bc.gov.vaxcheck.barcodeanalyzer.ScanningResultListener
import ca.bc.gov.vaxcheck.databinding.FragmentBarcodeScannerBinding
import ca.bc.gov.vaxcheck.utils.setSpannableLink
import ca.bc.gov.vaxcheck.utils.toast
import ca.bc.gov.vaxcheck.utils.viewBindings
import ca.bc.gov.vaxcheck.viewmodel.BarcodeScanResultViewModel
import ca.bc.gov.vaxcheck.viewmodel.SharedViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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

    private val viewModel: BarcodeScanResultViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->

            if (isGranted) {
                setUpCamera()
            } else {
                showRationalDialog()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    collectOnBoardingFlow()
                }
                launch {
                    collectImmunizationStatus()
                }
            }
        }

        binding.txtPrivacyPolicy.setSpannableLink {
            showPrivacyPolicy()
        }
    }

    private fun showPrivacyPolicy() {
        val webpage: Uri = Uri.parse(getString(R.string.url_privacy_policy))
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            context?.toast(getString(R.string.no_app_found))
        }
    }

    private suspend fun collectOnBoardingFlow() {
        sharedViewModel.isOnBoardingShown.collect { shown ->
            when (shown) {
                true -> {
                    cameraExecutor = Executors.newSingleThreadExecutor()

                    checkCameraPermission()

                    binding.overlay.post {
                        binding.overlay.setViewFinder()
                    }
                }

                false -> {
                    val startDestination = findNavController().graph.startDestination
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    findNavController().navigate(R.id.onBoardingFragment, null, navOptions)
                }
            }
        }
    }

    private suspend fun collectImmunizationStatus() {
        viewModel.status.collect { status ->
            val (state, data) = status
            when (state) {
                VaccinationStatus.FULLY_VACCINATED,
                VaccinationStatus.PARTIALLY_VACCINATED -> {
                    sharedViewModel.setStatus(status)
                    findNavController().navigate(
                        R.id.action_barcodeScannerFragment_to_barcodeScanResultFragment
                    )
                }

                VaccinationStatus.NOT_VACCINATED,
                VaccinationStatus.INVALID -> {
                    onFailure()
                }
            }
        }
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
            .setCancelable(false)
            .setMessage(getString(R.string.bc_permission_message))
            .setNegativeButton(getString(R.string.exit)) { dialog, which ->
                if (!findNavController().popBackStack() || !findNavController().navigateUp()) {
                    requireActivity().finish()
                }
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

            val hasCamera = cameraProvider.hasCamera(cameraSelector)

            if (hasCamera) {

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
            } else {
                showNoCameraAlertDialog()
            }
        }

        private fun showNoCameraAlertDialog() {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.bc_no_rear_camera_title))
                .setCancelable(false)
                .setMessage(getString(R.string.bc_nor_rear_camera_message))
                .setNegativeButton(getString(R.string.exit)) { dialog, which ->
                    if (!findNavController().popBackStack() || !findNavController().navigateUp()) {
                        requireActivity().finish()
                    }
                    dialog.dismiss()
                }
                .show()
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

            // Since camera is constantly analysing
            // Its good to clear analyzer to avoid duplicate dialogs
            // When barcode is not supported
            imageAnalysis.clearAnalyzer()

            viewModel.processShcUri(shcUri)
        }

        override fun onFailure() {

            // Since camera is constantly analysing
            // Its good to clear analyzer to avoid duplicate dialogs
            // When barcode is not supported
            imageAnalysis.clearAnalyzer()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.bc_invalid_barcode_title))
                .setCancelable(false)
                .setMessage(getString(R.string.bc_invalid_barcode_message))
                .setPositiveButton(getString(R.string.scan_next)) { dialog, which ->

                    // Attach analyzer again to start analysis.
                    imageAnalysis.setAnalyzer(cameraExecutor, BarcodeAnalyzer(this))

                    dialog.dismiss()
                }
                .show()
        }

        companion object {
            const val ON_BOARDING_SHOWN = "ON_BOARDING_SHOWN"
        }
    }
