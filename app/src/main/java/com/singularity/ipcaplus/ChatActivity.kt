package com.singularity.ipcaplus

import android.content.ContentValues.TAG
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.databinding.ActivityChatBinding
import com.singularity.ipcaplus.models.Message
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    var messages = arrayListOf<Message>()

    private lateinit var binding: ActivityChatBinding
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null

    val db = Firebase.firestore
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Variables
        val chat_id = intent.getStringExtra("chat_id")
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val formatted = current.format(formatter)

        println("Current Date is: $formatted")

        // Send Message
            binding.fabSend.setOnClickListener {
                if(!binding.editTextMessage.text.isNullOrBlank()) {

                    val message = Message(
                        Firebase.auth.currentUser!!.uid,
                        binding.editTextMessage.text.toString(),
                        "",
                        Timestamp.now(),
                        ""

                    )
                    db.collection("chat").document("$chat_id").collection("message")
                        .add(message.toHash())
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                    binding.editTextMessage.text.clear()
                }
        }
        db.collection("chat").document("$chat_id").collection("message").orderBy("time")
            .addSnapshotListener { documents, e ->

                documents?.let {
                    messages.clear()
                    for (document in it) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        val message = Message.fromHash(document)
                        messages.add(message)

                    }
                    mAdapter?.notifyDataSetChanged()
                }

            }

        // Recycler View Messages
        mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycleViewChat.layoutManager = mLayoutManager
        mAdapter = MessageAdapter()
        binding.recycleViewChat.itemAnimator = DefaultItemAnimator()
        binding.recycleViewChat.adapter = mAdapter

        /*
            if(binding.editTextMessage.text == null) {
                binding.buttonTakePhoto.visibility = View.VISIBLE
                println("VISIBLE")
            } else {
                binding.buttonTakePhoto.visibility = View.GONE
                println("GONE")
            }
        */

    }


    inner class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            if(viewType == 1) {
                return ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_message_self, parent, false))
            } else if (viewType == 2){
                return ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_message_system, parent, false))
            } else {
                return ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_message_others, parent, false))
            }

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {


            holder.v.apply {

                val textViewMessage = findViewById<TextView>(R.id.textViewMessage)
                textViewMessage.text = messages[position].message

            }
        }

        override fun getItemViewType(position: Int) : Int {
            if (messages[position].user == Firebase.auth.currentUser!!.uid) {
                return 1
            } else if (messages[position].user == "system"){
                return 2
            } else {
                return 0
            }
        }

        override fun getItemCount(): Int {
            return messages.size
        }


    }
}