package com.singularity.ipcaplus.profile

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.Utilis.getJsonDataFromAsset

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val jsonFileString = getJsonDataFromAsset(applicationContext, "ipcaclone.json")
        Log.i("data", jsonFileString.toString())

    }

}