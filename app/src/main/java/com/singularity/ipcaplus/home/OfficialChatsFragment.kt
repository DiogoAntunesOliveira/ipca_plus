package com.singularity.ipcaplus.home

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.ChatActivity
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.FragmentChatsBinding
import com.singularity.ipcaplus.databinding.FragmentOfficialChatsBinding
import com.singularity.ipcaplus.models.Chat


class OfficialChatsFragment : Fragment() {

    var chats = arrayListOf<Chat>()
    var chatIds = arrayListOf<String>()
    var user_groups = arrayListOf<String>()

    private var _binding: FragmentOfficialChatsBinding? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null

    private val binding get() = _binding!!

    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOfficialChatsBinding.inflate(layoutInflater)
        val root: View = binding.root

        db.collection("profile")
            .document("${Firebase.auth.currentUser!!.uid}")
            .collection("chat")
            .addSnapshotListener { documents, e ->

                documents?.let {
                    chats.clear()
                    chatIds.clear()
                    for (document in it) {
                        Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                        val chat = Chat.fromHash(document)
                        chatIds.add(document.id)
                        chats.add(chat)

                    }
                    mAdapter?.notifyDataSetChanged()
                }

            }

        mLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewGroups.layoutManager = mLayoutManager
        mAdapter = ChatAdapter()
        binding.recyclerViewGroups.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewGroups.adapter = mAdapter


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
       This function configures the fragment after its creation
    */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


      inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_chat, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {


            holder.v.apply {
                val textViewMessage = findViewById<TextView>(R.id.textViewChatName)
                val imageViewChatGroup = findViewById<ImageView>(R.id.imageViewChatGroup)


                textViewMessage.text = chats[position].name
                imageViewChatGroup.setImageResource(R.drawable.common_full_open_on_phone)
            }
            holder.v.setOnClickListener {
                val intent = Intent(activity, ChatActivity::class.java)
                intent.putExtra("chat_id", chatIds[position])
                activity?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return chats.size
        }
    }

}