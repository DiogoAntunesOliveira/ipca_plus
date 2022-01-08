package com.singularity.ipcaplus.chat

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.drawer.CalendarActivity
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.utils.Utilis
import com.singularity.ipcaplus.cryptography.decryptWithAESmeta
import com.singularity.ipcaplus.cryptography.encryptMeta
import com.singularity.ipcaplus.cryptography.getMetaOx
import com.singularity.ipcaplus.databinding.ActivityChatBinding
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Message
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Backend.createJsonArrayString
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection

class ChatActivity : AppCompatActivity() {

    var messages = arrayListOf<Message>()
    var currentUserIsAdmin = false
    private lateinit var binding: ActivityChatBinding
    private lateinit var chat_id : String
    var tokens_adress = arrayListOf<String>()
    var chat_user_uids = arrayListOf<String>()
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null

    val db = Firebase.firestore
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Variables
        chat_id = intent.getStringExtra("chat_id").toString()
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val formatted = current.format(formatter)

        // Check if user is admin
        Backend.getChatAdminIds(chat_id) {
            val currentUser = Firebase.auth.currentUser!!.uid
            for (admin in it) {
                if (admin == currentUser)
                    currentUserIsAdmin = true
            }
        }

        // Get all members id of chat
        Backend.getChatUsersUids(chat_id){
            chat_user_uids.clear()
            chat_user_uids.addAll(it)

            for (userId in chat_user_uids){
                // Getting all of tokens of  profile associated devices
                Backend.getAllTokens(userId) {
                    if (tokens_adress.isEmpty()){
                        tokens_adress.clear()
                    }
                    tokens_adress.addAll(it)

                    GlobalScope.launch {
                        withContext(Dispatchers.IO){
                            createNotificationGroup("otorrinolaringoloigista", createJsonArrayString(tokens_adress))
                        }
                    }

                }

            }

        }

        println("Current Date is: $formatted")

        // Send Message
        binding.fabSend.setOnClickListener {
            if(!binding.editTextMessage.text.isNullOrBlank()) {
                // get data of ecripted shared preferences ("chatuid" -> "key")
                val keygen = getMetaOx(this, chat_id)
                // Build encryptation data of message send by the user
                var meta = encryptMeta( binding.editTextMessage.text.toString(), keygen.toString())

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
                            withContext(Dispatchers.IO){
                                //APA91bEKDInIYA242YofpahBmhB57pEI4gNT63DJJenWCccJGqeSYrWzj0BSruX49DhVp2vGSY5xJ2fEJk2vhtoraT3_bbjEKw4Nx3eJKj7tttVRPjQs0Uc_OPkrcj4twR70H5tAilnY
                                sendNotificationToGroup( Firebase.auth.currentUser!!.uid, "Depois temos de mudar", "APA91bHFEOWkBe0cf6ZBdBqDiZ1WzYf--PmimY3tWzDlhAPWZ3UL1eSCSuO7nVt5k3i-2FNg6U5d6NoR767JXg9W_NT9lo4u3kiuT46w79LXjcK63dXUzyzIZEzac9olNNb2_siLHA-K")
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

        // Show Messages
        db.collection("chat").document("$chat_id").collection("message").orderBy("time", Query.Direction.DESCENDING)
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

        db.collection("chat")
            .addSnapshotListener { documents, e ->
                documents?.let {
                    for (document in it) {
                        if(document.id == chat_id) {
                            val chat = Chat.fromHash(document)
                            supportActionBar?.title = chat.name
                        }
                    }
                }
            }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)


        return true
    }


    /*
        This function define the events of the action bar buttons
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId){
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
                intent.putExtra("chat_name", supportActionBar?.title)
                startActivity(intent)
                return true
            }
        }

        return false
    }


    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    inner class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        var otherUser = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            if(viewType == 1) {
                otherUser = false
                return ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_message_self, parent, false))
            } else if (viewType == 2){
                return ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_message_system, parent, false))
            } else {
                otherUser = true
                return ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_message_others, parent, false))
            }

        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {


            holder.v.apply {

                val textViewMessage = findViewById<TextView>(R.id.textViewMessage)
                val timeLastMessage = findViewById<TextView?>(R.id.timeLastMessage)


                timeLastMessage?.isVisible = false
                val keygen = getMetaOx(context, chat_id)
                print( getMetaOx(context, chat_id)).toString()
                val message_decripted = decryptWithAESmeta(keygen.toString(), messages[position].message)
                textViewMessage.text = message_decripted
                println(message_decripted)
                if(position == messages.size - 1) {
                    val data = Utilis.getDate(
                        messages[position].time.seconds * 1000,
                        "yyyy-MM-dd'T'HH:mm:ss.SSS"
                    )
                    timeLastMessage.isVisible = true
                    timeLastMessage.text = Utilis.getHours(data) + ":" + Utilis.getMinutes(data)
                }

                if (otherUser) {
                    val imageViewUser = findViewById<ImageView?>(R.id.imageViewUser)
                    if (imageViewUser != null) {
                        Utilis.getFile(context, "profilePictures/${messages[position].user}.png", "png") { bitmap ->
                            imageViewUser.setImageBitmap(bitmap)
                        }
                    }
                }

            }
        }

        override fun getItemViewType(position: Int) : Int {
            if (messages[position].user == Firebase.auth.currentUser!!.uid) {
                return 1
            } else if (messages[position].user == "system"){
                return 2
            } else {
                return 0
            }
        }

        override fun getItemCount(): Int {
            return messages.size
        }


    }

    private suspend fun  createNotificationGroup(notificationKeyName : String, registrationIds : JSONArray)  {

        try {

            Log.d("json", registrationIds.toString())

            //The url of the API i want to access (Firebase Cloud Messaging)
            val endPoint = URL("https://fcm.googleapis.com/fcm/notification")

            //Establish a connection to fcm (Firebase Cloud Messaging) so i can send a push notification to a specific topic
            val httpsURLConnection: HttpsURLConnection =
                endPoint.openConnection() as HttpsURLConnection

            //Here i configure the connection to fcm
            //The time available to read from the input stream when the connection is established
            httpsURLConnection.readTimeout = 10000
            //The time available to connect to the url
            httpsURLConnection.connectTimeout = 15000
            //Defining the type of request to be made to the fcm
            httpsURLConnection.requestMethod = "POST"
            //Defining that the url connection can be used to send and receive data
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            //Here i give my server key so i can make a request to fcm (Firebase Cloud Messaging) of my application (FirebaseDemo)
            //and i define as well the type of content that i will be sending (json object)
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")
            val project_key = "AAAAMMR-Gaw:APA91bFeijRa909_QEdEFsQeDSaJZRYD7rOk8B8Bc2QiYcGoyLG1xqqpZLkOJXmZrG0FbScojvqBCsweSEWDrMLM6kr67boS-BVB2oy7fL6Zn1N9ICVk6efGniauDa3z8eaOb1TENmEs"
            val senderId = "209455028652"
            httpsURLConnection.setRequestProperty("authorization", "key=$project_key")
            httpsURLConnection.setRequestProperty("project_id", senderId)

            val json = JSONObject()

            //Here i need to verify if the chat has already been created
            //----------------------------------------------------------

            //Here i define the name of the group "chatName" and
            //the fcm tokens of the users that are going to be in the group "registrationIds"
            json.put("operation", "create")
            // GROUP NAME
            json.put("notification_key_name", notificationKeyName)
            // TOKENS OF FCM PROFILES LIST
            json.put("registration_ids", registrationIds)

            //json.put("notification_key_name", "AM_7")

            val outputStream: OutputStream =
                BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))

            //here i write the body of the post request and then i send the request (flush)
            //then i close the post request
            writer.write(json.toString())
            writer.flush()
            writer.close()

            outputStream.close()

            //The response code and message of the post requests
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage = httpsURLConnection.responseMessage

            Log.d(TAG, "$responseCode $responseMessage")


            // Check if the connection is successful
            if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }

            if (responseCode == 200) {
                Log.e(TAG, "Group Created!!")

                val response = httpsURLConnection.inputStream.bufferedReader()
                    .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {
                    // Convert raw JSON to pretty JSON using GSON library

                    //Here i get the notification_key that has been defined to the group that got created
                    val jsonObject  = JSONObject(response)
                    val notifKey = jsonObject.getString("notification_key")
                    println("NotifKey:")
                    println(notifKey)
                }
            } else {
                Log.e(TAG, "Error it didn´t work")
            }

            //Here i close the connection to the endPoint
            httpsURLConnection.disconnect()


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //This function sends push notifications to devices that are subscribed to a specific topic
    private suspend fun sendNotificationToGroup(title: String, message: String, notificationKey : String) {

        delay(1500)

        try {

            //The url of the API i want to access (Firebase Cloud Messaging)
            val url = URL("https://fcm.googleapis.com/fcm/send")

            //Establish a connection to fcm (Firebase Cloud Messaging) so i can send a push notification to a specific topic
            val httpsURLConnection: HttpsURLConnection =
                url.openConnection() as HttpsURLConnection

            //Here i configure the connection to fcm
            //The time available to read from the input stream when the connection is established
            httpsURLConnection.readTimeout = 10000
            //The time available to connect to the url
            httpsURLConnection.connectTimeout = 15000
            //Defining the type of request to be made to the fcm
            httpsURLConnection.requestMethod = "POST"
            //Defining that the url connection can be used to send and receive data
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            //Here i give my server key so i can make a request to fcm (Firebase Cloud Messaging) of my application (FirebaseDemo)
            //and i define as well the type of content that i will be sending (json)
            val project_key = "AAAAMMR-Gaw:APA91bFeijRa909_QEdEFsQeDSaJZRYD7rOk8B8Bc2QiYcGoyLG1xqqpZLkOJXmZrG0FbScojvqBCsweSEWDrMLM6kr67boS-BVB2oy7fL6Zn1N9ICVk6efGniauDa3z8eaOb1TENmEs"
            httpsURLConnection.setRequestProperty("authorization", "key=$project_key")
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")

            val body = JSONObject()
            val data = JSONObject()

            data.put("title", title)
            data.put("content", message)
            //Here i define the Activity in witch i want the user to navigate when they click the notification
            data.put("click_action", ".LoginActivity")
            //data.put("chat_id", "S77po7vNGjtKja2Rinyb")

            //val condition = "'$TOPIC1' in topics && '$TOPIC2' in topics && '$TOPIC3' in topics"
            //body.put("condition", condition)

            //here i define the body of the post request
            body.put("data", data)
            //Here i define the group via notification key in which i want to send the notification/message
            body.put("to", notificationKey)

            val outputStream: OutputStream =
                BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))

            //here i write the body of the post request and then i send the request
            //then i close the post request
            writer.write(body.toString())
            writer.flush()
            writer.close()

            outputStream.close()

            //The response code and message of the post requests
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage: String = httpsURLConnection.responseMessage


            Log.d(TAG, "Response from sendMes: $responseCode $responseMessage")


            // Check if the connection is successful or not
            if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }
            if (responseCode == 200) {
                Log.e(
                    TAG,
                    "Notification Sent \n Title: $title \n Body: $message"
                )
            } else {
                Log.e(TAG, "Notification Error")
            }

            httpsURLConnection.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}