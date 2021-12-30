package com.singularity.ipcaplus.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivityCalendarBinding
import com.singularity.ipcaplus.databinding.ActivityChatMembersBinding
import com.singularity.ipcaplus.databinding.ActivityChatMoreBinding
import com.singularity.ipcaplus.models.EventCalendar
import com.singularity.ipcaplus.models.Profile
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Utilis

class ChatMembersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatMembersBinding

    var members = arrayListOf<Profile>()
    private var membersAdapter: RecyclerView.Adapter<*>? = null
    private var membersLayoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_members)

        // Create the layout for this fragment
        binding = ActivityChatMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Membros"

        // Get previous data
        val chat_id = intent.getStringExtra("chat_id").toString()

        // Get Users in chat
        Backend.getChatUsers(chat_id) {

        }
        members.add(Profile())

        // List
        membersLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.membersRecyclerView.layoutManager = membersLayoutManager
        membersAdapter = EventAdapter()
        binding.membersRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.membersRecyclerView.adapter = membersAdapter
    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    inner class EventAdapter : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_user_in_chat, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {

                // Get data
                val imageViewUser = findViewById<ImageView>(R.id.imageViewUser)
                val textViewNomeUser = findViewById<TextView>(R.id.textViewNomeUser)
                val textViewAdminTag = findViewById<TextView>(R.id.textViewAdminTag)

                // Set data
                // println("-------------------> " + members[position].id)
                textViewNomeUser.text = members[position].name

                val isAdmin = true
                if (!isAdmin)
                    textViewAdminTag.visibility = View.GONE

            }
        }

        override fun getItemCount(): Int {
            return members.size
        }
    }
}