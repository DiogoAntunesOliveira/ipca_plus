package com.singularity.ipcaplus.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

class Subject {

    var name : String = ""
    var day : String = ""
    var start_time : String = ""
    var end_time : String = ""
    var classroom : String = ""
    var teacher : String = ""

    constructor(name : String, start_time: String) {
        this.name = name
        this.start_time = start_time
    }

    constructor(
        classroom : String,
        day : String,
        end_time : String,
        name : String,
        start_time : String,
        teacher : String
    ) {
        this.classroom = classroom
        this.day = day
        this.end_time = end_time
        this.name = name
        this.start_time = start_time
        this.teacher = teacher
    }

    fun toHash() :HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("classroom", classroom)
        hashMap.put("day", day)
        hashMap.put("end_time", end_time)
        hashMap.put("name", name)
        hashMap.put("start_time", start_time)
        hashMap.put("teacher", teacher)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : Subject {
            val subject = Subject(
                hashMap["classroom"] as String,
                hashMap["day"] as String,
                hashMap["end_time"] as String,
                hashMap["name"] as String,
                hashMap["start_time"] as String,
                hashMap["teacher"] as String
            )
            return subject
        }
    }
}