package ca.bc.gov.health.ircreader.ui.scanresult

import android.graphics.Color
import android.os.Binder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import ca.bc.gov.health.ircreader.R

/**
 * [BarcodeScanResultFragment]
 *
 * @author Pinakin Kansara
 */

class BarcodeScanResultFragment : Fragment(R.layout.fragment_barcode_scan_result) {

    /*
    * Both of these variable control the UI for status screen
    * vaccinationStatus = 0 is "No records found"
    * vaccinationStatus = 1 is "Partially vaccinated"
    * vaccinationStatus = 2 is "Fully vaccinated"
    * */
    private var userName : String = ""
    private var vaccinationStatus : Int = 0

    // TODO: 31/08/21 For some reason View Binding is getting detected, using findViewByID for the time being
    private lateinit var textViewUserName: TextView
    private lateinit var textViewResult: TextView
    private lateinit var viewStatusColor: View
    private lateinit var imageViewRightTick: ImageView
    private lateinit var imageViewClose: ImageView
    private lateinit var viewLineLeft: View
    private lateinit var viewLineRight: View
    private lateinit var viewLineTop: View
    private lateinit var viewLineBottom: View
    private lateinit var buttonScanAgain: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: 31/08/21 SafeArgs will be used here
        userName = arguments?.get("userName") as String
        vaccinationStatus = arguments?.getInt("vaccinationStatus")!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewUserName = view.findViewById(R.id.textViewName)
        textViewResult = view.findViewById(R.id.textViewResult)
        viewStatusColor = view.findViewById(R.id.viewStatusColor)
        imageViewRightTick = view.findViewById(R.id.imageViewRightTick)
        imageViewClose = view.findViewById(R.id.imageViewClose)
        viewLineLeft = view.findViewById(R.id.viewLineLeft)
        viewLineRight = view.findViewById(R.id.viewLineRight)
        viewLineTop = view.findViewById(R.id.viewLineTop)
        viewLineBottom = view.findViewById(R.id.viewLineBottom)
        buttonScanAgain = view.findViewById(R.id.buttonScanAgain)



        textViewUserName.text = userName

        when (vaccinationStatus){
            0 -> {
                textViewResult.text = getString(R.string.no_record_found)
                viewStatusColor.setBackgroundColor(resources.getColor(R.color.grey, null))
                viewLineLeft.visibility = View.INVISIBLE
                viewLineRight.visibility = View.INVISIBLE
                viewLineTop.visibility = View.INVISIBLE
                viewLineBottom.visibility = View.INVISIBLE
            }
            1 -> {
                textViewResult.text = getString(R.string.partially_vaccinated)
                viewStatusColor.setBackgroundColor(resources.getColor(R.color.blue, null))
            }
            2 -> {
                imageViewRightTick.visibility = View.VISIBLE
                textViewResult.text = getString(R.string.vaccinated)
                viewStatusColor.setBackgroundColor(resources.getColor(R.color.green, null))
            }
        }

        imageViewClose.setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }

        buttonScanAgain.setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }
    }
}