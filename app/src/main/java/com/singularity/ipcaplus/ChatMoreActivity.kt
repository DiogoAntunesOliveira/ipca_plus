package com.singularity.ipcaplus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.singularity.ipcaplus.databinding.ActivityChatMoreBinding

class ChatMoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_more)

        // Create the layout for this fragment
        binding = ActivityChatMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Definições do grupo"

        // Get previous data
        val chat_id = intent.getStringExtra("chat_id").toString()
        val chat_name = intent.getStringExtra("chat_name").toString()
        binding.textViewGroupName.text = chat_name

        // Set Chat Image

        binding.seeGroupMembers.setOnClickListener {
            println("-------------> 1")
        }

        binding.changeGroupName.setOnClickListener {
            println("-------------> 2")
        }

        binding.changeGroupImage.setOnClickListener {
            println("-------------> 3")
        }

        binding.groupFiles.setOnClickListener {
            println("-------------> 4")
        }

        binding.notifications.setOnClickListener {
            println("-------------> 5")
        }
    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}