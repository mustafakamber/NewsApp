package com.mustk.newsapp.ui.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.mustk.newsapp.databinding.FragmentSeeMoreBinding
import com.mustk.newsapp.util.onBackPressed
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SeeMoreFragment @Inject constructor() : Fragment() {

    private lateinit var binding : FragmentSeeMoreBinding
    @Inject lateinit var adRequest: AdRequest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSeeMoreBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSeeMoreScreen()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupSeeMoreScreen() = with(binding){
        seeMoreAdView.loadAd(adRequest)
        arguments?.let { bundle ->
            val args = SeeMoreFragmentArgs.fromBundle(bundle)
            val newsURL = args.newsURL
            with(webView.settings) {
                javaScriptEnabled = true
                setSupportZoom(true)
            }
            webView.apply {
                loadUrl(newsURL)
                webViewClient = WebViewClient()
            }
        }
        onBackPressed {
            if (webView.canGoBack()){
                webView.goBack()
            }
            else {
                findNavController().popBackStack()
            }
        }
    }
}