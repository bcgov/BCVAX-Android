package ca.bc.gov.health.ircreader.ui.scanner

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ca.bc.gov.health.ircreader.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * [BarcodeScannerFragment]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class BarcodeScannerFragment : Fragment(R.layout.fragment_barcode_scanner) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}