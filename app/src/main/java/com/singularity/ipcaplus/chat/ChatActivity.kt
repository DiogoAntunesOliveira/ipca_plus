package com.singularity.ipcaplus.chat

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE
import com.singularity.ipcaplus.drawer.CalendarActivity
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.utils.Utilis
import com.singularity.ipcaplus.cryptography.decryptWithAESmeta
import com.singularity.ipcaplus.cryptography.encryptMeta
import com.singularity.ipcaplus.cryptography.getMetaOx
import com.singularity.ipcaplus.databinding.ActivityChatBinding
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Message
import com.singularity.ipcaplus.utils.ActivityImageHelper
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Backend.createJsonArrayString
import com.singularity.ipcaplus.utils.Backend.getIv
import com.singularity.ipcaplus.utils.Backend.getNotificationKey
import com.singularity.ipcaplus.utils.UserLoggedIn
import com.singularity.ipcaplus.utils.Utilis.sendNotificationToGroup
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

class ChatActivity : ActivityImageHelper() {

    var messages = arrayListOf<Message>()
    var currentUserIsAdmin = false
    private lateinit var binding: ActivityChatBinding
    private lateinit var chat_id: String
    var tokens_adress = arrayListOf<String>()
    var chat_user_uids = arrayListOf<String>()
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null

    lateinit var keygen: String

    // receive img from gallery
    private var imageUri: Uri? = null
    //lateinit var storageRef: FirebaseStorage? = null

    val db = Firebase.firestore

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.appbar_custom_layout_chat)
        findViewById<TextView>(R.id.AppBarTittle).text = "Chat name"
        // Back button
        findViewById<ImageView>(R.id.BackButtonImageView).setOnClickListener {
            finish()
        }

        // Variables
        chat_id = intent.getStringExtra("chat_id").toString()
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val formatted = current.format(formatter)

        keygen = getMetaOx(this, chat_id).toString()

        // Check if user is admin
        Backend.getChatAdminIds(chat_id) {
            val currentUser = Firebase.auth.currentUser!!.uid
            for (admin in it) {
                if (admin == currentUser)
                    currentUserIsAdmin = true
            }
        }


        val profilePicfromTop = findViewById<ImageView>(R.id.ProfileImageView)
        Utilis.getFile(this, "chats/$chat_id/icon.png", "png") { bitmap ->
            profilePicfromTop.setImageBitmap(bitmap)
        }

        println("Current Date is: $formatted")

        // Send Message

        binding.fabSend.setOnClickListener {

            if (!binding.editTextMessage.text.isNullOrBlank()) {
                // get data of ecripted shared preferences ("chatuid" -> "key")
                // Build encryptation data of message send by the user
                getIv(chat_id) {

                    var meta = encryptMeta(binding.editTextMessage.text.toString(),
                        keygen.toString(),
                        it.toString())
                    val savedText = binding.editTextMessage.text.toString()

                    val message = Message(
                        Firebase.auth.currentUser!!.uid,
                        meta.toString(),
                        Timestamp.now(),
                        ""
                    )

                    db.collection("chat").document("$chat_id").collection("message")
                        .add(message.toHash())
                        .addOnSuccessListener { documentReference ->

                            GlobalScope.launch {
                                withContext(Dispatchers.IO) {
                                    //APA91bEKDInIYA242YofpahBmhB57pEI4gNT63DJJenWCccJGqeSYrWzj0BSruX49DhVp2vGSY5xJ2fEJk2vhtoraT3_bbjEKw4Nx3eJKj7tttVRPjQs0Uc_OPkrcj4twR70H5tAilnY
                                    // APA91bGaOoMTjTD2s9MU63F1AvLqP6tkwdAFE0Mqs9jbghlSgcWlfe_38CboFiE2iiWFoKqNRwhF0G_TA5X9xegTL0_Tg0OGuFadJuBj1sGZqjqCcmF1EH2ZeRU7ySHosdNkmLmmOyFF
                                    println("AVEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE" + savedText)
                                    getNotificationKey(chat_id) {
                                        GlobalScope.launch {
                                            withContext(Dispatchers.IO) {
                                                sendNotificationToGroup(chat_id,
                                                    savedText,
                                                    it.toString())
                                            }
                                        }
                                    }
                                }
                            }
                            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")


                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                    binding.editTextMessage.text.clear()

                }
            }

        }

        // send img
        binding.buttonTakePhoto.setOnClickListener() {
            checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE)
        }


        // Show Messages
        db.collection("chat").document("$chat_id").collection("message")
            .orderBy("time", Query.Direction.DESCENDING)
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

        mLayoutManager!!.reverseLayout = true

    }

    /*
       This function create the action bar above the activity
    */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_chat, menu)

        db.collection("profile")
            .document(Firebase.auth.currentUser!!.uid)
            .collection("chat")
            .addSnapshotListener { documents, e ->
                documents?.let {
                    for (document in it) {
                        if (document.id == chat_id) {
                            val chat = Chat.fromHash(document)
                            var name = chat.name
                            if (chat.type == "chat") {
                                name = Utilis.getFirstAndLastName(chat.name)
                            }
                            //supportActionBar?.title = chat.name
                            findViewById<TextView>(R.id.AppBarTittle).text = name
                        }
                    }
                }
            }


        return true
    }


    /*
        This function define the events of the action bar buttons
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.calendario -> {
                val intent = Intent(this, CalendarActivity::class.java)
                intent.putExtra("chat_id", chat_id)
                intent.putExtra("is_admin", currentUserIsAdmin)
                startActivity(intent)
                return true
            }
            R.id.chatMore -> {
                val intent = Intent(this, ChatMoreActivity::class.java)
                intent.putExtra("chat_id", chat_id)
                println("3------------------------------ " + currentUserIsAdmin)
                intent.putExtra("is_admin", currentUserIsAdmin)
                intent.putExtra("chat_name",
                    findViewById<TextView>(R.id.AppBarTittle).text.toString())
                startActivity(intent)
                return true
            }
        }

        return false
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
                //binding.imageViewProfile.setImageURI(result.uri)
                imageUri = result.uri


                val extensionArray = Pattern.compile("[.]").split(result.uri!!.toString())
                val extension = extensionArray[extensionArray.size - 1]

                CoroutineScope(Dispatchers.IO).launch {
                    val userId = FirebaseAuth.getInstance().currentUser!!.uid
                    val storageRef = FirebaseStorage.getInstance()
                        .getReference("chats/${chat_id}/messages/${Utilis.uniqueImageNameGen()}.${extension}")

                    println("----------------------------------------------------------------------" + storageRef)
                    // compressing image
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 6, byteArrayOutputStream)
                    val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

                    storageRef.putBytes(reducedImage)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener {

                                println("--------------------------------------- Entrou aqui")
                                //uploading image url
                                val filePath =
                                    "chats/${chat_id}/messages/${Utilis.uniqueImageNameGen()}.${extension}"
                                Utilis.uploadFile(result.uri,
                                    filePath)


                                getIv(chat_id) { iv ->

                                    var meta = encryptMeta(filePath,
                                        keygen,
                                        iv.toString())

                                    val message = Message(
                                        Firebase.auth.currentUser!!.uid,
                                        meta.toString(),
                                        Timestamp.now(),
                                        "img"
                                    )

                                    db.collection("chat").document("$chat_id").collection("message")
                                        .add(message.toHash())
                                        .addOnSuccessListener { documentReference ->

                                            GlobalScope.launch {
                                                withContext(Dispatchers.IO) {

                                                    getNotificationKey(chat_id) {
                                                        GlobalScope.launch {
                                                            withContext(Dispatchers.IO) {
                                                                sendNotificationToGroup(chat_id,
                                                                    Utilis.getFirstAndLastName(
                                                                        UserLoggedIn.name.toString()) + " enviou uma imagem.",
                                                                    it.toString())
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Log.d(TAG,
                                                "DocumentSnapshot added with ID: ${documentReference.id}")


                                        }


                                }
                            }
                        }
                }
            }
        }
    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    inner class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        var otherUser = false
        var currentIndex = 0
        var messageType = ""

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            messageType = messages[currentIndex].files
            println("----------------------------------------------------" + messageType)

            if (viewType == 1) {
                otherUser = false

                if (messageType == "img") {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_message_self_image, parent, false))
                } else if (messageType == "file") {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_message_self_file, parent, false))
                } else {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_message_self, parent, false))
                }

            } else if (viewType == 2) {
                otherUser = false
                return ViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.row_message_system, parent, false))
            } else {
                otherUser = true
                if (messageType == "img") {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_message_others_image, parent, false))
                } else if (messageType == "file") {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_message_others_file, parent, false))
                } else {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_message_others, parent, false))
                }

            }


        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {

                if (messageType == "") {
                    val textViewMessage = findViewById<TextView>(R.id.textViewMessage)
                    val timeLastMessage = findViewById<TextView?>(R.id.timeLastMessage)
                    val textViewUsername = findViewById<TextView?>(R.id.textViewUsername)

                    timeLastMessage?.isVisible = false
                    keygen = getMetaOx(context, chat_id).toString()
                    getIv(chat_id) {
                        val message_decripted = decryptWithAESmeta(keygen.toString(),
                            messages[position].message,
                            it.toString())

                        textViewMessage.text = message_decripted
                        println(message_decripted)
                        if (position == messages.size - 1) {
                            val data = Utilis.getDate(
                                messages[position].time.seconds * 1000,
                                "yyyy-MM-dd'T'HH:mm:ss.SSS"
                            )
                            timeLastMessage.isVisible = true
                            timeLastMessage.text =
                                Utilis.getHours(data) + ":" + Utilis.getMinutes(data)
                        }

                        if (otherUser) {
                            val imageViewUser = findViewById<ImageView?>(R.id.imageViewUser)
                            if (imageViewUser != null) {
                                Utilis.getFile(context,
                                    "profilePictures/${messages[position].user}.png",
                                    "png") { bitmap ->
                                    imageViewUser.setImageBitmap(bitmap)
                                }
                            }
                        }
                    }
                } else if (messageType == "img") {

                    val imageView: ImageView = findViewById(R.id.imageViewSend)
                    val timeLastMessage = findViewById<TextView?>(R.id.timeLastMessage)

                    getIv(chat_id) {
                        val message_decripted = decryptWithAESmeta(keygen.toString(),
                            messages[position].message,
                            it.toString())


                        if (position == messages.size - 1) {
                            val data = Utilis.getDate(
                                messages[position].time.seconds * 1000,
                                "yyyy-MM-dd'T'HH:mm:ss.SSS"
                            )
                            timeLastMessage.isVisible = true
                            timeLastMessage.text =
                                Utilis.getHours(data) + ":" + Utilis.getMinutes(data)
                        }

                        if (imageView != null) {
                            val extensionArray =
                                Pattern.compile("[.]").split(message_decripted.toString())
                            val extension = extensionArray[extensionArray.size - 1]
                            println("--------------------------------------------------------------------------------")
                            println(extension)
                            println(message_decripted)
                            Utilis.getFile(context,
                                message_decripted.toString(),
                                extension) { bitmap ->
                                imageView.setImageBitmap(bitmap)
                            }
                        }

                    }

                } else if (messageType == "file") {

                }

                currentIndex = position + 1
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (messages[position].user == Firebase.auth.currentUser!!.uid) {
                return 1
            } else if (messages[position].user == "system") {
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