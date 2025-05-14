package com.example.travel_mientrung

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ScenicSpotAdapter(private val onItemClick: (ScenicSpot) -> Unit) : RecyclerView.Adapter<ScenicSpotAdapter.SpotViewHolder>() {
    private var spotList = listOf<ScenicSpot>()

    fun setData(list: List<ScenicSpot>) {
        spotList = list
        notifyDataSetChanged()
    }

    class SpotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgSpot: ImageView = itemView.findViewById(R.id.imgSpot)
        val txtName: TextView = itemView.findViewById(R.id.txtSpotName)
        val txtDescription: TextView = itemView.findViewById(R.id.txtSpotDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scenic_spot, parent, false)
        return SpotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val spot = spotList[position]
        holder.txtName.text = spot.name
        holder.txtDescription.text = spot.description
        Picasso.get().load(spot.image_url).into(holder.imgSpot)

        holder.itemView.setOnClickListener {
            onItemClick(spot)
        }
    }

    override fun getItemCount(): Int = spotList.size
}

//class ScenicSpotAdapter : RecyclerView.Adapter<ScenicSpotAdapter.SpotViewHolder>() {
//
//    private var spotList = listOf<ScenicSpot>()
//
//    fun setData(list: List<ScenicSpot>) {
//        spotList = list
//        notifyDataSetChanged()
//    }
//
//    class SpotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imgSpot: ImageView = itemView.findViewById(R.id.imgSpot)
//        val txtName: TextView = itemView.findViewById(R.id.txtSpotName)
//        val txtDescription: TextView = itemView.findViewById(R.id.txtSpotDescription)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scenic_spot, parent, false)
//        return SpotViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
//        val spot = spotList[position]
//        holder.txtName.text = spot.name
//        holder.txtDescription.text = spot.description
//        Picasso.get().load(spot.image_url).into(holder.imgSpot)
//
//    }
//
//    override fun getItemCount(): Int = spotList.size
//}
