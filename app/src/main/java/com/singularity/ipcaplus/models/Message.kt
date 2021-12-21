package com.singularity.ipcaplus.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

class Message {

    var user : String = ""
    var message : String = ""
    var date : String = ""
    var time :  Timestamp
    var files : String = ""
    var id : String? =  null

    constructor(user: String, message: String, date: String, time: Timestamp, files: String) {
        this.user = user
        this.message = message
        this.date = date
        this.time = time
        this.files = files
    }

    fun toHash() :HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("user", user)
        hashMap.put("message", message)
        hashMap.put("date", date)
        hashMap.put("time", time)
        hashMap.put("files", files)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : Message {
            val message = Message(
                hashMap["user"] as String,
                hashMap["message"] as String,
                hashMap["date"] as String,
                hashMap["time"] as Timestamp,
                hashMap["files"] as String
            )
            return message
        }
    }
}