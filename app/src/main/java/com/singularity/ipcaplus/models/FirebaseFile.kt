package com.singularity.ipcaplus.models

class FirebaseFile {

    var name: String
    var icon: Int

    constructor(name: String, type: Int) {
        this.name = name
        this.icon = type
    }

}