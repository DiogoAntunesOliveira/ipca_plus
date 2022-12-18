package com.singularity.ipcaplus.drawer

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivityCalendarBinding
import com.singularity.ipcaplus.models.EventCalendar
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Utilis
import java.util.*

class CalendarActivity : AppCompatActivity() {

    var isAdmin = false
    lateinit var chat_id: String
    var events = arrayListOf<EventCalendar>()
    private var eventAdapter: RecyclerView.Adapter<*>? = null
    private var eventLayoutManager: LinearLayoutManager? = null
    private lateinit var binding: ActivityCalendarBinding

    // This property is only valid between onCreateView and
    // onDestroyView.

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stay)

        // Create the layout for this fragment
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Calendario"
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_bar_layout)

        findViewById<TextView>(R.id.AppBarTittle).text = "Calendario"
        // Back button
        findViewById<ImageView>(R.id.BackButtonImageView).setOnClickListener {
            finish()
        }

        // Set Current Date
        binding.monthTitle.text = Utilis.getMonthById(Utilis.getCurrentMonthId())
        binding.yearTitle.text = Utilis.getCurrentYear()
        binding.compactcalendarView.setCurrentDate(Date())

        // Get chat id
        chat_id =
            if (intent.hasExtra("chat_id")) intent.getStringExtra("chat_id").toString() else "none"

        // Get All Events This Month
        if (chat_id == "none") {
            addAllMonthEvents(binding.monthTitle.text.toString())
            placeCustomCalendarPinsGeneral()
        } else {
            addAllChatMonthEvents(binding.monthTitle.text.toString(), chat_id)
            placeCustomCalendarPinsChat()
        }

        // Check if Player is Chat Admin
        if (chat_id != "none") {
            isAdmin = intent.getBooleanExtra("is_admin", false)
        }

        // Add Event Button
        if (chat_id == "none") {
            binding.fabAddEvent.visibility = View.GONE
        } else {
            binding.fabAddEvent.setOnClickListener {
                val intent = Intent(this, AddEventActivity::class.java)
                intent.putExtra("chat_id", chat_id)
                startActivity(intent)
            }
        }

        // Calendar Interactions Events
        binding.compactcalendarView.setListener(object :
            CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {

                // Show All Selected day Events
                if (chat_id == "none") {
                    addAllDayEvents(binding.monthTitle.text.toString(), dateClicked.date)
                    placeCustomCalendarPinsGeneral()
                } else {
                    addAllChatDayEvents(binding.monthTitle.text.toString(),
                        dateClicked.date,
                        chat_id)
                    placeCustomCalendarPinsChat()
                }
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                binding.monthTitle.text = Utilis.getMonthById(firstDayOfNewMonth.month + 1)
                binding.yearTitle.text =
                    Utilis.getYearByCalendarId(firstDayOfNewMonth.year).toString()

                // Refresh with new Month Events
                if (chat_id == "none") {
                    addAllMonthEvents(binding.monthTitle.text.toString())
                    placeCustomCalendarPinsGeneral()
                } else {
                    addAllChatMonthEvents(binding.monthTitle.text.toString(), chat_id)
                    placeCustomCalendarPinsChat()
                }
            }
        })

        // Event List
        eventLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycleViewEvents.layoutManager = eventLayoutManager
        eventAdapter = EventAdapter()
        binding.recycleViewEvents.itemAnimator = DefaultItemAnimator()
        binding.recycleViewEvents.adapter = eventAdapter
    }

    override fun onResume() {
        super.onResume()

        // Get All Events This Month
        if (chat_id == "none") {
            addAllMonthEvents(binding.monthTitle.text.toString())
            placeCustomCalendarPinsGeneral()
        } else {
            addAllChatMonthEvents(binding.monthTitle.text.toString(), chat_id)
            placeCustomCalendarPinsChat()
        }

    }


    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    fun addAllMonthEvents(month: String) {
        Backend.getAllMonthEvents(month) { allEvents ->
            events.clear()
            events.addAll(allEvents)
            eventAdapter?.notifyDataSetChanged()
        }
    }


    fun addAllChatMonthEvents(month: String, chat_id: String) {
        Backend.getAllChatMonthEvents(month, chat_id) { allEvents ->
            events.clear()
            events.addAll(allEvents)
            eventAdapter?.notifyDataSetChanged()
        }
    }


    fun addAllDayEvents(month: String, day: Int) {
        Backend.getAllMonthDayEvents(month, day) { allEvents ->
            events.clear()
            events.addAll(allEvents)
            eventAdapter?.notifyDataSetChanged()
        }
    }


    fun addAllChatDayEvents(month: String, day: Int, chat_id: String) {
        Backend.getAllChatMonthDayEvents(month, day, chat_id) { allEvents ->
            events.clear()
            events.addAll(allEvents)
            eventAdapter?.notifyDataSetChanged()
        }
    }


    private fun placeCustomCalendarPinsGeneral() {
        Backend.getAllMonthEvents(binding.monthTitle.text.toString()) { allEvents ->
            binding.compactcalendarView.removeAllEvents()
            for (event in allEvents) {
                val ev1 = Event(Color.parseColor("#037B4A"), event.datetime.seconds * 1000)
                binding.compactcalendarView.addEvent(ev1)
            }
        }
    }


    private fun placeCustomCalendarPinsChat() {
        Backend.getAllChatMonthEvents(binding.monthTitle.text.toString(), chat_id) { allEvents ->
            binding.compactcalendarView.removeAllEvents()
            for (event in allEvents) {
                val ev1 = Event(Color.parseColor("#037B4A"), event.datetime.seconds * 1000)
                binding.compactcalendarView.addEvent(ev1)
            }
        }
    }


    inner class EventAdapter : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_event, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {

                // Get data
                val textViewDay = findViewById<TextView>(R.id.day_textview)
                val textViewName = findViewById<TextView>(R.id.name_textview)
                val textViewDesc = findViewById<TextView>(R.id.desc_textview)
                val textViewHour = findViewById<TextView>(R.id.hour_textview)
                val deleteButton = findViewById<ImageView>(R.id.deleteButton)

                // Set data
                val date = Utilis.getDate(events[position].datetime.seconds * 1000,
                    "yyyy-MM-dd'T'HH:mm:ss.SSS")
                textViewDay.text = Utilis.getDay(date)
                textViewName.text = events[position].name
                textViewDesc.text = events[position].desc
                textViewHour.text = Utilis.getHours(date) + ":" + Utilis.getMinutes(date)

                if (!isAdmin)
                    deleteButton.visibility = View.GONE

                deleteButton.setOnClickListener {
                    Backend.deleteEvent(chat_id, events[position].id)
                    addAllChatMonthEvents(binding.monthTitle.text.toString(), chat_id)
                }

            }
        }

        override fun getItemCount(): Int {
            return events.size
        }
    }
}