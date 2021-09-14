package ca.bc.gov.vaxcheck.ui.onboarding


import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.databinding.FragmentOnboardingBinding
import ca.bc.gov.vaxcheck.utils.setSpannableLink
import ca.bc.gov.vaxcheck.utils.viewBindings
import ca.bc.gov.vaxcheck.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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
            sharedViewModel.setOnBoardingShown(true)
        }

        viewLifecycleOwner.lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.isOnBoardingShown.collect { shown ->
                    when (shown) {
                        true -> {
                            val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.onBoardingFragment, true)
                                .build()
                            findNavController().navigate(
                                R.id.barcodeScannerFragment,
                                null,
                                navOptions
                            )
                        }
                    }
                }

            }

        }

        binding.txtPrivacyPolicy.setSpannableLink {
            val uri =
                Uri.parse(getString(R.string.url_privacy_policy))
            val action = OnBoardingFragmentDirections
                .actionOnBoardingFragmentToWebViewFragment(uri.toString())
            findNavController().navigate(action)
        }
    }
}