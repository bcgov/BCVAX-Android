package ca.bc.gov.vaxcheck.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.databinding.FragmentOnboardingBinding
import ca.bc.gov.vaxcheck.ui.scanner.BarcodeScannerFragment
import ca.bc.gov.vaxcheck.utils.viewBindings
import ca.bc.gov.vaxcheck.viewmodel.SharedViewModel

/**
 * [OnBoardingFragment]
 *
 * @author Amit Metri
 */
class OnBoardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val binding by viewBindings(FragmentOnboardingBinding::bind)

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
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
    }
}