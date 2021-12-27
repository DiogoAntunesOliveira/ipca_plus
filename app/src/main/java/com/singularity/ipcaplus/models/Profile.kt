package com.singularity.ipcaplus.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class Profile {
    var name : String = ""
    var studentNumber : String = ""
    var contact : String = ""
    var course : String = ""
    var year : String = ""
    var age : Int = 0
    var id : String? =  null

    constructor(user: String, studentNumber: String, contact: String, course: String, year: String, age : Int) {
        this.name = user
        this.studentNumber = studentNumber
        this.contact = contact
        this.course = course
        this.year = year
        this.age = age
    }

    fun toHash() :HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("name", name)
        hashMap.put("studentNumber", studentNumber)
        hashMap.put("contact", contact)
        hashMap.put("course", course)
        hashMap.put("year", year)
        hashMap.put("age", age)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : Profile {
            val profile = Profile(
                hashMap["name"] as String,
                hashMap["studentNumber"] as String,
                hashMap["contact"] as String,
                hashMap["course"] as String,
                hashMap["year"] as String,
                hashMap["age"] as Int
            )
            return profile
        }
    }
}