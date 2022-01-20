package com.singularity.ipcaplus.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class SubjectClass {

    var name: String = ""
    var teacher: String = ""
    var day: String = ""
    var start_time: String = ""
    var end_time: String = ""
    var classroom: String = ""

    constructor(day: String, start_time: String, end_time: String, classroom: String) {
        this.day = day
        this.start_time = start_time
        this.end_time = end_time
        this.classroom = classroom
    }

    constructor(name: String, start_time: String) {
        this.name = name
        this.start_time = start_time
    }

    constructor() {
    }

    fun toHash(): HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("day", day)
        hashMap.put("start_time", start_time)
        hashMap.put("end_time", end_time)
        hashMap.put("classroom", classroom)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): SubjectClass {
            val subjectClass = SubjectClass(
                hashMap["day"] as String,
                hashMap["start_time"] as String,
                hashMap["end_time"] as String,
                hashMap["classroom"] as String
            )
            return subjectClass
        }
    }

}