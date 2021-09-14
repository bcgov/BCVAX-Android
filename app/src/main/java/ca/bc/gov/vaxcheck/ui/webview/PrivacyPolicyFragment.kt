package ca.bc.gov.vaxcheck.ui.webview

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.databinding.FragmentPrivacyPolicyBinding
import ca.bc.gov.vaxcheck.utils.Helper
import ca.bc.gov.vaxcheck.utils.viewBindings


/**
 * [PrivacyPolicyFragment]
 *
 * @author amit metri
 */
class PrivacyPolicyFragment : Fragment(R.layout.fragment_privacy_policy) {

    private lateinit var url: String

    private val binding by viewBindings(FragmentPrivacyPolicyBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        url = resources.getString(R.string.url_privacy_policy)

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.swipeRefresh.isRefreshing = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.swipeRefresh.isRefreshing = false
            }
        }

        loadUrl()

        binding.swipeRefresh.setOnRefreshListener { loadUrl() }
    }

    private fun loadUrl() {
        if (Helper().isOnline(this.requireContext())) {
            binding.textView.visibility = View.INVISIBLE
            if (URLUtil.isHttpsUrl(url) && !URLUtil.isFileUrl(url))
                binding.webView.loadUrl(url)
        } else {
            binding.textView.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        binding.webView.stopLoading()
        super.onDestroyView()
    }
}