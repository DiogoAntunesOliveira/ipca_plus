package com.singularity.ipcaplus.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class Chat {
    var chat_name : String = ""
    var id : String? =  null

    constructor(chat_name : String) {
        this.chat_name = chat_name
    }

    fun toHash() :HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("chat_name", chat_name)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : Chat {
            val chat = Chat(
                hashMap["chat_name"] as String
            )
            return chat
        }
    }
}