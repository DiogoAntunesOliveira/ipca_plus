package com.singularity.ipcaplus.chat

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.cryptography.metaGenrateKey
import com.singularity.ipcaplus.databinding.ActivityCreateChatBinding
import com.singularity.ipcaplus.drawer.DrawerActivty
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Message
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Utilis.buildSystemMessage

class CreateChatActivity : AppCompatActivity() {

    // Variables
    private lateinit var binding: ActivityCreateChatBinding

    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_chat)

        binding = ActivityCreateChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Variables
        // Get Member list from previous activity
        var memberIds = arrayListOf<String>("yYltgAco9YVgSQEtlGhkt5qrVMJ2", "bEWfuAdTu3bfhHgOaOFkynIgHjH3", "Y90PjGQmLsMrxLicWkirOKpPSOx2", "hvxkhLNkDeYNdb0dxS0sgMXMDWy1")
        var chatName = "Os Gostosos"
        var chatPhoto = ""
        var adminName = ""

        // Generate key for chats
        val keygen = metaGenrateKey()

        // Create Chat
        binding.fabCreateChat.setOnClickListener {

            // Chat data
            val chat = Chat(
                chatName,
                "chat",
                keygen
            )

            // System message data
            val message = Message(
                "system",
                buildSystemMessage(keygen),
                Timestamp.now(),
                ""

            )

            Backend.getUserProfile(Firebase.auth.uid!!){
                adminName = it.name
            }

            val user = HashMap<String, Any>()
            val admin = hashMapOf<String?, Any> (
                "admin" to true
                    )

            db.collection("chat")
                .add(chat.toHash())
                .addOnSuccessListener { documentReference ->
                    db.collection("chat")
                        .document("${documentReference.id}")
                        .collection("message")
                        .add(message.toHash())
                    for(member in memberIds) {
                        db.collection("profile")
                            .document(member)
                            .collection("chat")
                            .document("${documentReference.id}")
                            .set(chat)
                        db.collection("chat")
                            .document("${documentReference.id}")
                            .collection("user")
                            .document(member)
                            .set(user)
                        if(member == Firebase.auth.currentUser!!.uid) {
                            db.collection("chat")
                                .document("${documentReference.id}")
                                .collection("user")
                                .document(member)
                                .update(admin)
                        }

                    }

                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }

            val intent = Intent(this, DrawerActivty::class.java)
            startActivity(intent)
        }

    }
}