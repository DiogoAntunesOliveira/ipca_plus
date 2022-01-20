package com.singularity.ipcaplus.drawer

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.singularity.ipcaplus.R
import android.content.Intent
import android.widget.*
import com.singularity.ipcaplus.BuildConfig
import java.lang.Exception


class ShareWithFriendsActivity : AppCompatActivity() {
    private lateinit var link : String

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_with_friends)

        supportActionBar?.title = "Definições do grupo"
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_bar_layout)
        findViewById<TextView>(R.id.AppBarTittle).text = "Share With Friends"
        // Back button
        findViewById<ImageView>(R.id.BackButtonImageView).setOnClickListener{
            finish()
        }

        val link = "https://drive.google.com/file/d/17y5lG687V_rPokKd-cmu6VRL_H8MjIiH/view"

        findViewById<Button>(R.id.shareWithFriendsButton).setOnClickListener {

            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "IPCA+")
                var shareMessage = "\nApresento te a app do IPCA com WEB 3.0 anda descobrir!\n\n"
                shareMessage = """
                ${shareMessage}$link
                """.trimIndent()

                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
                e.toString()
            }
        }
        findViewById<ImageView>(R.id.shareWithFriendImageView).setOnClickListener {
            copyTextToClipboard()
        }

    }

    private fun copyTextToClipboard() {
        val textToCopy = "https://drive.google.com/file/d/17y5lG687V_rPokKd-cmu6VRL_H8MjIiH/view"
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
    }
}
