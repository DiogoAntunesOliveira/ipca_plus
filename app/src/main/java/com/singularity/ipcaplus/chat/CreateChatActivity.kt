package com.singularity.ipcaplus.chat

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
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
import com.singularity.ipcaplus.utils.Backend.updateNotificationKeyCamp
import com.singularity.ipcaplus.utils.Utilis
import com.singularity.ipcaplus.utils.Utilis.buildSystemMessage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.*

class CreateChatActivity : ActivityImageHelper() {

    // Variables
    private lateinit var binding: ActivityCreateChatBinding
    var uri = Uri.EMPTY
    val db = Firebase.firestore
    var noteKey: String = ""
    lateinit var docId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_chat)

        binding = ActivityCreateChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_bar_layout)
        var tokens_adress = arrayListOf<String>()
        findViewById<TextView>(R.id.AppBarTittle).text = "Novo Grupo"
        // Back button
        findViewById<ImageView>(R.id.BackButtonImageView).setOnClickListener {
            finish()
        }

        // Variables
        var chatName: String

        var type = intent.getStringExtra("type")!!
        var memberIds = intent.getStringArrayListExtra("users")!!

        // Generate key for chats
        val keygen = metaGenrateKey()

        binding.imageViewChatPhoto.setOnClickListener {
            checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE)
        }


        // Create Chat
        binding.fabCreateChat.setOnClickListener {


            chatName = binding.editTextChatName.text.toString()
            val ivGenerated = generateRandomIV()

            // Chat data
            val chat = Chat(
                chatName,
                type.toString(),
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
                    docId = documentReference.id
                    db.collection("chat")
                        .document(documentReference.id)
                        .collection("message")
                        .add(message.toHash())
                    for (member in memberIds) {
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
                        // Getting all of tokens of  profile associated devices
                        Backend.getAllTokens(member) {
                            tokens_adress.addAll(it)
                            println("TOUUUUUUUUU $tokens_adress")
                            docId = documentReference.id
                        }
                        if (member == Firebase.auth.currentUser!!.uid) {
                            db.collection("chat")
                                .document(documentReference.id)
                                .collection("user")
                                .document(member)
                                .update(admin)
                        }
                        if (uri != Uri.EMPTY)
                            Utilis.uploadFile(uri, "chats/${documentReference.id}/icon.png")

                    }

                }.addOnCompleteListener {
                    val intent = Intent(this, DrawerActivty::class.java)
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            delay(1000)
                            noteKey = Utilis.createNotificationGroup(docId,
                                Backend.createJsonArrayString(tokens_adress))
                            updateNotificationKeyCamp(docId, noteKey)

                            println(docId)
                            println("TOUUUUUUUUU FINALLL $tokens_adress")
                            println("este $noteKey")
                            startActivity(intent)
                        }
                    }
                }
        }

    }

    /*
       This function happen after picking photo, and make changes in the activity
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.activity(data?.data)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                binding.imageViewChatPhoto.setImageURI(result.uri)
                uri = result.uri

            }
        }
    }
}