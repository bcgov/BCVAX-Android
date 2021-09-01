package ca.bc.gov.health.ircreader.ui.scanresult

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ca.bc.gov.health.ircreader.R
import ca.bc.gov.health.ircreader.databinding.FragmentBarcodeScanResultBinding
import ca.bc.gov.health.ircreader.utils.viewBindings
import ca.bc.gov.health.ircreader.viewmodel.BarcodeScanResultViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * [BarcodeScanResultFragment]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class BarcodeScanResultFragment : Fragment(R.layout.fragment_barcode_scan_result) {

    private val binding by viewBindings(FragmentBarcodeScanResultBinding::bind)

    /*
    * Both of these variable control the UI for status screen
    * vaccinationStatus = 0 is "No records found"
    * vaccinationStatus = 1 is "Partially vaccinated"
    * vaccinationStatus = 2 is "Fully vaccinated"
    * */
    private var userName: String = ""
    private var vaccinationStatus: Int = 0

    private val args: BarcodeScanResultFragmentArgs by navArgs()
    private val viewModel: BarcodeScanResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.shcData?.let { viewModel.processShcUri(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeUserName().observe(viewLifecycleOwner, {
            binding.textViewName.text = it
        })

        /*
        * Both of these variable control the UI for status screen
        * vaccinationStatus = 0 is "No records found"
        * vaccinationStatus = 1 is "Partially vaccinated"
        * vaccinationStatus = 2 is "Fully vaccinated"
        * */
        viewModel.observeVaccinationStatus().observe(viewLifecycleOwner, {
            when (it) {
                0 -> {
                    binding.textViewName.text = getString(R.string.no_record_found)
                    binding.viewStatusColor.setBackgroundColor(
                        resources.getColor(
                            R.color.grey,
                            null
                        )
                    )
                    binding.viewLineLeft.visibility = View.INVISIBLE
                    binding.viewLineRight.visibility = View.INVISIBLE
                    binding.viewLineTop.visibility = View.INVISIBLE
                    binding.viewLineBottom.visibility = View.INVISIBLE
                    binding.textViewBCLabel.visibility = View.INVISIBLE
                }
                1 -> {
                    binding.textViewResult.text = getString(R.string.partially_vaccinated)
                    binding.viewStatusColor.setBackgroundColor(
                        resources.getColor(
                            R.color.blue,
                            null
                        )
                    )
                }
                2 -> {
                    binding.imageViewRightTick.visibility = View.VISIBLE
                    binding.textViewResult.text = getString(R.string.vaccinated)
                    binding.viewStatusColor.setBackgroundColor(
                        resources.getColor(
                            R.color.green,
                            null
                        )
                    )
                }
            }
        })

        binding.imageViewClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonScanAgain.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
