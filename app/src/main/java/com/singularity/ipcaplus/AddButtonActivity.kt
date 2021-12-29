package com.singularity.ipcaplus

import android.content.Intent
import android.graphics.drawable.Icon
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.chat.ChatActivity
import com.singularity.ipcaplus.databinding.ActivityAddButtonBinding
import com.singularity.ipcaplus.databinding.ActivitySearchBinding
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Profile
import com.singularity.ipcaplus.utils.UserLoggedIn
import com.singularity.ipcaplus.utils.Utilis

class AddButtonActivity: AppCompatActivity() {

    var users = arrayListOf<Profile>()
    var chats = arrayListOf<Chat>()
    var chatIds = arrayListOf<String>()

    private lateinit var binding: ActivityAddButtonBinding

    private var userAdapter: RecyclerView.Adapter<*>? = null
    private var userLayoutManager: LinearLayoutManager? = null


    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_button)
        binding = ActivityAddButtonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // finish de activity
        val back = findViewById<ImageView>(R.id.back_btn)
        back.setOnClickListener(){
            finish()
        }

        // Get All Users
        db.collection("profile")
            .addSnapshotListener { documents, e ->
                documents?.let {
                    users.clear()
                    for (document in documents) {

                        val user = Profile.fromHash(document)
                        user.id = document.id
                        users.add(user)
                    }
                    userAdapter?.notifyDataSetChanged()
                }

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
                }
        }

        // Set data for RecyclerView Users

        userLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewProfile.layoutManager = userLayoutManager
        userAdapter = SearchAdapter()
        binding.recyclerViewProfile.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewProfile.adapter = userAdapter
       
    }


    inner class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_user, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {
                // Variables
                val textViewName = findViewById<TextView>(R.id.textViewProfileName)
                val imageViewImage = findViewById<ImageView>(R.id.imageViewProfileGroup)

                // give data
                textViewName.text = users[position].name
                Utilis.getImage("profilePictures/" + users[position].id + ".png") { bitmap ->
                    imageViewImage.setImageBitmap(bitmap)
                }

            }
            holder.v.setOnClickListener {
                val intent = Intent(this@AddButtonActivity, ChatActivity::class.java)
                intent.putExtra("chat_id", chatIds[position])
                startActivity(intent)
            }

        }

        override fun getItemCount(): Int {
            return users.size
        }
    }

}