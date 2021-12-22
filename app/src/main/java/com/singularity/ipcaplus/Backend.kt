package com.singularity.ipcaplus

import android.graphics.Color
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.models.EventCalendar
import com.singularity.ipcaplus.models.Subject

object Backend {

    val db = Firebase.firestore

    /*
       ------------------------------------------------ Events ------------------------------------------------
    */


    /*
       This function returns all events in the firebase to an list
       @callBack = return the list
    */
    fun getAllEvents(callBack: (List<EventCalendar>)->Unit) {

        val events = arrayListOf<EventCalendar>()

        db.collection("event")
            .addSnapshotListener { documents, _ ->

                documents?.let {

                    for (document in documents) {
                        val event = EventCalendar.fromHash(document)
                        events.add(event)
                    }

                    callBack(events)
                }

            }

    }


    /*
       This function returns all events during the month in the firebase to an list
       @month = selected month
       @callBack = return the list
    */
    fun getAllMonthEvents(month: String, callBack: (List<EventCalendar>)->Unit) {

        val events = arrayListOf<EventCalendar>()

        db.collection("event")
            .addSnapshotListener { documents, _ ->

                documents?.let {

                    for (document in documents) {
                        val event = EventCalendar.fromHash(document)

                        val date = Utilis.getDate(event.datetime.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
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
    fun getAllMonthDayEvents(month: String, day: Int, callBack: (List<EventCalendar>)->Unit) {

        val events = arrayListOf<EventCalendar>()

        db.collection("event")
            .addSnapshotListener { documents, _ ->

                documents?.let {

                    for (document in documents) {
                        val event = EventCalendar.fromHash(document)

                        val date = Utilis.getDate(event.datetime.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                        if (day == Utilis.getDay(date).toInt() && month == Utilis.getMonthById(Utilis.getMonth(date).toInt())) {
                            events.add(event)
                        }
                    }

                    callBack(events)
                }

            }
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

                            if (Utilis.convertHoursStringToInt(subjects[j].start_time) > Utilis.convertHoursStringToInt(subjects[j+1].start_time)) {
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
                            val diff = Utilis.convertHoursStringToInt(subjects[i].start_time) - Utilis.convertHoursStringToInt(subjects[i-1].end_time)
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
            .collection("course").limit(1)
            .addSnapshotListener { documents, _ ->

                documents?.let {

                    var courseId = ""
                    for (document in documents)
                        courseId = document.id

                    callBack(courseId)
                }
            }
    }
}