package com.singularity.ipcaplus.chat

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.encoder.QRCode
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.cryptography.getMetaOx

class VerifySecurityNumberActivity : AppCompatActivity() {

    private lateinit var vQRCode: ImageView
    private lateinit var keygenHash: String
    private lateinit var verifySecurity: Button
    private lateinit var hexaHashKey: TextView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_security_number)

        // Get previous data
        val chat_id = intent.getStringExtra("chat_id").toString()
        hexaHashKey = findViewById(R.id.hexaHashTextView)

        //keygenHash = binding.verifySecurity.toString()
        vQRCode = findViewById(R.id.qrImageView)
        //keygenHash = "662ede816988e58fb6d057d9d85605e0"
        val keyCall = getMetaOx(this, chat_id)
        val keyCallSpace = blankSpaces(keyCall.toString())
        keygenHash = keyCall.toString()
        val keygenData = keygenHash.trim()

        hexaHashKey.text = keyCallSpace

        hexaHashKey.animate().apply {
            duration = 1000
            rotationXBy(350f)
        }.withEndAction {
            hexaHashKey.animate().apply {
                duration = 1000
                rotationXBy(10f)
            }
        }

        if (keygenData.isNullOrEmpty()){
            Snackbar.make(verifySecurity,
                "You need to Sign Up with (ipca.pt) email!", Snackbar.LENGTH_SHORT).show()

        }else{
            val writer = QRCodeWriter()
            try{
                val bitMatrix = writer.encode(keygenData,
                    BarcodeFormat.QR_CODE,
                    512, 512)

                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

                for ( pxx in 0 until height){
                    for (pxy in 0 until height){
                        bmp.setPixel(pxx, pxy, if (bitMatrix[pxx, pxy]) Color.BLACK else Color.WHITE)
                    }
                }
                vQRCode.setImageBitmap(bmp)


            }catch (e: WriterException){
                e.printStackTrace()
            }
        }
    }
}

private fun blankSpaces(key : String): String {

    var result = ""
    for (i in key.indices)
        result += if (i % 4 == 0) " ${key[i]}" else key[i]

    return result
}