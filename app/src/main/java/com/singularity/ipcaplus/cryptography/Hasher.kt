package com.singularity.ipcaplus.cryptography

import java.security.MessageDigest

class Hasher {
    fun hash(): String {
        val bytes = this.toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}