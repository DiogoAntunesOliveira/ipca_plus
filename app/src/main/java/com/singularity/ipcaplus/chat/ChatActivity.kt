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
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.singularity.ipcaplus.AppConstants
import com.singularity.ipcaplus.drawer.CalendarActivity
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.utils.Utilis
import com.singularity.ipcaplus.cryptography.decryptWithAESmeta
import com.singularity.ipcaplus.cryptography.encryptMeta
import com.singularity.ipcaplus.cryptography.getMetaOx
import com.singularity.ipcaplus.databinding.ActivityChatBinding
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Message
import com.singularity.ipcaplus.models.PushNotification
import com.singularity.ipcaplus.utils.Backend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatActivity : AppCompatActivity() {

    var messages = arrayListOf<Message>()
    var currentUserIsAdmin = false
    private lateinit var binding: ActivityChatBinding
    private lateinit var chat_id : String
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

        println("Current Date is: $formatted")

        // Send Message
        binding.fabSend.setOnClickListener {
            if(!binding.editTextMessage.text.isNullOrBlank()) {
                // get data of ecripted shared preferences ("chatuid" -> "key")
                val keygen = getMetaOx(this, chat_id)
                // Build encryptation data of message send by the user
                var meta = encryptMeta( binding.editTextMessage.text.toString(), keygen.toString())

                var databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chat_id!!)


                val message = Message(
                    Firebase.auth.currentUser!!.uid,
                    meta.toString(),
                    Timestamp.now(),
                    ""

                )

                //getToken(binding.editTextMessage.text.toString())

                db.collection("chat").document("$chat_id").collection("message")
                    .add(message.toHash())
                    .addOnSuccessListener { documentReference ->
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
    }











private fun getToken(message: String) {

    val databaseReference = FirebaseDatabase
        .getInstance()
        .getReference("Profile")
        .child("bEWfuAdTu3bfhHgOaOFkynIgHjH3")

    databaseReference
        .addListenerForSingleValueEvent(object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val token = snapshot.child("token")
                    .value.toString()

                val to = JSONObject()
                val data = JSONObject()

                data.put("hisId", "Y90PjGQmLsMrxLicWkirOKpPSOx2")
                data.put("title", "Bruce Wayne")
                data.put("message", message)
                data.put("chatId", "CbartuF280ajG8nOAc5L")

                to.put("to", token)
                to.put("data", data)
                sendNotification(to)


            }
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }
    })
}

    private fun sendNotification(to: JSONObject) {

        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            AppConstants.NOTIFICATION_URL,
            to,
            Response.Listener { response: JSONObject ->

                Log.d("TAG", "onResponse: $response")
            },
            Response.ErrorListener {

                Log.d("TAG", "onError: $it")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val map: MutableMap<String, String> = HashMap()

                map["Authorization"] = "key=" + AppConstants.SERVER_KEY
                map["Content-type"] = "application/json"
                return map
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

    }