package com.singularity.ipcaplus.utils

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.models.*
import org.json.JSONArray
import java.io.File

object Backend {

    val db = Firebase.firestore

    /*
       ------------------------------------------------ Events ------------------------------------------------
    */

    /*
       This function returns all events during the month in the firebase to an list
       @month = selected month
       @callBack = return the list
    */
    fun getAllMonthEvents(month: String, callBack: (List<EventCalendar>)->Unit) {

        val events = arrayListOf<EventCalendar>()

        // Get all user chat ids
        val chatIds = arrayListOf<String>()
        getAllUserChatIds {
            chatIds.addAll(it)

            // Search in all chats
            for (id in chatIds) {
                db.collection("chat")
                    .document(id)
                    .collection("event")
                    .addSnapshotListener { documents, _ ->

                        documents?.let {

                            for (document in documents) {
                                val event = EventCalendar.fromHash(document)
                                event.id = document.id

                                val date = Utilis.getDate(event.datetime.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                                if (month == Utilis.getMonthById(Utilis.getMonth(date).toInt())) {
                                    events.add(event)
                                }
                            }
                            callBack(events)
                        }
                    }
            }
        }
    }


    private fun getAllUserChatIds(callBack: (List<String>)->Unit) {

        val chatIds = arrayListOf<String>()

        // Get Group Chats Ids
        db.collection("profile").document(Firebase.auth.currentUser!!.uid).collection("chat")
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    for (document in it) {
                        chatIds.add(document.id)
                    }

                    callBack(chatIds)
                }
            }
    }


    /*
       This function returns all events during the month in the firebase to an list
       @month = selected month
       @callBack = return the list
    */
    fun getAllChatMonthEvents(month: String, chat_id: String, callBack: (List<EventCalendar>)->Unit) {

        val events = arrayListOf<EventCalendar>()

        db.collection("chat")
            .document(chat_id)
            .collection("event")
            .addSnapshotListener { documents, _ ->

                documents?.let {

                    for (document in documents) {
                        val event = EventCalendar.fromHash(document)
                        event.id = document.id

                        val date = Utilis.getDate(
                            event.datetime.seconds * 1000,
                            "yyyy-MM-dd'T'HH:mm:ss.SSS"
                        )
                        if (month == Utilis.getMonthById(Utilis.getMonth(date).toInt())) {
                            events.add(event)
                        }
                    }

                    callBack(events)
                }

            }
    }


    /*
       This function returns all events during the day in the firebase to an list
       @day = selected day
       @callBack = return the list
    */
    fun getAllChatMonthDayEvents(month: String, day: Int, chat_id: String, callBack: (List<EventCalendar>)->Unit) {

        val events = arrayListOf<EventCalendar>()

        db.collection("chat")
            .document(chat_id)
            .collection("event")
            .addSnapshotListener { documents, _ ->

                documents?.let {

                    for (document in documents) {
                        val event = EventCalendar.fromHash(document)
                        event.id = document.id

                        val date = Utilis.getDate(
                            event.datetime.seconds * 1000,
                            "yyyy-MM-dd'T'HH:mm:ss.SSS"
                        )
                        if (day == Utilis.getDay(date).toInt() && month == Utilis.getMonthById(
                                Utilis.getMonth(date).toInt()
                            )
                        ) {
                            events.add(event)
                        }
                    }

                    callBack(events)
                }

            }
    }


    /*
       This function returns all events during the day in the firebase to an list
       @day = selected day
       @callBack = return the list
    */
    fun getAllMonthDayEvents(month: String, day: Int, callBack: (List<EventCalendar>)->Unit) {

        val events = arrayListOf<EventCalendar>()

        // Get all user chat ids
        val chatIds = arrayListOf<String>()
        getAllUserChatIds {
            chatIds.addAll(it)

            // Search in all chats
            for (id in chatIds) {
                db.collection("chat")
                    .document(id)
                    .collection("event")
                    .addSnapshotListener { documents, _ ->

                        documents?.let {

                            for (document in documents) {
                                val event = EventCalendar.fromHash(document)
                                event.id = document.id

                                val date = Utilis.getDate(event.datetime.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                                if (day == Utilis.getDay(date).toInt() && month == Utilis.getMonthById(
                                        Utilis.getMonth(date).toInt())
                                ) {
                                    events.add(event)
                                }
                            }

                            callBack(events)
                        }

                    }
            }
        }
    }


    fun deleteEvent(chatID: String, eventID: String) {

        db.collection("chat")
            .document(chatID)
            .collection("event")
            .document(eventID)
            .delete()

    }


    /*
       ------------------------------------------------ Schedule ------------------------------------------------
    */

    /*
       This function returns all events in the firebase to an list
       @callBack = return the list
    */
    fun getDayCourseSubjects(day: String, courseId: String, callBack: (List<Subject>)->Unit) {

        val subjects = arrayListOf<Subject>()
        val subjectsWithBreaks = arrayListOf<Subject>()

        db.collection("course").document(courseId).collection("subject")
            .addSnapshotListener { documents, _ ->
                documents?.let {

                    // Add every subject to the list
                    for (document in documents) {
                        val subject = Subject.fromHash(document)
                        if (day == subject.day) {
                            subjects.add(subject)
                        }
                    }

                    // Order the subjects by time
                    for (i in 0 until subjects.size) {
                        for (j in 0 until subjects.size - 1) {

                            if (Utilis.convertHoursStringToInt(subjects[j].start_time) > Utilis.convertHoursStringToInt(
                                    subjects[j + 1].start_time
                                )
                            ) {
                                val temp = subjects[j]
                                subjects[j] = subjects[j + 1]
                                subjects[j + 1] = temp
                            }
                        }
                    }

                    // Add Break Times Between Classes
                    for (i in 0 until subjects.size) {
                        if (i % 2 == 0) {
                            subjectsWithBreaks.add(subjects[i])
                        }
                        else {
                            val diff = Utilis.convertHoursStringToInt(subjects[i].start_time) - Utilis.convertHoursStringToInt(
                                subjects[i - 1].end_time
                            )
                            subjectsWithBreaks.add(Subject("breaktime", diff.toString()))
                            subjectsWithBreaks.add(subjects[i])
                        }
                    }

                    callBack(subjectsWithBreaks)
                }

            }

    }


    /*
       This function returns the user course by callback
       @id = user uid
    */
    fun getUserCourses(uid: String, callBack:(String)->Unit) {

        db.collection("profile")
            .document(uid)
            .collection("course")
            .addSnapshotListener { documents, _ ->

                documents?.let {

                    var courseId = ""
                    for (document in documents)
                        courseId = document.id

                    callBack(courseId)
                }
            }
    }


    /*
       This function returns the user course by callback
       @id = user uid
    */
    fun getUserCoursesIds(uid: String, courseTag: String, callBack:(List<String>)->Unit) {

        val courseIds = arrayListOf<String>()

        db.collection("course")
            .whereEqualTo("tag", courseTag)
            .get()
            .addOnSuccessListener { documents ->

                documents?.let {

                    for (document in documents)
                        courseIds.add(document.id)

                    callBack(courseIds)
                }

            }

    }


    fun setUserCourses(userID: String, courseID: String) {

        val profile = HashMap<String, Any>()
        db.collection("profile")
            .document(userID)
            .collection("course")
            .document(courseID)
            .set(profile)
    }


    /*
       ------------------------------------------------ Contacts ------------------------------------------------
    */

    /*
       This function returns all contacts in the firebase to an list
       @callBack = return the list
    */
    fun getAllContacts(callBack: (List<Contact>)->Unit) {

        val contacts = arrayListOf<Contact>()

        db.collection("contacts")
            .addSnapshotListener { documents, _ ->

                documents?.let {

                    for (document in documents) {
                        val contact = Contact.fromHash(document)
                        contacts.add(contact)
                    }

                    callBack(contacts)
                }

            }

    }


    /*
       ------------------------------------------------ Profile ------------------------------------------------
    */

    fun getUserProfile(userId: String, callBack:(Profile)->Unit) {

        var profile = Profile()

        db.collection("profile")
            .addSnapshotListener { documents, _ ->

                documents?.let {

                    for (document in documents) {

                        if (document.id == userId) {
                            profile = Profile.fromHash(document)
                            profile.id = document.id
                        }
                    }

                    callBack(profile)
                }
            }
    }

    fun getAllUsers (callBack:(List<Profile>)->Unit) {
        val profiles = arrayListOf<Profile>()

        db.collection("profile")
            .addSnapshotListener { documents, _ ->
                documents?.let {

                    for (document in documents) {
                        val profile = Profile.fromHash(document)
                        profile.id = document.id
                        profiles.add(profile)
                    }
                }

                callBack(profiles)
            }
    }

    fun getAllUsersExceptCurrent (callBack:(List<Profile>)->Unit) {
        val profiles = arrayListOf<Profile>()

        db.collection("profile")
            .addSnapshotListener { documents, _ ->
                documents?.let {

                    for (document in documents) {
                        val profile = Profile.fromHash(document)

                        if (Firebase.auth.currentUser!!.uid != document.id) {
                            profile.id = document.id
                            profiles.add(profile)
                        }
                    }
                }

                callBack(profiles)
            }
    }


    fun getAllUsersExceptChatUsers (chatID: String, callBack:(List<Profile>)->Unit) {

        val currentUserIds = arrayListOf<String>()
        val profiles = arrayListOf<Profile>()

        // Get current chat users ids
        getChatUsers(chatID) {
            for (user in it)
                currentUserIds.add(user.id!!)

            db.collection("profile")
                .addSnapshotListener { documents, _ ->
                    documents?.let {

                        for (document in documents) {
                            val profile = Profile.fromHash(document)

                            // Verify if user is not in the chat
                            var found = false
                            for (id in currentUserIds) {
                                if (id == document.id)
                                    found = true
                            }

                            if (!found) {
                                profile.id = document.id
                                profiles.add(profile)
                            }
                        }
                    }

                    callBack(profiles)
                }
        }
    }


    fun changeUserChatAdminStatus(chatId: String, userId: String, status: Boolean) {

        db.collection("chat")
            .document(chatId)
            .collection("user")
            .document(userId)
            .update("admin", status)

    }


    /*
       ------------------------------------------------ Chats ------------------------------------------------
    */


    fun getChatUsers(chatID: String, callBack: (List<Profile>)->Unit) {

        val userIds = arrayListOf<String>()
        val adminIds = arrayListOf<String>()
        val users = arrayListOf<Profile>()

        // Get the ids of the users in the chat
        db.collection("chat")
            .document(chatID)
            .collection("user")
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    for (document in documents) {
                        userIds.add(document.id)

                        if (document["admin"] != true)
                            adminIds.add(document.id)

                    }
                }

                // Find the data for each id
                db.collection("profile")
                    .addSnapshotListener { documents2, _ ->
                        documents2?.let {
                            for (document in documents2) {
                                for (id in userIds) {
                                    if (document.id == id) {
                                        val profile = Profile.fromHash(document)
                                        profile.id = document.id
                                        users.add(profile)
                                    }
                                }
                            }
                        }

                        // Get Admins
                        for (u in users) {
                            for (a in adminIds) {
                                if (u.id == a) {
                                    u.isAdmin = true
                                }
                            }
                        }

                        callBack(users)
                    }

            }

    }


    fun getChatUsersUids(chatID: String, callBack: (List<String>) -> Unit){
        var userIds = arrayListOf<String>()

        // Get the ids of the users in the chat
        db.collection("chat")
            .document(chatID)
            .collection("user")
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    for (document in documents) {
                        userIds.add(document.id)
                    }
                }

                callBack(userIds)
            }
    }


    fun changeChatName(chatID: String, newName: String) {

        var userIds = arrayListOf<String>()

        db.collection("chat")
            .document(chatID)
            .update("name", newName)

        getChatUsersUids(chatID) {
            userIds.addAll(it)

            for (userId in userIds) {
                db.collection("profile")
                    .document(userId)
                    .collection("chat")
                    .document(chatID)
                    .update("name", newName)
            }
        }
    }


    /*
       This function returns last chat message by chat id
       @callBack = return the message
    */
    fun getLastMessageByChatID(chatID: String, callBack: (Message?)->Unit) {

        var message : Message? = null

        db.collection("chat").document("${chatID}").collection("message")
            .orderBy("time", Query.Direction.DESCENDING).limit(1)
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    for (document in documents) {
                        message = Message.fromHash(document)
                        }
                    }

                    callBack(message)
                }

    }


    fun getChatAdminIds(chatID: String, callBack: (List<String>)->Unit) {

        val adminIds = arrayListOf<String>()

        // Get Admins
        db.collection("chat")
            .document(chatID)
            .collection("user")
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    for (document in documents) {

                        if (document["admin"] == true)
                            adminIds.add(document.id)

                    }

                    callBack(adminIds)
                }
            }

    }


    fun removeUserFromChat(chatId: String, userId: String) {

        // remover user from chat user list
        db.collection("chat")
            .document(chatId)
            .collection("user")
            .document(userId)
            .delete()

        // remove chat from user chat list
        db.collection("profile")
            .document(userId)
            .collection("chat")
            .document(chatId)
            .delete()
    }


    fun addUsersIntoChat(chat: Chat, chatId: String, usersId: ArrayList<String>, callBack: ()->Unit) {

        for (userId in usersId) {

            // Create chat in user profile
            db.collection("profile")
                .document(userId)
                .collection("chat")
                .document(chatId)
                .set(chat)
                .addOnCompleteListener {

                    val profile = HashMap<String, Any>()
                    db.collection("chat")
                        .document(chatId)
                        .collection("user")
                        .document(userId)
                        .set(profile)
                        .addOnCompleteListener {
                            callBack()
                        }

                }

        }
    }


    fun deleteChat(chatId: String, callback: ()->Unit) {

        val userIds = arrayListOf<String>()

        // get all chat members ids
        db.collection("chat")
            .document(chatId)
            .collection("user")
            .addSnapshotListener { documents, _ ->

                documents?.let {
                    for (document in documents) {
                        userIds.add(document.id)
                    }

                    // <------------------------------------------------------- Missing here

                    // delete chat
                    /*
                    db.collection("chat")
                        .document(chatId)
                        .delete()
*/

                    // delete chat references in members
                    for (i in 0 until userIds.size) {

                        db.collection("profile")
                            .document(userIds[i])
                            .collection("chat")
                            .document(chatId)
                            .delete()
                            .addOnCompleteListener {
                                // If its the last callback refresh the activity
                                if (i == userIds.size - 1)
                                    callback()
                            }

                    }
            }
        }





    }



    fun getAllDirectChatIdsByUser(userId: String, callBack: (List<String?>) -> Unit){

        var chatIds = arrayListOf<String?>()

        // Get all profile chat ids
        db.collection("profile")
            .document(userId)
            .collection("chat")
            .whereEqualTo("type", "chat")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    chatIds.add(document.id)
                }

                println("DEntroooo" + chatIds)
                callBack(chatIds)
            }

    }

    fun getDirectChatById(chatIds: List<String?>, userId: String, callBack: (String?)-> Unit) {

        var chatId : String? = null

        for (id in chatIds) {
            db.collection("chat")
                .document(id!!)
                .collection("user")
                .get()
                .addOnSuccessListener { documents ->
                    for(document in documents) {
                        if(document.id == userId){
                            chatId = id
                        }
                    }

                    println("ACABOU2---------------------------------------")
                    callBack(chatId)
                }
        }

    }


    /*
       ------------------------------------------------ Register Manipulation ------------------------------------------------
    */

    fun getIpcaData(email: String, callBack: (Profile?)->Unit) {

        var profile : Profile? = null

        db.collection("ipca_data")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    profile = Profile.fromHash(document)
                }

                callBack(profile)

            }

    }


    /*
       ------------------------------------------------ Files ------------------------------------------------
    */

    fun getAllChatFolderFiles(path: String, callBack: (List<FirebaseFile>) -> Unit) {

        val files = arrayListOf<FirebaseFile>()
        val listRef = Firebase.storage.reference.child(path)

        // Find all the prefixes and items.
        listRef.listAll().addOnSuccessListener {

            for (i in it.prefixes) {
                files.add(FirebaseFile(i.name, R.drawable.ic_folder))
            }

            for (i in it.items) {
                val icon = Utilis.getFileIcon(i.name)
                if (icon != -1) {
                    val file = FirebaseFile(i.name, icon)
                    files.add(file)
                }
            }

            callBack(files)
        }

    }

    /*
      ------------------------------------------------ Files ------------------------------------------------
   */


    fun deleteAllFilesInsideFolder(filePath: String, callback: ()->Unit) {

        val storage = Firebase.storage
        val listRef = storage.reference.child(filePath)

        listRef.listAll()
            .addOnSuccessListener {

                it.items.forEach { item ->
                    item.delete()
                        .addOnCompleteListener {
                            callback()
                        }
                }

            }
    }


    fun getFileUrl(filePath: String, callback: (Uri)->Unit) {

        val storageRef = FirebaseStorage.getInstance().reference.child(filePath)

        storageRef.downloadUrl.addOnCompleteListener {
            callback(it.result!!)
        }

    }


    fun postTokenAddress(tokenAdress: String, uid: String){
        println(tokenAdress)
        println(uid)
        var token = HashMap<String, String>()
        db.collection("profile")
            .document(uid)
            .collection("tokens")
            .document(tokenAdress)
            .set(token)
    }

    fun getAllTokens(uid: String, callBack: (List<String>) -> Unit){
        val tokens = arrayListOf<String>()

        db.collection("profile").document(uid).collection("tokens")
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    for (document in documents) {
                            tokens.add(document.id)
                    }

                    callBack(tokens)
                }
            }

    }

    fun createJsonArrayString(array: List<String>): JSONArray {
        return JSONArray(array)
    }
}