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
    fun getAllDaySubjects(day: String ,callBack: (List<Subject>)->Unit) {

        val subjects = arrayListOf<Subject>()

        db.collection("course").document("lLhV0mtn5kNZ5Ivr3FKH").collection("subject")
            .addSnapshotListener { documents, _ ->
                documents?.let {
                    for (document in documents) {
                        val subject = Subject.fromHash(document)
                        println(day + "==" + subject.day)
                        if (day == subject.day) {
                            subjects.add(subject)
                            println("adadadadadad")
                        }
                    }

                    callBack(subjects)
                }

            }

    }

}