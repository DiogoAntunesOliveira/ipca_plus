package com.singularity.ipcaplus

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.databinding.ActivityMainBinding
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Message
import kotlin.io.path.Path
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    var chats = arrayListOf<Chat>()
    var chatIds = arrayListOf<String>()
    var user_groups = arrayListOf<String>()

    private lateinit var binding: ActivityMainBinding
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null

    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Criação de Chat
        binding.fabAddChat.setOnClickListener {
            val chat = Chat(
                "Chat ${Random.nextInt(0, 100)}"

            )
            val message = Message(
                Firebase.auth.currentUser!!.uid,
                "Teste",
                "",
                "",
                ""

            )
            db.collection("chat")
                .add(chat.toHash())
                .addOnSuccessListener { documentReference ->
                    db.collection("chat").document("${documentReference.id}").collection("message")
                        .add(message.toHash())
                    db.collection("profile").document("${Firebase.auth.currentUser!!.uid}").collection("chat").document("${documentReference.id}")
                        .set(chat)

                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }

        }

        db.collection("profile").document("${Firebase.auth.currentUser!!.uid}").collection("chat")
            .addSnapshotListener { documents, e ->

                documents?.let {
                    chats.clear()
                    chatIds.clear()
                    for (document in it) {
                        Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                        val chat = Chat.fromHash(document)
                        chatIds.add(document.id)
                        chats.add(chat)

                    }
                    mAdapter?.notifyDataSetChanged()
                }

            }

        mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewGroups.layoutManager = mLayoutManager
        mAdapter = ChatAdapter()
        binding.recyclerViewGroups.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewGroups.adapter = mAdapter
    }


    inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_chat, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {


            holder.v.apply {
                val textViewMessage = findViewById<TextView>(R.id.textViewChatName)
                val imageViewChatGroup = findViewById<ImageView>(R.id.imageViewChatGroup)


                textViewMessage.text = chats[position].name
                imageViewChatGroup.setImageResource(R.drawable.common_full_open_on_phone)
            }
            holder.v.setOnClickListener {
                val intent = Intent(this@MainActivity, ChatActivity::class.java)
                intent.putExtra("chat_id", chatIds[position])
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return chats.size
        }
    }
}
