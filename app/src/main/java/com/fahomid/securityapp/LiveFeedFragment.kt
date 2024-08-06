package com.fahomid.securityapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

// Fragment to display the live video feed
class LiveFeedFragment : Fragment() {

    private lateinit var webView: WebView  // WebView to display the live feed

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_live_feed, container, false)
        webView = view.findViewById(R.id.web_view)

        // Retrieve the video URL from the activity
        val videoUrl = (activity as DeviceDetailActivity).getVideoFeed()

        // Initialize WebView if video URL is available
        if (videoUrl != null) {
            initializeWebView(videoUrl)
        }

        return view
    }

    // Initialize the WebView settings and load the video URL
    private fun initializeWebView(videoUrl: String) {
        webView.webViewClient = WebViewClient()
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = false
        webSettings.displayZoomControls = false

        // Load the video URL
        webView.loadUrl(videoUrl)
    }

    // Clean up WebView resources when the view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        webView.destroy()
    }
}
