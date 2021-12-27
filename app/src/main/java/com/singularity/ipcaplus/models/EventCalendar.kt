package com.singularity.ipcaplus.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

class EventCalendar {

    var id: String = ""
    var name : String = ""
    var desc : String = ""
    lateinit var datetime: Timestamp

    constructor() {

    }

    constructor(
        datetime : Timestamp,
        desc : String,
        name : String
    ) {
        this.datetime = datetime
        this.desc = desc
        this.name = name
    }

    fun toHash() :HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("datetime", datetime)
        hashMap.put("desc", desc)
        hashMap.put("name", name)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : EventCalendar {
            val event = EventCalendar(
                hashMap["datetime"] as Timestamp,
                hashMap["desc"] as String,
                hashMap["name"] as String
            )
            return event
        }
    }
}