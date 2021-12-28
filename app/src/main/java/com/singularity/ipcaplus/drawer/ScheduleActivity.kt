package com.singularity.ipcaplus.drawer

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.utils.PreferenceHelper.userId
import com.singularity.ipcaplus.databinding.ActivityScheduleBinding
import com.singularity.ipcaplus.models.Subject
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.PreferenceHelper

class ScheduleActivity : AppCompatActivity() {

    // Variables
    var day = "seg"
    var weekButtons = arrayListOf<Button>()
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

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Horário"

        // Add all Subjects To List based on the selected day and the User Course
        val prefs = PreferenceHelper.customPreference(this, "User_data")
        Backend.getUserCourse(prefs.userId!!) {
            addSubjectsToList(it)
        }

        // Button Events
        weekButtons.add(binding.buttonSeg)
        weekButtons.add(binding.buttonTer)
        weekButtons.add(binding.buttonQua)
        weekButtons.add(binding.buttonQui)
        weekButtons.add(binding.buttonSex)
        for (bt in weekButtons)
            bt.setOnClickListener(onClickWeekDay)

        // Schedule List
        scheduleLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewSchedule.layoutManager = scheduleLayoutManager
        scheduleAdapter = ScheduleAdapter()
        binding.recyclerViewSchedule.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewSchedule.adapter = scheduleAdapter
    }


    // This function is for select an section by clicking on the section image
    private var onClickWeekDay: (view: View)->Unit = {

        val button = it as Button

        // Reset all buttons styles
        for (bt in weekButtons) {
            bt.backgroundTintList = this.resources.getColorStateList(R.color.white)
            bt.setTextColor(Color.BLACK)
        }

        // Set this button style
        button.backgroundTintList = this.resources.getColorStateList(R.color.green_200)
        button.setTextColor(Color.WHITE)

        // Reset Schedule and get the new Subjects
        day = button.text.toString().lowercase()
        val prefs = PreferenceHelper.customPreference(this, "User_data")
        Backend.getUserCourse(prefs.userId!!) {
            addSubjectsToList(it)
        }

        // if (button.currentTextColor == Color.BLACK) {

    }


    // Get All Subjects during the day
    fun addSubjectsToList(courseId: String) {
        Backend.getDayCourseSubjects(day, courseId){
            subjects.clear()
            subjects.addAll(it)
            currentIndex = 0
            scheduleAdapter?.notifyDataSetChanged()
        }
    }


    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    var currentIndex: Int = 0
    inner class ScheduleAdapter : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            if (subjects[currentIndex].name == "breaktime") {

                return ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_breaktime, parent, false)
                )
            }
            else {
                return ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_subject, parent, false)
                )
            }

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {

                if (subjects[currentIndex].name == "breaktime") {

                    val textViewBreakTimeTime = findViewById<TextView>(R.id.textViewBreakTimeTime)
                    textViewBreakTimeTime.text = "${subjects[currentIndex].start_time} min"
                    currentIndex++
                }
                else {

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

                    currentIndex++
                }
            }
        }

        override fun getItemCount(): Int {
            return subjects.size
        }
    }
}