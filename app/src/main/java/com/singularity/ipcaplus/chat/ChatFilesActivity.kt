package com.singularity.ipcaplus.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivityChatFilesBinding
import com.singularity.ipcaplus.databinding.ActivityTermsAndCondictionsBinding
import com.singularity.ipcaplus.models.FirebaseFile
import com.singularity.ipcaplus.models.Profile
import com.singularity.ipcaplus.utils.Backend

class ChatFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatFilesBinding

    var files = arrayListOf<FirebaseFile>()
    private var filesAdapter: RecyclerView.Adapter<*>? = null
    private var filesLayoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_files)

        // Create the layout for this fragment
        binding = ActivityChatFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Ficheiros"

        // Get previous data
        val chat_id = intent.getStringExtra("chat_id").toString()

        Backend.getAllChatFolderFiles("chats/$chat_id") { _files ->

            files.clear()
            files.addAll(_files)
            filesAdapter?.notifyDataSetChanged()

        }

        // List
        filesLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.filesRecyclerView.layoutManager = filesLayoutManager
        filesAdapter = FileAdapter()
        binding.filesRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.filesRecyclerView.adapter = filesAdapter
    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    inner class FileAdapter : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_file, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {

                val textViewName = findViewById<TextView>(R.id.textViewName)
                val imageViewIcon = findViewById<ImageView>(R.id.imageViewIcon)
                textViewName.text = files[position].name
                imageViewIcon.setImageDrawable(resources.getDrawable(files[position].icon))

            }
        }

        override fun getItemCount(): Int {
            return files.size
        }
    }

}