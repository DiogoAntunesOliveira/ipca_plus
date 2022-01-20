package com.singularity.ipcaplus.chat

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.QRCodeWriter
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.cryptography.getMetaOx
import com.singularity.ipcaplus.databinding.ActivityDrawerActivtyBinding
import com.singularity.ipcaplus.databinding.ActivityRegisterBinding
import com.singularity.ipcaplus.databinding.ActivityVerifySecurityNumberBinding
import com.singularity.ipcaplus.utils.Backend.put0xBlank
import com.singularity.ipcaplus.utils.Backend.put0xBlankProfile
import kotlinx.coroutines.*

class VerifySecurityNumberActivity : AppCompatActivity() {

    private lateinit var vQRCode: ImageView
    private lateinit var keygenHash: String
    private lateinit var verifySecurity: Button
    private lateinit var hexaHashKey: TextView
    private lateinit var binding: ActivityVerifySecurityNumberBinding


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_security_number)

        binding = ActivityVerifySecurityNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Custom action bar
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_bar_layout)
        findViewById<TextView>(R.id.AppBarTittle).text = "Verificação de Segurança"
        // Back button
        findViewById<ImageView>(R.id.BackButtonImageView).setOnClickListener {
            finish()
        }


        // Get previous data
        val chat_id = intent.getStringExtra("chat_id").toString()
        hexaHashKey = findViewById(R.id.hexaHashTextView)

        //keygenHash = binding.verifySecurity.toString()
        vQRCode = findViewById(R.id.qrImageView)

        //keygenHash = "662ede816988e58fb6d057d9d85605e0"
        val keyCall = getMetaOx(this, chat_id)

        // Customize Ox key on TextView
        val keyCallSpace = blankSpaces(keyCall.toString())
        keygenHash = keyCall.toString()
        val keygenData = keygenHash.trim()
        hexaHashKey.text = keyCallSpace

        // Animation
        hexaHashKey.animate().apply {
            duration = 1000
            rotationXBy(350f)
        }.withEndAction {
            hexaHashKey.animate().apply {
                duration = 1000
                rotationXBy(10f)
            }
        }

        if (keygenData.isNullOrEmpty()) {
            Snackbar.make(verifySecurity,
                "Please make sure you have a normal log in", Snackbar.LENGTH_SHORT).show()

        } else {
            val writer = QRCodeWriter()
            try {
                val bitMatrix = writer.encode(keygenData,
                    BarcodeFormat.QR_CODE,
                    512, 512)

                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

                for (pxx in 0 until height) {
                    for (pxy in 0 until height) {
                        bmp.setPixel(pxx,
                            pxy,
                            if (bitMatrix[pxx, pxy]) Color.BLACK else Color.WHITE)
                    }
                }
                vQRCode.setImageBitmap(bmp)


            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }

        vQRCode.setOnClickListener {
            // Starts scanner on Create of Overlay (you can also call this function using a button click)
            initScanner()
        }

        findViewById<Button>(R.id.verifySecurityButton).setOnClickListener {
            put0xBlank(chat_id)
            put0xBlankProfile(chat_id) {}
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    Snackbar.make(binding.root,
                        "A tua rende foi validada", Snackbar.LENGTH_SHORT).show()
                    delay(1000)
                    Snackbar.make(binding.root,
                        "Bem vindo a WEB 3.0", Snackbar.LENGTH_SHORT).show()
                    delay(2000)
                    finish()
                }
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {

            if (result.contents == null) {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Secreat Key " + result.contents, Toast.LENGTH_LONG).show()
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun blankSpaces(key: String): String {

        var result = ""
        for (i in key.indices)
            result += if (i % 4 == 0) " ${key[i]}" else key[i]

        return result
    }

    // Start the QR Scanner
    private fun initScanner() {
        IntentIntegrator(this).initiateScan()
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Ipca plus security in Blockchain")
        integrator.setCameraId(0)
        integrator.setOrientationLocked(true)
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

}

