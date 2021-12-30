package com.singularity.ipcaplus.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class Profile {
    var name : String = ""
    var studentNumber : String = ""
    var gender : String = ""
    var contact : String = ""
    var course : String = ""
    var courseTag : String = ""
    var age : String = ""
    var role : String = ""
    var id : String? =  null

    constructor() {

    }

    constructor(age: String, contact: String, course: String, courseTag: String, gender: String, name : String, role : String, studentNumber : String) {
        this.name = name
        this.gender = gender
        this.studentNumber = studentNumber
        this.contact = contact
        this.course = course
        this.courseTag = courseTag
        this.age = age
        this.role = role
    }

    fun toHash() :HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap.put("age", age)
        hashMap.put("contact", contact)
        hashMap.put("course", course)
        hashMap.put("course_tag", courseTag)
        hashMap.put("gender", gender)
        hashMap.put("name", name)
        hashMap.put("role", role)
        hashMap.put("student_number", studentNumber)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : Profile {

            val profile = Profile(
                hashMap["age"] as String,
                hashMap["contact"] as String,
                hashMap["course"] as String,
                hashMap["course_tag"] as String,
                hashMap["gender"] as String,
                hashMap["name"] as String,
                hashMap["role"] as String,
                hashMap["student_number"] as String
            )
            return profile
        }
    }
}