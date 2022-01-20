package com.singularity.ipcaplus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.chat.ChatActivity
import com.singularity.ipcaplus.chat.CreateChatActivity
import com.singularity.ipcaplus.chat.CreateDirectChatActivity
import com.singularity.ipcaplus.databinding.ActivityAddButtonBinding
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Profile
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Utilis

class AddButtonActivity : AppCompatActivity() {

    var users = arrayListOf<Profile>()
    var chats = arrayListOf<Chat>()
    var chatIds = arrayListOf<String?>()
    val userIds = arrayListOf<String>()
    var directChat: String? = null
    val currentUser = Firebase.auth.currentUser!!.uid

    var currentUserchats = arrayListOf<String>()
    var selectedUserchats = arrayListOf<String>()

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
        binding.backBtn.setOnClickListener() {
            finish()
        }

        binding.constraintLayoutNewGroup.setOnClickListener() {
            val intent = Intent(this, AddPeopleActivity::class.java)
            startActivity(intent)

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

        // Get All User Chats
        db.collection("profile").document("${Firebase.auth.currentUser!!.uid}").collection("chat")
            .addSnapshotListener { documents, e ->
                documents?.let {
                    chats.clear()
                    for (document in it) {
                        val chat = Chat.fromHash(document)
                        if (chat.type == "chat") {
                            chats.add(chat)
                            currentUserchats.add(document.id)
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
                val imageViewImage = findViewById<ImageView>(R.id.imageViewProfile)

                // give data
                textViewName.text = users[position].name
                Utilis.getFile(this.context,
                    "profilePictures/" + users[position].id + ".png",
                    "png") { bitmap ->
                    imageViewImage.setImageBitmap(bitmap)
                }

            }
            holder.v.setOnClickListener {
                Backend.getAllDirectChatIdsByUser(currentUser) {
                    chatIds.addAll(it)

                    if (chatIds.isNotEmpty()) {
                        Backend.getDirectChatById(it, users[position].id.toString()) {
                            directChat = it

                            if (directChat.isNullOrEmpty()) {
                                userIds.add(users[position].id.toString())
                                userIds.add(currentUser)

                                val intent = Intent(this@AddButtonActivity,
                                    CreateDirectChatActivity::class.java)
                                intent.putExtra("type", "chat")
                                intent.putExtra("users", userIds)
                                startActivity(intent)

                            } else {
                                // Abrir chat ja criado
                                val intent =
                                    Intent(this@AddButtonActivity, ChatActivity::class.java)
                                intent.putExtra("chat_id", directChat)
                                startActivity(intent)
                            }
                        }
                    } else {
                        userIds.add(users[position].id.toString())
                        userIds.add(currentUser)

                        val intent =
                            Intent(this@AddButtonActivity, CreateDirectChatActivity::class.java)
                        intent.putExtra("users", userIds)
                        intent.putExtra("type", "chat")
                        startActivity(intent)
                    }

                }

            }

        }

        override fun getItemCount(): Int {
            return users.size
        }
    }

}