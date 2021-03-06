package com.singularity.ipcaplus.chat

import android.content.Intent
import android.os.Build
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
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.core.view.size
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.cryptography.decryptWithAESmeta
import com.singularity.ipcaplus.cryptography.getMetaOx
import com.singularity.ipcaplus.cryptography.saveKeygenOx
import com.singularity.ipcaplus.databinding.ActivitySearchBinding
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.utils.Utilis
import java.util.*
import java.util.regex.Pattern


class SearchActivity : AppCompatActivity() {

    var chats = arrayListOf<Chat>()
    var chatIds = arrayListOf<String>()
    var tempArrayChats = arrayListOf<Chat>()
    var tempArrayChatsIds = arrayListOf<String>()

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
        overridePendingTransition(0, 0)

        back.setOnClickListener() {
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
                        if (chat.type == "chat" || chat.type == "group") {
                            chats.add(chat)
                            chatIds.add(document.id)
                        }
                    }

                    tempArrayChats.addAll(chats)
                    tempArrayChatsIds.addAll(chatIds)

                    mAdapter?.notifyDataSetChanged()
                }
            }


        // SearchBar Find chat
        val search = findViewById<SearchView>(R.id.searchView)
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                tempArrayChats.clear()

                val searchText = newText!!.toLowerCase(Locale.getDefault())

                if (searchText.isEmpty()) {
                    tempArrayChats.clear()
                    tempArrayChats.addAll(chats)

                } else if (searchText.isNotEmpty()) {
                    tempArrayChats.clear()
                    for (chat in chats) {
                        if (chat.name.toLowerCase().contains(searchText)) {

                            tempArrayChats.add(chat)

                        }
                    }
                }

                mAdapter?.notifyDataSetChanged()

                return false
            }


        })

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

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {


                // Variables
                val textViewMessage = findViewById<TextView>(R.id.textViewProfileName)
                val imageViewImage = findViewById<ImageView>(R.id.imageViewProfileGroup)
                val lastMessageText = findViewById<TextView>(R.id.textViewLastMessage)


                // set name
                textViewMessage.text = Utilis.getFirstAndLastName(tempArrayChats[position].name)

                // sync data recieved form direbase with encrypted shared preferences (key -> 1x)
                if (chats[position].ox.isNullOrBlank() || chats[position].ox.isNullOrEmpty()) {
                    chats[position].ox = getMetaOx(context, chatIds[position])
                } else {
                    saveKeygenOx(context, chatIds[position], chats[position].ox.toString())
                }

                // Set Last Chat Message
                Backend.getLastMessageByChatID(chatIds[position]) {

                    val keygen = getMetaOx(context, chatIds[position])
                    Backend.getIv(chatIds[position]) { iv ->
                        val message_decripted =
                            decryptWithAESmeta(keygen.toString(), it?.message, iv.toString())
                        lastMessageText.text = message_decripted
                    }

                }

                // set image
                Utilis.getFile(this.context,
                    "chats/${chatIds[position]}/icon.png",
                    "png") { bitmap ->
                    imageViewImage.setImageBitmap(bitmap)
                }

            }
            holder.v.setOnClickListener {
                val intent = Intent(this@SearchActivity, ChatActivity::class.java)
                intent.putExtra("chat_id", chatIds[position])
                startActivity(intent)
            }

        }

        override fun getItemCount(): Int {
            return tempArrayChats.size
        }
    }

}