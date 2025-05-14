package com.example.travel_mientrung

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_mientrung.databinding.ItemFestivalBinding
import com.squareup.picasso.Picasso

class FestivalAdapter(private val festivals: List<Festival>) :
    RecyclerView.Adapter<FestivalAdapter.FestivalViewHolder>() {

    inner class FestivalViewHolder(val binding: ItemFestivalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FestivalViewHolder {
        val binding = ItemFestivalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FestivalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FestivalViewHolder, position: Int) {
        val festival = festivals[position]
        with(holder.binding) {
            textViewFestivalName.text = festival.name
            textViewFestivalDescription.text = festival.description
            Glide.with(imageViewFestival.context)
                .load(festival.imageUrl)
                .into(imageViewFestival)
        }
    }

    override fun getItemCount(): Int = festivals.size
}
