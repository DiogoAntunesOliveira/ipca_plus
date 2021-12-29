package com.singularity.ipcaplus

import android.content.Intent
import android.graphics.drawable.Icon
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.databinding.ActivityAddPeopleBinding
import com.singularity.ipcaplus.databinding.ActivitySearchBinding
import com.singularity.ipcaplus.models.Chat

class AddPeopleActivity: AppCompatActivity() {

    var users = arrayListOf<Chat>()
    var selectedUsers = arrayListOf<String>()

    private lateinit var binding: ActivityAddPeopleBinding

    private var userAdapter: RecyclerView.Adapter<*>? = null
    private var userSelectedAdapter: RecyclerView.Adapter<*>? = null
    private var userLayoutManager: LinearLayoutManager? = null


    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_people)
        binding = ActivityAddPeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val back = findViewById<ImageView>(R.id.back_btn)

        back.setOnClickListener(){
            finish()
        }

        // Get Users



        // RecyclerView Users



       
    }


    inner class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_search, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {
                // Variables



                // Set data

            }
            holder.v.setOnClickListener {

            }

        }

        override fun getItemCount(): Int {
            return users.size
        }
    }
}