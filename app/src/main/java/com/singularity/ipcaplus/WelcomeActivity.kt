package com.singularity.ipcaplus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

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

    }




}