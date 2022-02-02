package ca.bc.gov.vaxcheck.ui.scanresult

import android.os.Bundle
import android.os.CountDownTimer
import android.transition.Scene
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ca.bc.gov.shcdecoder.model.VaccinationStatus
import ca.bc.gov.shcdecoder.model.getPatient
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.databinding.FragmentBarcodeScanResultBinding
import ca.bc.gov.vaxcheck.utils.viewBindings
import ca.bc.gov.vaxcheck.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * [BarcodeScanResultFragment]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class BarcodeScanResultFragment : Fragment(R.layout.fragment_barcode_scan_result) {

    private val binding by viewBindings(FragmentBarcodeScanResultBinding::bind)

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var sceneFullyVaccinated: Scene

    private lateinit var scenePartiallyVaccinated: Scene

    private lateinit var sceneNoRecord: Scene

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

        sharedViewModel.status.observe(viewLifecycleOwner) { status ->
            val (state, shcData) = status
            if (shcData != null) {
                val patient = shcData.getPatient()
                val fullNameBuilder = StringBuilder()
                if (patient.firstName != null) {
                    fullNameBuilder.append(patient.firstName)
                    fullNameBuilder.append(" ")
                }
                if (patient.lastName != null) {
                    fullNameBuilder.append(patient.lastName)
                }
                binding.txtFullName.text = fullNameBuilder.toString()
                when (state) {
                    VaccinationStatus.FULLY_VACCINATED -> {
                        sceneFullyVaccinated.enter()
                        sceneFullyVaccinated.sceneRoot.findViewById<View>(R.id.buttonScanNext)
                            .setOnClickListener {
                                findNavController().popBackStack()
                            }
                    }
                    VaccinationStatus.PARTIALLY_VACCINATED -> {
                        scenePartiallyVaccinated.enter()
                        scenePartiallyVaccinated.sceneRoot.findViewById<View>(R.id.buttonScanNext)
                            .setOnClickListener {
                                findNavController().popBackStack()
                            }
                    }
                    else -> {
                        sceneNoRecord.enter()
                        sceneNoRecord.sceneRoot.findViewById<View>(R.id.buttonScanNext)
                            .setOnClickListener {
                                findNavController().popBackStack()
                            }
                    }
                }
            }

            val countDownTimer = object : CountDownTimer(10000, 1000) {

                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    lifecycleScope.launchWhenResumed {
                        findNavController().popBackStack()
                    }
                }
            }
            countDownTimer.start()
        }
    }
}
