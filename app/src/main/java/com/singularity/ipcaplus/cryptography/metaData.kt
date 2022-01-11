package com.singularity.ipcaplus.cryptography

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Base64
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.Security
import java.util.Objects.hash
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec





fun encryptMeta(strToEncrypt: String, secret_key: String): String? {
    Security.addProvider(BouncyCastleProvider())
    hash(strToEncrypt)
    var keyBytes: ByteArray
    //val initVector = generateRandomIV()
    val initVector = "7c5afb00aaecb1a1"
    val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))
    println("AVEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE $initVector")


    try {
        keyBytes = secret_key.toByteArray(charset("UTF8"))
        val skey = SecretKeySpec(keyBytes, "AES")
        val input = strToEncrypt.toByteArray(charset("UTF8"))

        synchronized(Cipher::class.java) {
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, skey, iv)

            val cipherText = ByteArray(cipher.getOutputSize(input.size))
            var ctLength = cipher.update(
                input, 0, input.size,
                cipherText, 0
            )
            ctLength += cipher.doFinal(cipherText, ctLength)
            return String(
                Base64.encode(cipherText)
            )
        }
    } catch (uee: UnsupportedEncodingException) {
        uee.printStackTrace()
    } catch (ibse: IllegalBlockSizeException) {
        ibse.printStackTrace()
    } catch (bpe: BadPaddingException) {
        bpe.printStackTrace()
    } catch (ike: InvalidKeyException) {
        ike.printStackTrace()
    } catch (nspe: NoSuchPaddingException) {
        nspe.printStackTrace()
    } catch (nsae: NoSuchAlgorithmException) {
        nsae.printStackTrace()
    } catch (e: ShortBufferException) {
        e.printStackTrace()
    }

    return null
}

fun decryptWithAESmeta(key: String, strToDecrypt: String?): String? {
    Security.addProvider(BouncyCastleProvider())
    var keyBytes: ByteArray
    val initVector = "7c5afb00aaecb1a1"

    try {
        val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))
        keyBytes = key.toByteArray(charset("UTF8"))
        val skey = SecretKeySpec(keyBytes, "AES")
        val input = org.bouncycastle.util.encoders.Base64
            .decode(strToDecrypt?.trim { it <= ' ' }?.toByteArray(charset("UTF8")))

        synchronized(Cipher::class.java) {
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, skey, iv)

            val plainText = ByteArray(cipher.getOutputSize(input.size))
            var ptLength = cipher.update(input, 0, input.size, plainText, 0)
            ptLength += cipher.doFinal(plainText, ptLength)
            val decryptedString = String(plainText)
            return decryptedString.trim { it <= ' ' }
        }
    } catch (uee: UnsupportedEncodingException) {
        uee.printStackTrace()
    } catch (ibse: IllegalBlockSizeException) {
        ibse.printStackTrace()
    } catch (bpe: BadPaddingException) {
        bpe.printStackTrace()
    } catch (ike: InvalidKeyException) {
        ike.printStackTrace()
    } catch (nspe: NoSuchPaddingException) {
        nspe.printStackTrace()
    } catch (nsae: NoSuchAlgorithmException) {
        nsae.printStackTrace()
    } catch (e: ShortBufferException) {
        e.printStackTrace()
    }

    return null
}

fun metaGenrateKey(): String {
    //val secretKey: String = "662ede816988e58fb6d057d9d85605e0"
    val keygen = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "0123456789"
            + "abcdefghijklmnopqrstuvxyz")

    val n = 32

    // create StringBuffer size of keygen
    val secretKey = StringBuilder(n)
    for (i in 0 until n) {

        // generate a random number between
        // 0 to keygen variable length
        val index = (keygen.length
                * Math.random()).toInt()

        // add Character one by one in end of secretKey
        secretKey.append(keygen[index])

    }
    println(secretKey)
    return secretKey.toString()
}

fun metaBlock(message: String){
    //val secretKey: String = "662ede816988e58fb6d057d9d85605e0"
    val keygen = metaGenrateKey()

    var meta = encryptMeta(message, keygen)
    println(meta)

    val message_decripted = decryptWithAESmeta(keygen, meta)
    println(message_decripted)
}

@RequiresApi(Build.VERSION_CODES.M)
fun saveKeygenOx(context : Context, chatUid : String, keygen: String){

    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    val sharedPreferences = EncryptedSharedPreferences.create(
        "meta_shared_preferences",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // storing a value
    sharedPreferences
        .edit()
        .putString(chatUid, keygen)
        .apply()
}

@RequiresApi(Build.VERSION_CODES.M)
fun getMetaOx(context: Context, chatUid: String): String? {

    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    val sharedPreferences = EncryptedSharedPreferences.create(
        "meta_shared_preferences",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // reading a value
    println(sharedPreferences.getString(chatUid, ""))
    return sharedPreferences.getString(chatUid, "") // -> "some_data"
}

fun generateRandomIV(): String {
    val ranGen = SecureRandom()
    val aesKey = ByteArray(16)
    ranGen.nextBytes(aesKey)
    val result = StringBuffer()
    for (b in aesKey) {
        result.append(String.format("%02x", b))
    }
    return if (16 > result.toString().length) {
        result.toString()
    } else {
        result.toString().substring(0, 16)
    }
}