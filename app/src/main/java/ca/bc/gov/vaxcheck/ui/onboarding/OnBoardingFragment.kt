package ca.bc.gov.vaxcheck.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.databinding.FragmentOnboardingBinding
import ca.bc.gov.vaxcheck.ui.scanner.BarcodeScannerFragment
import ca.bc.gov.vaxcheck.utils.setSpannableLink
import ca.bc.gov.vaxcheck.utils.viewBindings
import ca.bc.gov.vaxcheck.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * [OnBoardingFragment]
 *
 * @author Amit Metri
 */
@AndroidEntryPoint
class OnBoardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val binding by viewBindings(FragmentOnboardingBinding::bind)

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAllowCameraPermission.setOnClickListener {
            sharedViewModel.writeFirstLaunch(BarcodeScannerFragment.ON_BOARDING_SHOWN, true)
        }


        sharedViewModel.isOnBoardingShown(BarcodeScannerFragment.ON_BOARDING_SHOWN)
            .observe(viewLifecycleOwner, { isOnBoardingShown ->
                if (isOnBoardingShown) {
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.onBoardingFragment, true)
                        .build()
                    findNavController().navigate(R.id.barcodeScannerFragment, null, navOptions)
                }
            })

        binding.txtPrivacyPolicy.setSpannableLink {
            val uri = Uri.parse("https://www2.gov.bc.ca/gov/content/covid-19/vaccine/proof/businesses#app-privacy-policy")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            requireContext().startActivity(intent)
        }
    }
}