package com.singularity.ipcaplus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        var registerButton = findViewById<Button>(R.id.LoginButton)
        registerButton.setOnClickListener{

            var intent = Intent(this, LoginActivity::class.java )
            startActivity(intent)
        }

    }




}