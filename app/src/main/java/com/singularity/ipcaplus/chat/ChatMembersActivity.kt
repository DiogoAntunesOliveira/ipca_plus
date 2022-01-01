package com.singularity.ipcaplus.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
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

        // Get previous data
        val chat_id = intent.getStringExtra("chat_id").toString()

        // Get Users in chat
        Backend.getChatUsers(chat_id) {
            members.clear()
            members.addAll(it)
            membersAdapter?.notifyDataSetChanged()
        }

        // List
        membersLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.membersRecyclerView.layoutManager = membersLayoutManager
        membersAdapter = EventAdapter()
        binding.membersRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.membersRecyclerView.adapter = membersAdapter
    }

    /*
    This function create the action bar above the activity
    */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_add, menu)

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Membros"

        return true
    }


    /*
        This function define the events of the action bar buttons
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId){
            R.id.add -> {
                println("-------------------> adicionar")
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
                val imageViewProfile = findViewById<ImageView>(R.id.imageViewProfile)
                val textViewNomeUser = findViewById<TextView>(R.id.textViewNomeUser)
                val textViewAdminTag = findViewById<TextView>(R.id.textViewAdminTag)

                // Set data
                Utilis.getFile("profilePictures/${members[position].id}.png", "png") { bitmap ->
                    imageViewProfile.setImageBitmap(bitmap)
                }

                textViewNomeUser.text = Utilis.getFirstAndLastName(members[position].name)

                if (members[position].isAdmin)
                    textViewAdminTag.visibility = View.GONE

            }
        }

        override fun getItemCount(): Int {
            return members.size
        }
    }
}