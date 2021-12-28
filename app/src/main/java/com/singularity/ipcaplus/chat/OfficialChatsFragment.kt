package com.singularity.ipcaplus.chat

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.utils.Utilis
import com.singularity.ipcaplus.databinding.FragmentOfficialChatsBinding
import com.singularity.ipcaplus.models.Chat


class OfficialChatsFragment : Fragment() {

    // Variables
    var chats = arrayListOf<Chat>()
    var chatIds = arrayListOf<String>()

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

        // Get Oficial Chats
        db.collection("profile").document("${Firebase.auth.currentUser!!.uid}").collection("chat")
            .addSnapshotListener { documents, e ->
                documents?.let {
                    chats.clear()
                    chatIds.clear()
                    for (document in it) {
                        val chat = Chat.fromHash(document)
                        if (chat.type == "oficial") {
                            chats.add(chat)
                            chatIds.add(document.id)
                        }
                    }
                    mAdapter?.notifyDataSetChanged()
                }
            }

        // RecyclerView Chats
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

      inner class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_chat, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {


                holder.v.apply {

                    // Variables
                    val textViewMessage = findViewById<TextView>(R.id.textViewChatName)
                    val imageViewChatGroup = findViewById<ImageView>(R.id.imageViewChatGroup)
                    val lastMessageTime = findViewById<TextView>(R.id.lastMessageTime)
                    val lastMessageText = findViewById<TextView>(R.id.textViewLastMessage)

                    textViewMessage.text = chats[position].name
                    // Set Last Chat Message
                    Backend.getLastMessageByChatID(chatIds[position]) {
                        val data = Utilis.getDate(it!!.time.seconds *1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                        lastMessageTime.text = Utilis.getHours(data) + ":" + Utilis.getMinutes(data)
                        lastMessageText.text = it.message
                    }
                    imageViewChatGroup.setImageResource(R.drawable.common_full_open_on_phone)

                }
                holder.v.setOnClickListener {
                    val intent = Intent(activity, ChatActivity::class.java)
                    intent.putExtra("chat_id", chatIds[position])
                    activity?.startActivity(intent)
                }

        }

          override fun getItemViewType(position: Int) : Int {
              if (chats[position].type == "oficial") {
                  return 1
              } else {
                  return 0
              }
          }

        override fun getItemCount(): Int {
            return chats.size
        }
    }

}