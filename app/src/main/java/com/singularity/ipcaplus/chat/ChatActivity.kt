package com.singularity.ipcaplus.chat

import android.annotation.SuppressLint
import android.app.ActionBar
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
import com.singularity.ipcaplus.utils.Backend.getIv
import com.singularity.ipcaplus.utils.Backend.getNotificationKey
import com.singularity.ipcaplus.utils.Utilis.sendNotificationToGroup
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
        findViewById<ImageView>(R.id.BackButtonImageView).setOnClickListener{
            finish()
        }

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


        val profilePicfromTop = findViewById<ImageView>(R.id.ProfileImageView)
        Utilis.getFile(this, "chats/$chat_id/icon.png", "png") { bitmap ->
            profilePicfromTop.setImageBitmap(bitmap)
        }

        println("Current Date is: $formatted")

        // Send Message
        binding.fabSend.setOnClickListener {
            if(!binding.editTextMessage.text.isNullOrBlank()) {
                // get data of ecripted shared preferences ("chatuid" -> "key")
                val keygen = getMetaOx(this, chat_id)
                // Build encryptation data of message send by the user
                getIv(chat_id) {

                    var meta = encryptMeta( binding.editTextMessage.text.toString(), keygen.toString(), it.toString())
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
                                withContext(Dispatchers.IO){
                                    //APA91bEKDInIYA242YofpahBmhB57pEI4gNT63DJJenWCccJGqeSYrWzj0BSruX49DhVp2vGSY5xJ2fEJk2vhtoraT3_bbjEKw4Nx3eJKj7tttVRPjQs0Uc_OPkrcj4twR70H5tAilnY
                                    // APA91bGaOoMTjTD2s9MU63F1AvLqP6tkwdAFE0Mqs9jbghlSgcWlfe_38CboFiE2iiWFoKqNRwhF0G_TA5X9xegTL0_Tg0OGuFadJuBj1sGZqjqCcmF1EH2ZeRU7ySHosdNkmLmmOyFF
                                    println("AVEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE"+ savedText)
                                    getNotificationKey(chat_id){
                                        GlobalScope.launch {
                                            withContext(Dispatchers.IO){
                                                sendNotificationToGroup( chat_id, savedText, it.toString())
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

        db.collection("profile")
            .document(Firebase.auth.currentUser!!.uid)
            .collection("chat")
            .addSnapshotListener { documents, e ->
                documents?.let {
                    for (document in it) {
                        if(document.id == chat_id) {
                            val chat = Chat.fromHash(document)
                            var name = chat.name
                            if (chat.type == "chat") {
                                name  = Utilis.getFirstAndLastName(chat.name)
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
                intent.putExtra("chat_name",  findViewById<TextView>(R.id.AppBarTittle).text.toString())
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
                otherUser = false
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
                val textViewUsername = findViewById<TextView?>(R.id.textViewUsername)


                timeLastMessage?.isVisible = false
                val keygen = getMetaOx(context, chat_id)
                getIv(chat_id){
                    val message_decripted = decryptWithAESmeta(keygen.toString(), messages[position].message, it.toString())

                    /* if (otherUser) {
                             Backend.getUserProfile(messages[position].user) {
                                 val userName = Utilis.getFirstAndLastName(it.name)
                                 textViewUsername.text = userName
                             }
                         }
                     */

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