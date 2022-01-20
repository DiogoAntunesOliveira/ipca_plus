package com.singularity.ipcaplus.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class Contact {

    var desc: String = ""
    var email: String = ""
    var location: String = ""
    var name: String = ""
    var number: String = ""
    var site: String = ""

    constructor() {

    }

    constructor(
        desc: String,
        email: String,
        location: String,
        name: String,
        number: String,
        site: String,
    ) {
        this.desc = desc
        this.email = email
        this.location = location
        this.name = name
        this.number = number
        this.site = site
    }

    fun toHash(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap.put("desc", desc)
        hashMap.put("email", email)
        hashMap.put("location", location)
        hashMap.put("name", name)
        hashMap.put("number", number)
        hashMap.put("site", site)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): Contact {
            val contact = Contact(
                hashMap["desc"] as String,
                hashMap["email"] as String,
                hashMap["location"] as String,
                hashMap["name"] as String,
                hashMap["number"] as String,
                hashMap["site"] as String
            )
            return contact
        }
    }
}