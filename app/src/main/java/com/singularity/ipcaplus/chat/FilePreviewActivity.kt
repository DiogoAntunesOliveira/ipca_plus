package com.singularity.ipcaplus.chat

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.singularity.ipcaplus.AddPeopleActivity
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.utils.Utilis
import java.util.regex.Pattern

class FilePreviewActivity : AppCompatActivity() {

    var fileName = ""
    var currentPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_preview)

        // Get data
        val url = intent.getStringExtra("url")
        fileName = intent.getStringExtra("fileName").toString()
        currentPath = intent.getStringExtra("currentPath").toString()

        // Setup WebView
        val webView = findViewById<WebView>(R.id.webview)
        var progDailog = ProgressDialog.show(this, "A carregar", "Espera um bocado...", true);
        progDailog.setCancelable(false);
        webView.settings.javaScriptEnabled = true

        // Load WebView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let { view?.loadUrl(it) }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progDailog.dismiss()
            }
        }
        progDailog.show()
        webView.loadUrl(url!!)

    }


    fun downloadFileRequest(path: String, name: String) {

        confirmationDialog("Transferir Ficheiro",
            "Tens certeza que queres transferir este ficheiro?") {

            val fileRef = Firebase.storage.reference.child("$path/${name}")
            val strArray = Pattern.compile("[.]").split(name)
            val fileName = strArray[0]
            val fileExtension = strArray[strArray.size - 1]

            fileRef.downloadUrl.addOnSuccessListener {
                Utilis.downloadFile(this, fileName, ".$fileExtension",
                    Environment.DIRECTORY_DOWNLOADS, it)
            }
        }
    }


    private fun confirmationDialog(title: String, description: String, callBack: () -> Unit) {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle(title)
        alertDialog.setMessage(description)

        alertDialog.setPositiveButton("Sim") { _, _ ->
            callBack()
        }

        alertDialog.setNegativeButton("Não") { _, _ ->
            alertDialog.show().dismiss()
        }

        alertDialog.show()
    }


    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    /*
        This function create the action bar above the activity
    */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_file, menu)

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = fileName

        return true
    }


    /*
        This function define the events of the action bar buttons
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.download -> {

                downloadFileRequest(currentPath, fileName)

                return true
            }
        }
        return false
    }

}