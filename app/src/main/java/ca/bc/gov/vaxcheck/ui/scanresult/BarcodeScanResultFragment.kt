package ca.bc.gov.vaxcheck.ui.scanresult

import android.os.Bundle
import android.os.CountDownTimer
import android.transition.Scene
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.databinding.FragmentBarcodeScanResultBinding
import ca.bc.gov.vaxcheck.utils.PayLoadProcessor
import ca.bc.gov.vaxcheck.utils.viewBindings
import ca.bc.gov.vaxcheck.viewmodel.BarcodeScanResultViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * [BarcodeScanResultFragment]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class BarcodeScanResultFragment : Fragment(R.layout.fragment_barcode_scan_result) {

    private val binding by viewBindings(FragmentBarcodeScanResultBinding::bind)

    private val args: BarcodeScanResultFragmentArgs by navArgs()
    private val viewModel: BarcodeScanResultViewModel by viewModels()

    private lateinit var sceneFullyVaccinated: Scene
    private lateinit var scenePartiallyVaccinated: Scene
    private lateinit var sceneNoRecord: Scene

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.shcData?.let { viewModel.processShcUri(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sceneFullyVaccinated = Scene.getSceneForLayout(
            binding.sceneRoot,
            R.layout.scene_fully_vaccinated,
            requireContext()
        )
        scenePartiallyVaccinated = Scene.getSceneForLayout(
            binding.sceneRoot,
            R.layout.scene_partially_vaccinated,
            requireContext()
        )
        sceneNoRecord =
            Scene.getSceneForLayout(binding.sceneRoot, R.layout.scene_no_record, requireContext())

        viewModel.observeUserName().observe(viewLifecycleOwner, {
            binding.textViewName.text = it
        })

        viewModel.observeVaccinationStatus().observe(viewLifecycleOwner, { immuStatus ->
            when (immuStatus!!) {
                PayLoadProcessor.ImmuStatus.NO_RECORD -> {
                    sceneNoRecord.enter()
                }
                PayLoadProcessor.ImmuStatus.PARTIALLY_IMMUNIZED -> {
                    scenePartiallyVaccinated.enter()
                }
                PayLoadProcessor.ImmuStatus.FULLY_IMMUNIZED -> {
                    sceneFullyVaccinated.enter()
                }
            }

            /*
            * To remove status screen after 10 seconds
            * */
            val countDownTimer = object : CountDownTimer(10000, 1000) {

                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    lifecycleScope.launchWhenResumed {
                        findNavController().popBackStack()
                    }
                }
            }
            countDownTimer.start()

        })

        binding.buttonScanAgain.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
