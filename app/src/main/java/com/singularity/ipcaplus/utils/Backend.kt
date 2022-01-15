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
import javax.security.auth.callback.Callback

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
    fun getDayCourseClasses(day: String, courseId: String, callBack: (List<SubjectClass>)->Unit) {

        val subjectClasses = arrayListOf<SubjectClass>()
        val subjectClassesWithBreaks = arrayListOf<SubjectClass>()

        // Get all subjects in course
        db.collection("course").document(courseId).collection("subject")
            .addSnapshotListener { documents1, _ ->
                documents1?.let {

                    // Add every subject to the list
                    for (_document1 in documents1) {

                        // get current subject
                        val subject = Subject.fromHash(_document1)

                        // Get all classes of all subjects of the course
                        db.collection("course")
                            .document(courseId)
                            .collection("subject")
                            .document(_document1.id)
                            .collection("class")
                            .addSnapshotListener { documents2, _ ->

                                // Get teacher name
                                db.collection("ipca_data")
                                    .document(subject.teacher)
                                    .get()
                                    .addOnSuccessListener { documents3 ->
                                        val teacherName = documents3.data!!["name"].toString()
                                        println("---------------------------------> teacher: " + teacherName)

                                    documents2?.let {

                                        for (_document2 in documents2) {

                                            val subjectClass = SubjectClass.fromHash(_document2)
                                            if (day == subjectClass.day) {
                                                subjectClass.name = subject.name
                                                subjectClass.teacher = teacherName
                                                subjectClasses.add(subjectClass)
                                            }
                                        }

                                    }

                                    // Order the subjects by time
                                    for (i in 0 until subjectClasses.size) {
                                        for (j in 0 until subjectClasses.size - 1) {

                                            if (Utilis.convertHoursStringToInt(subjectClasses[j].start_time) > Utilis.convertHoursStringToInt(
                                                    subjectClasses[j + 1].start_time
                                                )
                                            ) {
                                                val temp = subjectClasses[j]
                                                subjectClasses[j] = subjectClasses[j + 1]
                                                subjectClasses[j + 1] = temp
                                            }
                                        }
                                    }

                                    // Add Break Times Between Classes
                                    for (i in 0 until subjectClasses.size) {
                                        if (i % 2 == 0) {
                                            subjectClassesWithBreaks.add(subjectClasses[i])
                                        }
                                        else {
                                            val diff = Utilis.convertHoursStringToInt(subjectClasses[i].start_time) - Utilis.convertHoursStringToInt(
                                                subjectClasses[i - 1].end_time
                                            )
                                            subjectClassesWithBreaks.add(SubjectClass("breaktime", diff.toString()))
                                            subjectClassesWithBreaks.add(subjectClasses[i])
                                        }
                                    }

                                    callBack(subjectClassesWithBreaks)
                                }
                            }

                    }


                }

            }

    }

    fun getDayTeacherClasses() {

    }


    /*
       This function returns the user course by callback
       @id = user uid
    */
    fun getUserCourseId(uid: String, callBack:(String)->Unit) {

        db.collection("profile")
            .document(uid)
            .collection("course")
            .addSnapshotListener { documents, _ ->

                documents?.let {

                    var courseTag = ""
                    for (document in documents) {
                        val course = Course.fromHash(document)
                        courseTag = course.tag
                    }

                    db.collection("course")
                        .whereEqualTo("tag", courseTag)
                        .addSnapshotListener { documents, _ ->

                            documents?.let {

                                var courseId = ""
                                for (document in documents) {
                                    courseId = document.id
                                }

                                callBack(courseId)
                            }

                        }

                }
            }
    }


    fun setUserCourseByIpcaData(userID: String, ipcaDataID: String, callBack:(String)->Unit) {

        var courseTag = ""

        db.collection("ipca_data")
            .document(ipcaDataID)
            .collection("course")
            .get()
            .addOnSuccessListener { documents ->
                documents.let {

                    for (document in documents) {

                        val course = Course.fromHash(document)
                        courseTag = course.tag

                        db.collection("profile")
                            .document(userID)
                            .collection("course")
                            .document(document.id)
                            .set(course)
                            .addOnCompleteListener {
                                callBack(courseTag)
                            }

                    }
                }
            }
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
                            chatId = document.id
                        }
                    }
                    callBack(chatId)
                }
        }

    }

    fun getOficialChatByTag(courseTag : String, callBack: (List<Chat>) -> Unit) {

        var oficialChat = arrayListOf<Chat>()

        db.collection("chat")
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    // Get all Oficial Course Chats
                    for (document in documents) {
                        val chat = Chat.fromHash(document)
                        chat.id = document.id
                        if (chat.type == "oficial$courseTag") {
                            oficialChat.add(chat)
                        }
                    }
                    callBack(oficialChat)
                }
            }

    }

    fun setTeacherSubjectsByIpcaData(userId: String, userIpcaDataId: String) {

        // Get all subject ids in user ipca data id
        db.collection("ipca_data")
            .document(userIpcaDataId)
            .collection("subject")
            .addSnapshotListener { documents, _ ->
                documents?.let {

                    // Get all subjects names
                    for (document in documents) {
                        val name = document["name"] as String
                        val id = document["id"] as String

                        // add every one in the profile
                        db.collection("profile")
                            .document(userId)
                            .collection("subject")
                            .add(mapOf(
                                "name" to name,
                                "id" to id
                            ))
                            .addOnCompleteListener {

                                // Get current oficial chat
                                db.collection("chat")
                                    .whereEqualTo("name", name)
                                    .get()
                                    .addOnSuccessListener { documents ->
                                        for (chat in documents) {

                                            // Create oficial chat references
                                            db.collection("profile")
                                                .document(userId)
                                                .collection("chat")
                                                .document(chat.id)
                                                .set(chat.data)
                                            db.collection("chat")
                                                .document(chat.id)
                                                .collection("user")
                                                .document(userId)
                                                .set(mapOf(
                                                    "admin" to true
                                                ))

                                        }
                                    }
                            }
                    }

                }
            }

    }

    fun setOficialChat (userId: String, chats: (List<Chat>)) {

            // Insert chats into user profile
            for (chat in chats) {
                db.collection("profile")
                    .document(userId)
                    .collection("chat")
                    .document(chat.id.toString())
                        .set(chat)
                db.collection("chat")
                    .document(chat.id.toString())
                    .collection("user")
                    .document(userId)
                    .set(mapOf(
                        "admin" to null
                    ))


            }
        }




    /*
       ------------------------------------------------ Register Manipulation ------------------------------------------------
    */

    fun getIpcaData(email: String, callBack: (Profile?, String)->Unit) {

        var profile : Profile? = null
        var id = ""

        db.collection("ipca_data")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    profile = Profile.fromHash(document)
                    id = document.id
                }

                callBack(profile, id)
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

    fun getIv(chat_id: String, callBack: (String?)->Unit) {

        var iv : String? = null

        db.collection("chat")
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    if (document.id == chat_id) {
                        val chat = Chat.fromHash(document)
                        iv = chat.iv.toString()
                    }
                }
                callBack(iv)
            }
    }

    fun getNotificationKey(chat_id: String, callBack: (String?)->Unit) {

        var notificationKey : String? = null

        db.collection("chat")
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    if (document.id == chat_id) {
                        val chat = Chat.fromHash(document)
                        notificationKey = chat.notificationKey.toString()
                    }
                }
                callBack(notificationKey)
            }
    }

    fun put0xBlank(chatId: String){

        db.collection("chat")
            .document(chatId)
            .update(mapOf(
                "ox" to ""
            ))
    }
    fun put0xBlankProfile(chatId: String, callback: () -> Unit){

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

                    for (i in 0 until userIds.size) {

                        db.collection("profile")
                            .document(userIds[i])
                            .collection("chat")
                            .document(chatId)
                            .update(mapOf(
                                "ox" to ""
                            ))

                    }
                }
            }
    }

}