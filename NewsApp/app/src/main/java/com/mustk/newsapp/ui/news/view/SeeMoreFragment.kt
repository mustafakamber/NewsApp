package com.mustk.newsapp.ui.news.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mustk.newsapp.databinding.FragmentSeeMoreBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SeeMoreFragment @Inject constructor() : Fragment() {

    private lateinit var binding : FragmentSeeMoreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSeeMoreBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebViewScreen()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewScreen() = with(binding){
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

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()){
                    webView.goBack()
                }
                else {
                    findNavController().popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
    }
}