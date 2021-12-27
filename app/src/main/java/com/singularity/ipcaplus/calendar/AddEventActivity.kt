package com.singularity.ipcaplus.calendar

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.DrawerActivty
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivityAddEventBinding
import com.singularity.ipcaplus.models.EventCalendar
import java.util.concurrent.TimeUnit

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding


    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Marcar tarefa"

        // Get chat id
        val chat_id = if (intent.hasExtra("chat_id")) intent.getStringExtra("chat_id").toString() else "none"

        // Save Event
        binding.buttonSave.setOnClickListener {
            if(!binding.editTextTitle.text.isNullOrBlank()) {
                val stampCurrent = System.currentTimeMillis()  // Transformar datetime em millis e mandar praqui
                val stampSec = TimeUnit.MILLISECONDS.toSeconds(stampCurrent)

                val event = EventCalendar(
                    Timestamp(stampSec, 0),
                    binding.editTextDescription.text.toString(),
                    binding.editTextTitle.text.toString()
                )
                db.collection("chat")
                    .document(chat_id)
                    .collection("event")
                    .add(event.toHash())
                    .addOnSuccessListener {

                        // Change Activity
                        onBackPressed()
                    }

            }
        }

    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}