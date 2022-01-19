package com.singularity.ipcaplus.utils

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.cryptography.encryptMeta
import java.lang.Exception
import java.net.URI
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.util.regex.Pattern

import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.singularity.ipcaplus.chat.ChatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection


object  Utilis {

    /*
        This function create a Date picker
        @dateTextView = the textView responsible for select the date
        @context = activity that the date picker is in
     */
    fun initDatePicker(dateTextView: TextView, context: Context): DatePickerDialog {

        // After the user finish selecting a date, change the date in the button
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->

                // Add the date to the button
                val date = "$day/${month+1}/$year"
                dateTextView.text = date

                // Call the time pop up window
                val timePickerDialog = initTimePicker(dateTextView, context)
                timePickerDialog.show()
            }

        // Get the data to create the date picker
        val cal: Calendar = Calendar.getInstance()
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)
        val style: Int = AlertDialog.THEME_HOLO_LIGHT

        // Create and return the date picker
        return DatePickerDialog(context, style, dateSetListener, year, month, day)
    }


    /*
        This function create a Time picker and join the time with the date previous selected
        @dateTextView = the textView responsible for select the date
        @context = activity that the date picker is in
     */
    private fun initTimePicker(dateTextView: TextView, context: Context): TimePickerDialog {

        // After the user finish selecting a time, join the time in the previous string
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener {_, hour, minute ->
                val time = "$hour:$minute"
                "${dateTextView.text} - $time".also { dateTextView.text = it }
            }

        // Get the data to create the time picker
        val cal: Calendar = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR)
        val minute = cal.get(Calendar.MINUTE)
        val style: Int = AlertDialog.THEME_HOLO_LIGHT

        // Create and return the date picker
        return TimePickerDialog(context, style, timeSetListener, hour, minute, true)
    }


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
        var result = 2021 - 121 // add 1900

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

        var lastNumber = ((strArray[1].toInt() * 100) / 60).toString()

        if (lastNumber == "0")
            lastNumber = "00"

        val result = strArray[0] + lastNumber

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


    fun limitString(name: String, size: Int): String {
        return name.dropLast(size)
    }


    /*
       ------------------------------------------------ Images ------------------------------------------------
    */

    fun convertDrawableToBitmap(context: Context, drawable: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.circle)
    }


    fun getFile(context: Context, path: String, suffix: String, callback:(Bitmap)->Unit) {

        // Retrieve image from firebase
        val storageRef = FirebaseStorage.getInstance().reference.child(path)
        val localfile = File.createTempFile("tempImage", suffix)

        // Set ImageView
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            callback(bitmap)
        }.addOnFailureListener {
            val bitmap = (ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_defaultimage,
                null
            ) as Drawable?)!!.toBitmap()
            callback(bitmap)
        }

    }

    fun downloadFile(context: Context, fileName: String, fileExtension: String, destinationDirectory: String, uri: Uri) {

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(uri)

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(destinationDirectory, fileName + fileExtension)

        downloadManager.enqueue(request)
    }


    fun uploadFile(filePath: Uri, targetPath: String) {

        val storage = Firebase.storage
        val storageRef = storage.reference

        if (filePath != Uri.EMPTY) {
            val ref: StorageReference = storageRef.child(targetPath)
            ref.putFile(filePath)
        }
    }


    fun getFileIcon(fileName:String): Int {
        return if (fileName.contains(".")) {

            val extensionArray = Pattern.compile("[.]").split(fileName)
            val extension = extensionArray[extensionArray.size-1]

            when (extension) {
                "png", "jpg", "jpeg", "jep", "jfif", "gif" -> R.drawable.ic_picture
                "invisible" -> -1
                else -> R.drawable.ic_file
            }

        } else
            -1
    }


    fun uniqueImageNameGen(): String {
        val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!#$$%&()=@[]{}"

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

    /*
        ------------------------------------------------ Chat ------------------------------------------------
     */

    /*
       This function returns the encrypted system message
       @callBack = return the list
    */
    fun buildSystemMessage(key: String, iv : String) : String {

        // Build encryptation data of first message send by the system
        var message = encryptMeta("This chat is being encripted with Singularity Encryption!", key, iv)

        return message.toString()

    }

    // Que merda é esta :v
    fun getUID(): String? {
        val firebaseAuth = FirebaseAuth.getInstance()
        return firebaseAuth.uid
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    suspend fun  createNotificationGroup(notificationKeyName : String, registrationIds : JSONArray) : String {

        var notificationKey = ""

        try {

            Log.d("json", registrationIds.toString())

            //Request
            val endPoint = URL("https://fcm.googleapis.com/fcm/notification")

            //Establish a connection
            val httpsURLConnection: HttpsURLConnection =
                endPoint.openConnection() as HttpsURLConnection

            //Connection to fcm
            //The time available to read from the input stream when the connection is established
            httpsURLConnection.readTimeout = 10000
            //The time available to connect to the url
            httpsURLConnection.connectTimeout = 15000
            //Defining the type of request to be made to the fcm
            httpsURLConnection.requestMethod = "POST"
            //Defining that the url connection can be used to send and receive data
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            // Build parameters for json
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")
            val project_key = "AAAAMMR-Gaw:APA91bFeijRa909_QEdEFsQeDSaJZRYD7rOk8B8Bc2QiYcGoyLG1xqqpZLkOJXmZrG0FbScojvqBCsweSEWDrMLM6kr67boS-BVB2oy7fL6Zn1N9ICVk6efGniauDa3z8eaOb1TENmEs"
            val senderId = "209455028652"
            httpsURLConnection.setRequestProperty("authorization", "key=$project_key")
            httpsURLConnection.setRequestProperty("project_id", senderId)

            val json = JSONObject()

            json.put("operation", "create")
            json.put("notification_key_name", notificationKeyName)
            json.put("registration_ids", registrationIds)


            // Writer
            val outputStream: OutputStream =
                BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))

            // POST
            writer.write(json.toString())
            writer.flush()
            writer.close()

            outputStream.close()

            //The response code and message of the POST requests
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage = httpsURLConnection.responseMessage

            Log.d(ContentValues.TAG, "$responseCode $responseMessage")


            // Check server STATUS
            if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }
            println("CUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU NAO CHEGOU SEU BURRO")
            if (responseCode == 200) {
                Log.e(ContentValues.TAG, "Group Created!!")

                val response = httpsURLConnection.inputStream.bufferedReader()
                    .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {
                    //notification_key
                    val jsonObject  = JSONObject(response)
                    notificationKey = jsonObject.getString("notification_key")
                    println("NotifKey: $notificationKey")
                    Log.d("NotifKey", notificationKey)
                }
                println("CUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU $notificationKey")
                return notificationKey
            } else {
                Log.e(ContentValues.TAG, "Error it didn´t work")
            }

            //Here i close the connection to the endPoint
            httpsURLConnection.disconnect()


        } catch (e: Exception) {
            e.printStackTrace()
        }
        return notificationKey
    }

    //This function sends push notifications to devices that are subscribed to a specific topic
    suspend fun sendNotificationToGroup(title: String, message: String, notificationKey : String) {

        delay(1500)

        try {

            //Request
            val url = URL("https://fcm.googleapis.com/fcm/send")

            //Establish a connection
            val httpsURLConnection: HttpsURLConnection =
                url.openConnection() as HttpsURLConnection

            //The time available to read from the input stream when the connection is established
            httpsURLConnection.readTimeout = 10000
            //The time available to connect to the url
            httpsURLConnection.connectTimeout = 15000
            //Defining the type of request to be made to the fcm
            httpsURLConnection.requestMethod = "POST"
            //Defining that the url connection can be used to send and receive data
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            // Config of FCM
            val project_key =
                "AAAAMMR-Gaw:APA91bFeijRa909_QEdEFsQeDSaJZRYD7rOk8B8Bc2QiYcGoyLG1xqqpZLkOJXmZrG0FbScojvqBCsweSEWDrMLM6kr67boS-BVB2oy7fL6Zn1N9ICVk6efGniauDa3z8eaOb1TENmEs"
            httpsURLConnection.setRequestProperty("authorization", "key=$project_key")
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")

            val jsonObject = JSONObject()
            val data = JSONObject()

            data.put("title", title)
            data.put("content", message)
            //On Notification Click Activity
            data.put("click_action", ".LoginActivity")

            //jsonObject for POST
            jsonObject.put("data", data)
            //
            jsonObject.put("to", notificationKey)

            val outputStream: OutputStream =
                BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))

            writer.write(jsonObject.toString())
            writer.flush()
            writer.close()

            outputStream.close()

            //The response code and message of the POST requests
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage: String = httpsURLConnection.responseMessage


            Log.d(ContentValues.TAG, "Response from sendMes: $responseCode $responseMessage")


            // Check server STATUS
            if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }
            if (responseCode == 200) {
                Log.e(
                    ContentValues.TAG,
                    "Notification Sent \n Title: $title \n Body: $message"
                )
            } else {
                Log.e(ContentValues.TAG, "Notification Error")
            }

            httpsURLConnection.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    suspend fun  removeKeyFromNotificationGroup(notificationKeyName : String, registrationIds : JSONArray) : String {

        var notificationKey = ""

        try {

            Log.d("json", registrationIds.toString())

            //Request
            val endPoint = URL("https://fcm.googleapis.com/fcm/notification")

            //Establish a connection
            val httpsURLConnection: HttpsURLConnection =
                endPoint.openConnection() as HttpsURLConnection

            //Connection to fcm
            //The time available to read from the input stream when the connection is established
            httpsURLConnection.readTimeout = 10000
            //The time available to connect to the url
            httpsURLConnection.connectTimeout = 15000
            //Defining the type of request to be made to the fcm
            httpsURLConnection.requestMethod = "POST"
            //Defining that the url connection can be used to send and receive data
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            // Build parameters for json
            httpsURLConnection.setRequestProperty("Content-Type", "application/json")
            val project_key = "AAAAMMR-Gaw:APA91bFeijRa909_QEdEFsQeDSaJZRYD7rOk8B8Bc2QiYcGoyLG1xqqpZLkOJXmZrG0FbScojvqBCsweSEWDrMLM6kr67boS-BVB2oy7fL6Zn1N9ICVk6efGniauDa3z8eaOb1TENmEs"
            val senderId = "209455028652"
            httpsURLConnection.setRequestProperty("authorization", "key=$project_key")
            httpsURLConnection.setRequestProperty("project_id", senderId)

            val json = JSONObject()

            json.put("operation", "remove")
            json.put("notification_key_name", notificationKeyName)
            json.put("registration_ids", registrationIds)


            // Writer
            val outputStream: OutputStream =
                BufferedOutputStream(httpsURLConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))

            // POST
            writer.write(json.toString())
            writer.flush()
            writer.close()

            outputStream.close()

            //The response code and message of the POST requests
            val responseCode: Int = httpsURLConnection.responseCode
            val responseMessage = httpsURLConnection.responseMessage

            Log.d(ContentValues.TAG, "$responseCode $responseMessage")


            // Check server STATUS
            if (responseCode in 400..499) {
                httpsURLConnection.errorStream
            } else {
                httpsURLConnection.inputStream
            }
            println("CUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU NAO CHEGOU SEU BURRO")
            if (responseCode == 200) {
                Log.e(ContentValues.TAG, "Group Created!!")

                val response = httpsURLConnection.inputStream.bufferedReader()
                    .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {
                    //notification_key
                    val jsonObject  = JSONObject(response)
                    notificationKey = jsonObject.getString("notification_key")
                    println("NotifKey: $notificationKey")
                    Log.d("NotifKey", notificationKey)
                }
                println("CUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU $notificationKey")
                return notificationKey
            } else {
                Log.e(ContentValues.TAG, "Error it didn´t work")
            }

            //Here i close the connection to the endPoint
            httpsURLConnection.disconnect()


        } catch (e: Exception) {
            e.printStackTrace()
        }
        return notificationKey
    }

}