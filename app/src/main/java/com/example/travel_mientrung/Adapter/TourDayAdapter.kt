package com.example.travel_mientrung.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_mientrung.R
import com.example.travel_mientrung.dataclass.TourDay

class TourDayAdapter(private val dayList: List<TourDay>) :
    RecyclerView.Adapter<TourDayAdapter.DayViewHolder>() {

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTitle: TextView = itemView.findViewById(R.id.dayTitle)
        val activityList: TextView = itemView.findViewById(R.id.activityList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tour_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = dayList[position]
        holder.dayTitle.text = day.title
        holder.activityList.text = day.activities.joinToString("\n• ", prefix = "• ")
    }

    override fun getItemCount(): Int = dayList.size
}
