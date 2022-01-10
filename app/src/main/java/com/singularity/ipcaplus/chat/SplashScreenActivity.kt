package com.singularity.ipcaplus.chat

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.drawer.DrawerActivty
import com.google.firebase.messaging.FirebaseMessaging


class SplashScreenActivity : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        startActivity(Intent(this, DrawerActivty::class.java))
        finish()
    }
}