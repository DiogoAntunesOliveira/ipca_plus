package com.singularity.ipcaplus.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class Chat {
    var name : String = ""
    var type : String = ""
    var ox : String? = ""
    var id : String? =  null
    var iv : String? = ""
    var notificationKey : String? = ""

    constructor(name : String, type : String, ox : String, iv : String, notificationKey : String) {
        this.name = name
        this.type = type
        this.ox = ox
        this.iv = iv
        this.notificationKey = notificationKey
    }

    fun toHash() :HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("name", name)
        hashMap.put("type", type)
        hashMap.put("ox", ox!!)
        hashMap.put("iv", iv!!)
        hashMap.put("notificationKey", notificationKey!!)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : Chat {
            val chat = Chat(
                hashMap["name"] as String,
                hashMap["type"] as String,
                hashMap["ox"] as String,
                hashMap["iv"] as String,
                hashMap["notificationKey"] as String

            )
            return chat
        }
    }
}