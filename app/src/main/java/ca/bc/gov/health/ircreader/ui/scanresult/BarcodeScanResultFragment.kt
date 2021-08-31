package ca.bc.gov.health.ircreader.ui.scanresult

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import ca.bc.gov.health.ircreader.R
import ca.bc.gov.health.ircreader.databinding.FragmentBarcodeScanResultBinding
import ca.bc.gov.health.ircreader.utils.viewBindings
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
