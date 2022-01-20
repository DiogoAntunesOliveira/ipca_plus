package com.singularity.ipcaplus.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

class Subject {

    var name: String = ""
    var teacher: String = ""

    constructor(name: String, teacher: String) {
        this.name = name
        this.teacher = teacher
    }

    fun toHash(): HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("name", name)
        hashMap.put("teacher", teacher)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): Subject {
            val subject = Subject(
                hashMap["name"] as String,
                hashMap["teacher"] as String
            )
            return subject
        }
    }
}