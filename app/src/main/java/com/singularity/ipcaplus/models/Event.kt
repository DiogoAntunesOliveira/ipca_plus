package com.singularity.ipcaplus.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class Event {
    var id : String? =  null
    var name : String = ""
    var desc : String = ""
    var month: String = ""
    var day : String = ""
    var hour : String = ""

    constructor() {

    }

    constructor(
        name : String,
        desc : String,
        month : String,
        day : String,
        hour : String,
    ) {
        this.name = name
        this.desc = desc
        this.month = month
        this.day = day
        this.hour = hour
    }

    fun toHash() :HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("name", name)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : Event {
            val event = Event(
                hashMap["name"] as String,
                hashMap["desc"] as String,
                hashMap["month"] as String,
                hashMap["day"] as String,
                hashMap["hour"] as String
            )
            return event
        }
    }
}