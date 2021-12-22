package com.singularity.ipcaplus.calendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularity.ipcaplus.Backend
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.Utilis
import com.singularity.ipcaplus.databinding.ActivityCalendarBinding
import com.singularity.ipcaplus.databinding.ActivityScheduleBinding
import com.singularity.ipcaplus.models.EventCalendar
import com.singularity.ipcaplus.models.Subject

class ScheduleActivity : AppCompatActivity() {

    var subjects = arrayListOf<Subject>()
    private var scheduleAdapter: RecyclerView.Adapter<*>? = null
    private var scheduleLayoutManager: LinearLayoutManager? = null

    private lateinit var binding: ActivityScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // Create the layout for this fragment
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Horário"

        // Get All Subjects during the day
        Backend.getAllDaySubjects("seg"){
            subjects.addAll(it)
        }

        // Get All Break times during the day


        // Schedule List
        scheduleLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewSchedule.layoutManager = scheduleLayoutManager
        scheduleAdapter = ScheduleAdapter()
        binding.recyclerViewSchedule.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewSchedule.adapter = scheduleAdapter
    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    inner class ScheduleAdapter : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_subject, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {

                // Get data
                val textViewSubject = findViewById<TextView>(R.id.textViewSubject)
                val textViewTitleTeacher = findViewById<TextView>(R.id.textViewTitleTeacher)
                val textViewHours = findViewById<TextView>(R.id.textViewHours)
                val textViewClassRoom = findViewById<TextView>(R.id.textViewClassRoom)

                // Set data
                textViewSubject.text = subjects[position].name
                textViewTitleTeacher.text = subjects[position].teacher
                textViewHours.text = subjects[position].start_time + " - " + subjects[position].end_time
                textViewClassRoom.text = "Sala " + subjects[position].classroom

            }
        }

        override fun getItemCount(): Int {
            return subjects.size
        }
    }
}