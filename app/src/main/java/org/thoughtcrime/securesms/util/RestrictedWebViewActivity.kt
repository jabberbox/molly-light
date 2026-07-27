package org.thoughtcrime.securesms.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class RestrictedWebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(EXTRA_URL) ?: run { finish(); return }

        toolbar = Toolbar(this).apply {
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            title = Uri.parse(url).host ?: ""
        }

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    return when (request.url.scheme?.lowercase()) {
                        "http", "https" -> {
                            startActivity(getIntent(this@RestrictedWebViewActivity, request.url.toString()))
                            true
                        }
                        "mailto" -> {
                            try { startActivity(Intent(Intent.ACTION_SENDTO, request.url)) } catch (_: Exception) {}
                            true
                        }
                        else -> false
                    }
                }

                override fun onPageFinished(view: WebView, url: String) {
                    toolbar.title = Uri.parse(url).host ?: ""
                }
            }
            loadUrl(url)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(toolbar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(webView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        }
        setContentView(layout)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    companion object {
        private const val EXTRA_URL = "url"

        @JvmStatic
        fun getIntent(context: Context, url: String): Intent =
            Intent(context, RestrictedWebViewActivity::class.java).putExtra(EXTRA_URL, url)
    }
}
