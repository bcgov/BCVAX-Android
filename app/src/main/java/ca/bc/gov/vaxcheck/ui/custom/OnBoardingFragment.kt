package ca.bc.gov.vaxcheck.ui.custom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.databinding.FragmentBarcodeScannerBinding
import ca.bc.gov.vaxcheck.databinding.FragmentOnboardingBinding
import ca.bc.gov.vaxcheck.utils.viewBindings

/**
 * A simple [Fragment] subclass.
 * Use the [OnBoardingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnBoardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val binding by viewBindings(FragmentOnboardingBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            findNavController().navigate(R.id.barcodeScannerFragment)
        }
    }
}