package com.singularity.ipcaplus.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.models.Contact

class ExpandableListViewAdapter internal constructor(private val context: Context, private val chapterList: List<String>, private val topicsList: HashMap<String, List<Contact>>):
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return chapterList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return this.topicsList[this.chapterList[groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return chapterList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return this.topicsList[this.chapterList[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView
        val name = getGroup(groupPosition) as String

        if (convertView == null) {
            val  inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.row_parent_contact, null)
        }

        val textViewName = convertView!!.findViewById<TextView>(R.id.textViewName)
        textViewName.text = name

        return convertView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView
        val info = getChild(groupPosition, childPosition) as Contact

        if (convertView == null) {
            val  inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.row_child_contact, null)
        }
        val textViewDesc = convertView!!.findViewById<TextView>(R.id.textViewDesc)
        val textViewLocation = convertView!!.findViewById<TextView>(R.id.textViewLocation)
        val textViewNumber = convertView!!.findViewById<TextView>(R.id.textViewNumber)
        val textViewEmail = convertView!!.findViewById<TextView>(R.id.textViewEmail)
        val textViewSite = convertView!!.findViewById<TextView>(R.id.textViewSite)
        textViewDesc.text = info.desc
        textViewLocation.text = info.location
        textViewNumber.text = info.number
        textViewEmail.text = info.email
        textViewSite.text = info.site

        return convertView
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

}