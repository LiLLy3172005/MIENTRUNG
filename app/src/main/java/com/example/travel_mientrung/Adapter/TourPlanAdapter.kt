package com.example.travel_mientrung.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_mientrung.databinding.ItemTourPlanBinding
import com.example.travel_mientrung.dataclass.TourPlan
import com.example.travel_mientrung.layoutdetail.TourPlanDetailActivity

class TourPlanAdapter(private val tourPlans: List<TourPlan>) :
    RecyclerView.Adapter<TourPlanAdapter.TourPlanViewHolder>() {

    inner class TourPlanViewHolder(val binding: ItemTourPlanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tourPlan: TourPlan) {
            binding.titleTour.text = tourPlan.name
            Glide.with(binding.root).load(tourPlan.image_url).into(binding.imageTour)

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, TourPlanDetailActivity::class.java)
                intent.putExtra("title", tourPlan.name)
                intent.putExtra("image_url", tourPlan.image_url)
                intent.putExtra("description", tourPlan.description)
                intent.putParcelableArrayListExtra("days", ArrayList(tourPlan.days))
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourPlanViewHolder {
        val binding = ItemTourPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TourPlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TourPlanViewHolder, position: Int) {
        holder.bind(tourPlans[position])
    }

    override fun getItemCount(): Int = tourPlans.size
}
