package com.singularity.ipcaplus.drawer

import android.annotation.SuppressLint
import android.app.ActionBar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivityAddEventBinding
import com.singularity.ipcaplus.models.EventCalendar
import com.singularity.ipcaplus.utils.Utilis
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding


    val db = Firebase.firestore
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Marcar tarefa"
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_bar_layout)
        findViewById<TextView>(R.id.AppBarTittle).text = "Agendar Evento"
        // Back button
        findViewById<ImageView>(R.id.BackButtonImageView).setOnClickListener{
            finish()
        }

        // Get chat id
        val chat_id = if (intent.hasExtra("chat_id")) intent.getStringExtra("chat_id").toString() else "none"

        val datePickerDialog = Utilis.initDatePicker(binding.datePicker, this)

        binding.datePicker.setOnClickListener {
            datePickerDialog.show()
        }

        // Save Event
        binding.buttonSave.setOnClickListener {

            val lastHalf = Pattern.compile("/").split(binding.datePicker.text)[2]
            val year = Pattern.compile(" - ").split(lastHalf)[0]
            val month = Pattern.compile("/").split(binding.datePicker.text)[1]
            val day = Pattern.compile("/").split(binding.datePicker.text)[0]
            val hour = Pattern.compile(":").split(Pattern.compile(" - ").split(lastHalf)[1])[0]
            val minute = Pattern.compile(":").split(Pattern.compile(" - ").split(lastHalf)[1])[1]

            val myDate = "$year/$month/$day $hour:$minute:00"
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val date = sdf.parse(myDate)
            val millis = date.time

            if (!binding.editTextTitle.text.isNullOrBlank()) {
                val stampSec = TimeUnit.MILLISECONDS.toSeconds(millis)

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