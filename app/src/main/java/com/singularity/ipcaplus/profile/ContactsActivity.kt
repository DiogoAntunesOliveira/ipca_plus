package com.singularity.ipcaplus.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListAdapter
import com.singularity.ipcaplus.Backend
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivityContactsBinding
import com.singularity.ipcaplus.models.Contact

class ContactsActivity : AppCompatActivity() {

    lateinit var contactsAdapter: ExpandableListViewAdapter
    var contactNames: List<String> = ArrayList()
    var contactInfos: HashMap<String, List<Contact>> = HashMap()

    private lateinit var binding: ActivityContactsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        // Create the layout for this fragment
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
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

        }

    }

}