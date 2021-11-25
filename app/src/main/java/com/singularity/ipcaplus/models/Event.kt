package com.singularity.ipcaplus.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class Event {
    var id : String? =  null
    var name : String = ""
    var desc : String = ""
    var day : String = ""
    var hour : String = ""

    constructor() {

    }

    constructor(
        name : String,
        desc : String,
        day : String,
        hour : String,
    ) {
        this.name = name
        this.desc = desc
        this.day = day
        this.hour = hour
    }

    fun toHash() :HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("name", name)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : Chat {
            val chat = Chat(
                hashMap["name"] as String
            )
            return chat
        }
    }
}