package ca.bc.gov.vaxcheck.ui.webview

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.databinding.FragmentWebViewBinding
import ca.bc.gov.vaxcheck.utils.Helper
import ca.bc.gov.vaxcheck.utils.viewBindings


/**
 * [WebViewFragment]
 *
 * @author amit metri
 */
class WebViewFragment : Fragment(R.layout.fragment_web_view) {

    private lateinit var url : String

    private val args: WebViewFragmentArgs by navArgs()

    private val binding by viewBindings(FragmentWebViewBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        url = args.url

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.webView.webViewClient = object :WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.swipeRefresh.isRefreshing  = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.swipeRefresh.isRefreshing  = false
            }
        }

        loadUrl()

        binding.swipeRefresh.setOnRefreshListener { loadUrl() }
    }

    private fun loadUrl(){
        if(Helper().isOnline(this.requireContext())) {
            binding.textView.visibility = View.INVISIBLE
            if (URLUtil.isHttpsUrl(url) && !URLUtil.isFileUrl(url))
                binding.webView.loadUrl(url)
        } else {
            binding.textView.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing  = false
        }
    }

    override fun onDestroyView() {
        binding.webView.stopLoading()
        super.onDestroyView()
    }
}