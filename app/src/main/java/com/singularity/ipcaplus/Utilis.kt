package com.singularity.ipcaplus

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Telephony.Carriers.PORT
import androidx.annotation.RequiresApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.util.regex.Pattern

object  Utilis {

    /*
        This function return the current month id
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentMonthId(): Int {

        val c = LocalDate.now()
        val strArray = Pattern.compile("-").split(c.toString())

        return strArray[1].toInt()
    }

    /*
        This function return the current year id
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentYear(): String {

        val c = LocalDate.now()
        val strArray = Pattern.compile("-").split(c.toString())

        return strArray[0]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getYearByCalendarId(id: Int): Int {

        var count = id
        var result = 2021 - 121

        while (count > 0) {
            count -= 1
            result ++
        }

        return result
    }

    /*
        This function return the month value in string by an id
        @month = month id
     */
    fun getMonthById(month: Int): String {
        return when (month) {
            1 -> "Janeiro"
            2 -> "Fevereiro"
            3 -> "Março"
            4 -> "Abril"
            5 -> "Maio"
            6 -> "Junho"
            7 -> "Julho"
            8 -> "Agosto"
            9 -> "Setembro"
            10 -> "Outubro"
            11 -> "Novembro"
            12 -> "Dezembro"
            else -> "None"
        }
    }

    fun convertHoursStringToInt(strVal: String): Int {
        val strArray = Pattern.compile(":").split(strVal)
        val result = strArray[0] + strArray[1]
        return result.toInt()
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }


    /*
        This function split the date and return only the day
        @date = default date
     */
    fun getDay(dateTime: String): String {

        // Split the date
        val strArray = Pattern.compile("-").split(dateTime)
        val strArray2 = Pattern.compile("T").split(strArray[2])

        return strArray2[0]
    }


    /*
        This function split the date and return only the month in text
        @date = default date
     */
    fun getMonth(dateTime: String): String {

        // Split the date
        val strArray = Pattern.compile("-").split(dateTime)

        return strArray[1].toString()
    }


    /*
        This function split the date and return only the year in text
        @date = default date
     */
    fun getYear(dateTime: String): String {

        // Split the date
        val strArray = Pattern.compile("-").split(dateTime)

        return strArray[0].toString()
    }


    /*
        This function split the date and return only the Hours in text
        @date = default date
     */
    fun getHours(dateTime: String): String {

        // Split the date
        val strArray = Pattern.compile("T").split(dateTime)
        val strArray2 = Pattern.compile(":").split(strArray[1])

        return strArray2[0].toString()
    }


    /*
        This function split the date and return only the Minutes in text
        @date = default date
     */
    fun getMinutes(dateTime: String): String {

        // Split the date
        val strArray = Pattern.compile("T").split(dateTime)
        val strArray2 = Pattern.compile(":").split(strArray[1])

        return strArray2[1].toString()
    }

    /*
        This function get the buffer with the name of filename on assets folder
     */

    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    fun getFirstAndLastName(fullName: String): String {

        val nameArray = Pattern.compile(" ").split(fullName)

        return nameArray[0] + " " + nameArray[nameArray.size-1]
    }


    /*
       ------------------------------------------------ Images ------------------------------------------------
    */

    fun getUserImage(userId: String, callback:(Bitmap)->Unit) {

        // Retrieve image from firebase
        val storageRef = FirebaseStorage.getInstance().reference.child("profilePictures/" + Firebase.auth.uid!! + ".png")
        val localfile = File.createTempFile("tempImage", "jpg")

        // Set ImageView
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            callback(bitmap)
        }
    }


    fun uploadImage(filePath: Uri, targetPath: String, context: Context) {

        val storage = Firebase.storage
        val storageRef = storage.reference

        if (filePath != null) {
            val ref: StorageReference = storageRef.child(targetPath)
            ref.putFile(filePath)
        }
    }

    fun uniqueImageNameGen(): String {
        val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!#$$%&/()=@[]{}"

        val sb = StringBuilder(15)

        for(x in 0 until 15){
            val random = (characters.indices).random()
            sb.append(characters[random])
        }

        return sb.toString()
    }
    /*
        This function gets the domain of email
        split remove the @ and make the array like this -> [a20115][alunos.ipca.pt]
     */
    fun getEmailDomain(email: String) : String{
        val strArray = Pattern.compile("@").split(email)
        val result= strArray[1]

        return result.toString()

    }

}