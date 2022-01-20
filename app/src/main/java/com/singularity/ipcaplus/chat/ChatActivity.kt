package com.singularity.ipcaplus.chat

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE
import com.singularity.ipcaplus.AddButtonActivity
import com.singularity.ipcaplus.drawer.CalendarActivity
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.utils.Utilis
import com.singularity.ipcaplus.cryptography.decryptWithAESmeta
import com.singularity.ipcaplus.cryptography.encryptMeta
import com.singularity.ipcaplus.cryptography.getMetaOx
import com.singularity.ipcaplus.databinding.ActivityChatBinding
import com.singularity.ipcaplus.drawer.DrawerActivty
import com.singularity.ipcaplus.drawer.ProfileActivity
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Message
import com.singularity.ipcaplus.models.Profile
import com.singularity.ipcaplus.utils.ActivityImageHelper
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Backend.createJsonArrayString
import com.singularity.ipcaplus.utils.Backend.getChatUsers
import com.singularity.ipcaplus.utils.Backend.getIv
import com.singularity.ipcaplus.utils.Backend.getNotificationKey
import com.singularity.ipcaplus.utils.UserLoggedIn
import com.singularity.ipcaplus.utils.Utilis.getFirstAndLastName
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
    var chat_users = arrayListOf<Profile>()
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null

    lateinit var keygen: String
    var userName = ""

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

                    Backend.getUserProfile(Firebase.auth.currentUser!!.uid) { user ->
                        userName = Utilis.getFirstAndLastName(user.name)
                    }

                    db.collection("chat").document("$chat_id").collection("message")
                        .add(message.toHash())
                        .addOnSuccessListener { documentReference ->

                            GlobalScope.launch {
                                withContext(Dispatchers.IO) {
                                    getNotificationKey(chat_id) {

                                        Backend.getGroupChatById(chat_id) { chat ->
                                            GlobalScope.launch {
                                                withContext(Dispatchers.IO) {
                                                    sendNotificationToGroup(chat!!.name,
                                                        "$userName: $savedText",
                                                        it.toString())

                                                }
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


        // send file
        binding.buttonSendAnexo.setOnClickListener {
            chooseFile()
        }

        // Show Messages
        db.collection("chat").document("$chat_id").collection("message")
            .orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { documents, e ->

                documents?.let {
                    messages.clear()
                    for (document in it) {
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

        binding.recycleViewChat.setItemViewCacheSize(20)
        binding.recycleViewChat.isDrawingCacheEnabled = true
        binding.recycleViewChat.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

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
        Open Select a file window
    */
    private fun chooseFile() {
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val extraMimeTypes = arrayOf("application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
            "text/plain",
            "application/pdf",
            "application/zip",
            "image/gif",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/svg+xml",
            "image/webp",
            "image/vnd.wap.wbmp",
            "image/vnd.nok-wallpaper",
            "text/xml",
            "application/json",
            "text/json",
            "text/javascript"
        )
        intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes)
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, 1)
    }


    /*
        Request confirmation before download the file, then download and store the file in the download folder in the mobile device
    */
    fun downloadFileRequest(path: String, name: String) {

        confirmationDialog("Transferir Ficheiro",
            "Tens certeza que queres transferir este ficheiro?") {

            val fileRef = Firebase.storage.reference.child("$path/${name}")
            val strArray = Pattern.compile("[.]").split(name)
            val fileName = strArray[0]
            val fileExtension = strArray[strArray.size - 1]

            fileRef.downloadUrl.addOnSuccessListener {
                Utilis.downloadFile(this, fileName, ".$fileExtension",
                    Environment.DIRECTORY_DOWNLOADS, it)
            }
        }
    }


    /*
        Confirmation Dialog Display Yes / No Options
    */
    private fun confirmationDialog(title: String, description: String, callBack: () -> Unit) {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle(title)
        alertDialog.setMessage(description)

        alertDialog.setPositiveButton("Sim") { _, _ ->
            callBack()
        }

        alertDialog.setNegativeButton("Não") { _, _ ->
            alertDialog.show().dismiss()
        }

        alertDialog.show()
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
                    val filePath =
                        "chats/${chat_id}/messages/${Utilis.uniqueImageNameGen()}.${extension}"
                    val storageRef = FirebaseStorage.getInstance()
                        .getReference(filePath)

                    // compressing image
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 6, byteArrayOutputStream)
                    val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

                    storageRef.putBytes(reducedImage)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener {

                                //uploading image url
                                Utilis.uploadFile(result.uri, filePath)


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

        if (requestCode == 1 && resultCode == RESULT_OK) {
            var path = ""
            var fileName = ""

            val clipData = data?.clipData
            if (clipData == null) {

                var cursor: Cursor? = null
                try {
                    contentResolver.query(data?.data!!, null, null, null, null).use {

                        cursor = it

                        if (cursor != null && cursor!!.moveToFirst()) {

                            fileName = cursor!!.getString(cursor!!.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME).toInt())

                        }

                    }
                } finally {
                    cursor?.close()

                }

                path += data?.data.toString()
            } else {
                for (i in 0 until clipData.itemCount) {
                    val item = clipData.getItemAt(i)
                    val uri: Uri = item.uri
                    path += uri.toString() + "\n"
                }
            }

            CoroutineScope(Dispatchers.IO).launch {

                val filePath = "chats/$chat_id/messages/$fileName"
                val storageRef = FirebaseStorage.getInstance().getReference(filePath)

                //uploading image url
                Utilis.uploadFile(path.toUri(), filePath)

                getIv(chat_id) { iv ->

                    var meta = encryptMeta(filePath,
                        keygen,
                        iv.toString())

                    val message = Message(
                        Firebase.auth.currentUser!!.uid,
                        meta.toString(),
                        Timestamp.now(),
                        "file"
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
                                                        UserLoggedIn.name.toString()) + " enviou um ficheiro.",
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

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    inner class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        var otherUser = false
        var currentIndex = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            if (viewType == 0) {
                otherUser = false
                return ViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.row_message_system, parent, false))


            } else if (viewType <= 3) {
                otherUser = false

                if (viewType == 2) {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_message_self_image, parent, false))
                } else if (viewType == 3) {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_message_self_file, parent, false))
                } else {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_message_self, parent, false))
                }

            } else {
                otherUser = true
                if (viewType == 5) {
                    return ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_message_others_image, parent, false))
                } else if ((viewType == 6)) {
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

                val messageType = messages[position].files
                currentIndex = position + 1


                if (messageType == "") {

                    val textViewMessage = findViewById<TextView?>(R.id.textViewMessage)
                    val timeLastMessage = findViewById<TextView?>(R.id.timeLastMessage)
                    val textViewUsername = findViewById<TextView?>(R.id.textViewUsername)


                    timeLastMessage.visibility = View.INVISIBLE

                    textViewMessage.setOnLongClickListener {
                        copyTextToClipboard(textViewMessage)
                        true
                    }

                    textViewMessage.setOnClickListener {
                        timeLastMessage.visibility = View.VISIBLE
                    }


                    keygen = getMetaOx(context, chat_id).toString()
                    getIv(chat_id) {
                        val message_decripted = decryptWithAESmeta(keygen.toString(),
                            messages[position].message,
                            it.toString())

                        textViewMessage?.text = message_decripted
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

                            /*Backend.getUserProfile(messages[position].user) { user ->
                                textViewUsername.text = getFirstAndLastName(user.name)
                            }*/

                            if (imageViewUser != null) {
                                Utilis.getFile(context,
                                    "profilePictures/${messages[position].user}.png",
                                    "png") { bitmap ->
                                    imageViewUser.setImageBitmap(bitmap)

                                    imageViewUser.setOnLongClickListener {
                                        //messages[position].user
                                        openShortcut(bitmap,
                                            messages[position].user)
                                        true
                                    }
                                }
                            }
                        }
                    }
                } else if (messageType == "img") {

                    val imageView = findViewById<ImageView?>(R.id.imageViewSend)
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
                            Utilis.getFile(context,
                                message_decripted.toString(),
                                extension) { bitmap ->
                                imageView.setImageBitmap(bitmap)
                            }
                        }

                    }

                } else if (messageType == "file") {

                    getIv(chat_id) {


                        val timeLastMessage = findViewById<TextView?>(R.id.timeLastMessage2)
                        val downloadButton = findViewById<ImageView?>(R.id.imageViewSend2)

                        val message_decripted = decryptWithAESmeta(keygen.toString(),
                            messages[position].message,
                            it.toString())

                        val strArray = Pattern.compile("[/]").split(message_decripted)
                        val str = strArray[strArray.size - 1]
                        if (timeLastMessage != null)
                            timeLastMessage.text = str

                        downloadButton?.setOnClickListener {

                            val strArray = Pattern.compile("[/]").split(message_decripted)
                            val str = strArray[strArray.size - 1]

                            downloadFileRequest("chats/$chat_id/messages/", str)
                        }

                    }

                }

            }
        }

        override fun getItemViewType(position: Int): Int {

            // Self Message
            if (messages[position].user == Firebase.auth.currentUser!!.uid) {

                if (messages[position].files == "img")
                    return 2
                else if (messages[position].files == "file")
                    return 3
                else
                    return 1

            }
            // System Message Text
            else if (messages[position].user == "system") {
                return 0
            }
            // Other User Message
            else {

                if (messages[position].files == "img")
                    return 5
                else if (messages[position].files == "file")
                    return 6
                else
                    return 4

            }

        }

        override fun getItemCount(): Int {
            return messages.size
        }
    }

    private fun copyTextToClipboard(textCopy: TextView) {
        val textToCopy = textCopy.text
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
    }

    private fun openShortcut(image: Bitmap, userId: String) {

        // Variables
        val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
        val row = layoutInflater.inflate(R.layout.shortcut_manager_dialog, null)

        row.findViewById<ImageView>(R.id.imageViewProfileShorcut).setImageBitmap(image)

        Backend.getUserProfile(userId) {
            row.findViewById<TextView>(R.id.UserNameTextView).text = getFirstAndLastName(it.name)
        }


        row.findViewById<LinearLayout>(R.id.seeProfile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        row.findViewById<LinearLayout>(R.id.SendMessage).setOnClickListener {
            val intent = Intent(this, AddButtonActivity::class.java)
            startActivity(intent)
        }

        dialog.setContentView(row)
        dialog.show()

    }


}