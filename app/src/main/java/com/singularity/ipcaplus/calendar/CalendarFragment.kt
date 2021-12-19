package com.singularity.ipcaplus.calendar

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.Utilis
import com.singularity.ipcaplus.databinding.FragmentCalendarBinding
import com.singularity.ipcaplus.models.Event
import com.singularity.ipcaplus.models.Message

class CalendarFragment : Fragment() {

    val db = Firebase.firestore
    var events = arrayListOf<Event>()
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

        // Get Month
        binding.monthTitle.text = Utilis.getMonthById(Utilis.getCurrentMonthId())
        // Get Year

        // Set the calendar to this date

        // Get This Month Events <- all months
        db.collection("event")
            .addSnapshotListener { documents, e ->

                documents?.let {
                    events.clear()
                    for (document in it) {
                        val event = Event.fromHash(document)
                        events.add(event)
                    }
                    eventAdapter?.notifyDataSetChanged()
                }

            }

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
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                textViewDay.text = events[position].day
                textViewName.text = events[position].name
                textViewDesc.text = events[position].desc
                textViewHour.text = events[position].hour

            }
        }

        override fun getItemCount(): Int {
            return events.size
        }
    }
}