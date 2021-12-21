package com.singularity.ipcaplus.calendar

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.Utilis
import com.singularity.ipcaplus.databinding.FragmentCalendarBinding
import com.singularity.ipcaplus.models.EventCalendar
import java.util.*
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.singularity.ipcaplus.Backend
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class CalendarFragment : Fragment() {

    var events = arrayListOf<EventCalendar>()
    private var eventAdapter: RecyclerView.Adapter<*>? = null
    private var eventLayoutManager: LinearLayoutManager? = null
    private var _binding: FragmentCalendarBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    /*
        This function create the view
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Create the layout for this fragment
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set Current Date
        binding.monthTitle.text = Utilis.getMonthById(Utilis.getCurrentMonthId())
        binding.yearTitle.text = Utilis.getCurrentYear()
        binding.compactcalendarView.setCurrentDate(Date())

        // Get This Month Events
        addAllMonthEvents(binding.monthTitle.text.toString())

        // Add Event Button
        binding.fabAddEvent.setOnClickListener {
            val intent = Intent(activity, AddEventActivity::class.java)
            startActivity(intent)
        }

        // Calendar Interactions Events
        binding.compactcalendarView.setListener(object : CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {

                // Show All Selected day Events
                addAllDayEvents(binding.monthTitle.text.toString(), dateClicked.date)

            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                binding.monthTitle.text = Utilis.getMonthById(firstDayOfNewMonth.month+1)
                binding.yearTitle.text = Utilis.getYearByCalendarId(firstDayOfNewMonth.year).toString()

                // Refresh with new Month Events
                addAllMonthEvents(binding.monthTitle.text.toString())
            }
        })

        // Event List
        eventLayoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        binding.recycleViewEvents.layoutManager = eventLayoutManager
        eventAdapter = EventAdapter()
        binding.recycleViewEvents.itemAnimator = DefaultItemAnimator()
        binding.recycleViewEvents.adapter = eventAdapter

        return root
    }


    /*
       This function configures the fragment after its creation
    */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun addAllMonthEvents(month: String) {
        Backend.getAllMonthEvents (month) { allEvents ->
            events.clear()
            events.addAll(allEvents)
            eventAdapter?.notifyDataSetChanged()

            // Add Icons into the calendar
            // Bugado <----------- vvvvvvvvvvv
            for (event in events) {
                val ev = Event(0,event.datetime.seconds * 1000, event.desc)
                binding.compactcalendarView.addEvent(ev)
            }
        }
    }


    fun addAllDayEvents(month: String, day: Int) {
        Backend.getAllMonthDayEvents (month, day) { allEvents ->
            events.clear()
            events.addAll(allEvents)
            eventAdapter?.notifyDataSetChanged()
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

                // Set data
                val date = Utilis.getDate(events[position].datetime.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                textViewDay.text = Utilis.getDay(date)
                textViewName.text = events[position].name
                textViewDesc.text = events[position].desc
                textViewHour.text = Utilis.getHours(date) + ":" + Utilis.getMinutes(date)

            }
        }

        override fun getItemCount(): Int {
            return events.size
        }
    }
}