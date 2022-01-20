package com.singularity.ipcaplus.chat

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.cryptography.generateRandomIV
import com.singularity.ipcaplus.cryptography.metaGenrateKey
import com.singularity.ipcaplus.databinding.ActivityCreateChatBinding
import com.singularity.ipcaplus.drawer.DrawerActivty
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Message
import com.singularity.ipcaplus.utils.ActivityImageHelper
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Backend.createJsonArrayString
import com.singularity.ipcaplus.utils.Utilis
import com.singularity.ipcaplus.utils.Utilis.buildSystemMessage
import com.singularity.ipcaplus.utils.Utilis.createNotificationGroup
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateDirectChatActivity : ActivityImageHelper() {


    val db = Firebase.firestore
    var noteKey: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Variables
        var chatName: String

        var type = intent.getStringExtra("type")!!
        var memberIds = intent.getStringArrayListExtra("users")!!
        var tokens_adress = arrayListOf<String>()

        val ivGenerated = generateRandomIV()

        for (memberId in memberIds) {

            // Getting all of tokens of  profile associated devices
            Backend.getAllTokens(memberId) {
                if (tokens_adress.isEmpty()) {
                    tokens_adress.clear()
                }
                tokens_adress.addAll(it)

                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        Log.d("paaaaaa", tokens_adress.toString())
                        noteKey = createNotificationGroup(generateRandomIV(),
                            createJsonArrayString(tokens_adress))
                    }
                }

            }

        }

        // Generate key for chats
        val keygen = metaGenrateKey()

        Backend.getUserProfile(memberIds[1]) {


            chatName = it.name

            // Chat data
            var chat = Chat(
                chatName,
                type,
                keygen,
                ivGenerated,
                noteKey
            )

            // System message data
            val message = Message(
                "system",
                buildSystemMessage(keygen, ivGenerated),
                Timestamp.now(),
                ""

            )

            val user = HashMap<String, Any>()
            val admin = hashMapOf<String?, Any>(
                "admin" to true
            )

            db.collection("chat")
                .add(chat.toHash())
                .addOnSuccessListener { documentReference ->
                    db.collection("chat")
                        .document(documentReference.id)
                        .collection("message")
                        .add(message.toHash())
                    for (member in memberIds) {
                        Backend.getUserProfile(memberIds[0]) {
                            if (member == Firebase.auth.currentUser!!.uid) {
                                // Chat data
                                chat = Chat(
                                    it.name,
                                    type,
                                    keygen,
                                    ivGenerated,
                                    noteKey
                                )
                            }

                            db.collection("profile")
                                .document(member)
                                .collection("chat")
                                .document(documentReference.id)
                                .set(chat)
                            db.collection("chat")
                                .document(documentReference.id)
                                .collection("user")
                                .document(member)
                                .set(user)
                            db.collection("chat")
                                .document(documentReference.id)
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