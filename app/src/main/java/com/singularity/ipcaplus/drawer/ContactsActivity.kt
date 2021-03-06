package com.singularity.ipcaplus.drawer

import android.annotation.SuppressLint
import android.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.singularity.ipcaplus.utils.ExpandableListViewAdapter
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivityContactsBinding
import com.singularity.ipcaplus.models.Contact
import com.singularity.ipcaplus.utils.Backend

class ContactsActivity : AppCompatActivity() {

    lateinit var contactsAdapter: ExpandableListViewAdapter
    var contactNames: List<String> = ArrayList()
    var contactInfos: HashMap<String, List<Contact>> = HashMap()

    private lateinit var binding: ActivityContactsBinding

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stay)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_bar_layout)

        findViewById<TextView>(R.id.AppBarTittle).text = "Contactos"
        // Back button
        findViewById<ImageView>(R.id.BackButtonImageView).setOnClickListener {
            finish()
        }

        // Create the layout for this fragment
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create Action Bar
        supportActionBar?.title = "Contactos"

        // Show List
        getData()

        // Set up Adapter
        contactsAdapter = ExpandableListViewAdapter(this, contactNames, contactInfos)
        binding.expandableListViewContacts.setAdapter(contactsAdapter)
    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun getData() {
        Backend.getAllContacts {

            for (item in it) {

                (contactNames as ArrayList<String>).add(item.name)

                val info: MutableList<Contact> = ArrayList()
                info.add(item)

                contactInfos[item.name] = info

            }

            contactsAdapter.notifyDataSetChanged()
        }

    }

}