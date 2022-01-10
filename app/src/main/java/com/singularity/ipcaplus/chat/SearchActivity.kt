package com.singularity.ipcaplus.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivitySearchBinding
import com.singularity.ipcaplus.models.Chat

class SearchActivity: AppCompatActivity() {

    var chats = arrayListOf<Chat>()
    var chatIds = arrayListOf<String>()

    private lateinit var binding: ActivitySearchBinding

    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null


    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val back = findViewById<ImageView>(R.id.back_btn)

        back.setOnClickListener(){
            finish()
        }

        // Get All Chats
        db.collection("profile").document("${Firebase.auth.currentUser!!.uid}").collection("chat")
            .addSnapshotListener { documents, e ->
                documents?.let {
                    chats.clear()
                    chatIds.clear()
                    for (document in it) {
                        val chat = Chat.fromHash(document)
                        if (chat.type == "chat") {
                            chats.add(chat)
                            chatIds.add(document.id)
                        }
                    }
                    mAdapter?.notifyDataSetChanged()
                }
            }

        // RecyclerView Chats
        mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewProfile.layoutManager = mLayoutManager
        mAdapter = SearchAdapter()
        binding.recyclerViewProfile.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewProfile.adapter = mAdapter
       
    }


    inner class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_search, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {




            holder.v.apply {


                // Variables
                val textViewMessage = findViewById<TextView>(R.id.textViewProfileName)
                val imageViewChatGroup = findViewById<ImageView>(R.id.imageViewProfileGroup)
                val lastMessageText = findViewById<TextView>(R.id.textViewLastMessage)

                textViewMessage.text = chats[position].name
                // Set Last Chat Message
                Backend.getLastMessageByChatID(chatIds[position]) {
                    lastMessageText.text = it?.message
                }
                imageViewChatGroup.setImageResource(R.drawable.common_full_open_on_phone)

                textViewMessage.text = chats[position].name
            }
            holder.v.setOnClickListener {
                val intent = Intent(this@SearchActivity, ChatActivity::class.java)
                intent.putExtra("chat_id", chatIds[position])
                startActivity(intent)
            }

        }

        override fun getItemCount(): Int {
            return chats.size
        }
    }

}