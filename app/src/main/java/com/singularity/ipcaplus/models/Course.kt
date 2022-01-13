package com.singularity.ipcaplus.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class Course {

    var name : String = ""
    var tag : String = ""

    constructor(name : String, tag : String) {
        this.name = name
        this.tag = tag
    }

    fun toHash() :HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("name", name)
        hashMap.put("tag", tag)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : Course {
            val course = Course(
                hashMap["name"] as String,
                hashMap["tag"] as String
            )
            return course
        }
    }

}