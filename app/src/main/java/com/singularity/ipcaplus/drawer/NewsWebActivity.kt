package com.singularity.ipcaplus.drawer

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.singularity.ipcaplus.R

class NewsWebActivity : AppCompatActivity() {
    private lateinit var webView : WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_web)

        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stay)

        webView = findViewById(R.id.webview)
        var progDailog = ProgressDialog.show(this, "A carregar","Espera um bocado...", true);
        progDailog.setCancelable(false);
        webView.settings.setJavaScriptEnabled(true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let { view?.loadUrl(it) }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progDailog.dismiss();
            }
        }
        progDailog.show();
        webView.loadUrl("https://www.sapo.pt/")
    }


}