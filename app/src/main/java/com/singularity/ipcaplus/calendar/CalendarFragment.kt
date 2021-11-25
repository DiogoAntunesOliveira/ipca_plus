package com.singularity.ipcaplus.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.FragmentCalendarBinding
import com.singularity.ipcaplus.models.Event
import com.singularity.ipcaplus.models.Message

class CalendarFragment : Fragment() {

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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Create the layout for this fragment
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        events.add(Event())
        events.add(Event())
        events.add(Event())
        events.add(Event())

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

                //val textViewMessage = findViewById<TextView>(R.id.textViewMessage)
                //textViewMessage.text = events[position].name

            }
        }

        override fun getItemCount(): Int {
            return events.size
        }
    }
}