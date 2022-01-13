package com.singularity.ipcaplus.chat

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.AddPeopleActivity
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivityCalendarBinding
import com.singularity.ipcaplus.databinding.ActivityChatMembersBinding
import com.singularity.ipcaplus.databinding.ActivityChatMoreBinding
import com.singularity.ipcaplus.drawer.ProfileActivity
import com.singularity.ipcaplus.models.EventCalendar
import com.singularity.ipcaplus.models.Profile
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Utilis

class ChatMembersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatMembersBinding

    var currentUserIsAdmin = false
    var members = arrayListOf<Profile>()
    var selectedUser = ""
    private lateinit var chat_id: String
    private var membersAdapter: RecyclerView.Adapter<*>? = null
    private var membersLayoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_members)

        // Create the layout for this fragment
        binding = ActivityChatMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get previous data
        chat_id = intent.getStringExtra("chat_id").toString()
        currentUserIsAdmin = intent.getBooleanExtra("is_admin", false)

        // Get Users in chat
        refreshList()

        // List
        membersLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.membersRecyclerView.layoutManager = membersLayoutManager
        membersAdapter = EventAdapter()
        binding.membersRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.membersRecyclerView.adapter = membersAdapter
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        Backend.getChatUsers(chat_id) {
            members.clear()
            members.addAll(it)
            membersAdapter?.notifyDataSetChanged()
        }
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

        if (!currentUserIsAdmin)
            return false

        return true
    }


    /*
        This function define the events of the action bar buttons
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId){
            R.id.add -> {
                val intent = Intent(this, AddPeopleActivity::class.java)
                intent.putExtra("chat_id", chat_id)
                startActivity(intent)
                println("-------------------> adicionar")
                return true
            }
        }
        return false
    }


    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        selectedUser = v.findViewById<TextView>(R.id.userId).text.toString()
        val isAdmin = v.findViewById<TextView>(R.id.isAdmin).text.toString()

        if (isAdmin == "1")
            menu.add(0, v.id, 0, "Atribuir admin")
        else
            menu.add(0, v.id, 0, "Remover admin")

        menu.add(0, v.id, 0, "Remover do grupo")
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {

        if (item.title == "Atribuir admin") {
            Backend.changeUserChatAdminStatus(chat_id, selectedUser, true)
        }
        else if (item.title == "Remover admin") {
            Backend.changeUserChatAdminStatus(chat_id, selectedUser, false)
        }
        else if (item.title == "Remover do grupo") {
            Backend.removeUserFromChat(chat_id, selectedUser)
            println("-------------------> remover $selectedUser")
        }

        refreshList()

        return true
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

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {

                // Get data
                val imageViewProfile = findViewById<ImageView>(R.id.imageViewProfile)
                val imageViewThreePoints = findViewById<ImageView>(R.id.imageViewThreePoints)
                val textViewNomeUser = findViewById<TextView>(R.id.textViewNomeUser)
                val textViewAdminTag = findViewById<TextView>(R.id.textViewAdminTag)
                val rowMoreOptions = findViewById<ConstraintLayout>(R.id.rowMoreOptions)
                val userId = findViewById<TextView>(R.id.userId)
                val isAdmin = findViewById<TextView>(R.id.isAdmin)

                // Set data
                Utilis.getFile(context, "profilePictures/${members[position].id}.png", "png") { bitmap ->
                    imageViewProfile.setImageBitmap(bitmap)
                }

                textViewNomeUser.text = Utilis.getFirstAndLastName(members[position].name)
                userId.text = members[position].id
                isAdmin.text = if (members[position].isAdmin) "1" else "0"

                if (members[position].isAdmin) {
                    textViewAdminTag.visibility = View.GONE
                }
                else {
                    textViewAdminTag.visibility = View.VISIBLE
                }

                if (currentUserIsAdmin) {

                    rowMoreOptions.setOnClickListener {
                        registerForContextMenu(it)
                        it.showContextMenu(100f, 50f)
                    }
                }
                else {
                    imageViewThreePoints.visibility = View.GONE
                }

            }

            holder.v.setOnClickListener {
                val intent = Intent(this@ChatMembersActivity, ProfileActivity::class.java)
                intent.putExtra("userId", members[position].id)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return members.size
        }
    }
}