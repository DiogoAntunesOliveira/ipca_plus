package com.singularity.ipcaplus.utils

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.models.*

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
    fun getUserCourse(uid: String, callBack:(String)->Unit) {

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
                        }
                    }

                    callBack(profile)
                }
            }
    }


    /*
       ------------------------------------------------ Chats ------------------------------------------------
    */
    /*
    /*
       This function returns chats based on given type
       @callBack = return the list
    */
    fun getChatByType(type: String ,callBack: (List<Chat>, List<String>)->Unit) {

        val chats = arrayListOf<Chat>()
        val chatIds = arrayListOf<String>()

        db.collection("profile").document("${Firebase.auth.currentUser!!.uid}").collection("chat")
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    for (document in documents) {
                        val chat = Chat.fromHash(document)
                        if (type == chat.type) {
                            chats.add(chat)
                            chatIds.add(document.id)
                        }
                    }

                    callBack(chats, chatIds)
                }

            }

    }
    */

    /*
       This function returns last chat message by chat id
       @callBack = return the list
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

        // Get Group Chats Ids
        db.collection("chat").document(chatID).collection("admin")
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    for (document in it) {
                        adminIds.add(document.id)
                    }

                    callBack(adminIds)
                }
            }

    }

    /*
       ------------------------------------------------ Register Manipulation ------------------------------------------------
    */

    fun getIpcaData(email: String, callBack: (Profile?)->Unit) {

        var profile : Profile? = null

        db.collection("ipca_data")
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    for (document in documents) {
                        println(document.getField("email"))
                        if (email == document.getField("email")){
                            profile = Profile.fromHash(document)
                        }
                    }
                }
                callBack(profile)
            }

    }



}