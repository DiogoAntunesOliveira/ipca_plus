package com.singularity.ipcaplus

import android.R.id
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.R.id.text2
import android.graphics.Color

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.underline
import com.singularity.ipcaplus.drawer.ProfileActivity
import org.bouncycastle.util.Arrays.append


class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        var loginButton = findViewById<Button>(R.id.LoginButton)
        var registerButton = findViewById<Button>(R.id.RegisterButton)

        loginButton.setOnClickListener{

            var intent = Intent(this, LoginActivity::class.java )
            startActivity(intent)
        }

        registerButton.setOnClickListener{

            var intent = Intent(this, RegisterActivity::class.java )
            startActivity(intent)
        }

        val text = SpannableStringBuilder()
            .append("Ao clicares em registar, concordas com os")
            .color(resources.getColor(R.color.green_200)) { underline { append(" termos e condições ") } }
            .append("de uso do IPCA Plus.")

        val terms = findViewById<TextView>(R.id.textViewTerms)
        terms.text = text

        terms.setOnClickListener {
            val intent = Intent(this, TermsAndCondictionsActivity::class.java)
            startActivity(intent)
        }

    }




}