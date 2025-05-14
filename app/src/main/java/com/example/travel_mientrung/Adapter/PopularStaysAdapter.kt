package com.example.travel_mientrung

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_mientrung.databinding.ItemPopularStayBinding
import com.example.travel_mientrung.layoutdetail.LocationActivity
import com.squareup.picasso.Picasso

class PopularStaysAdapter : RecyclerView.Adapter<PopularStaysAdapter.PopularStayViewHolder>() {
    var popularStaysList = mutableListOf<PopularStay>()
    private var onItemClick: ((PopularStay) -> Unit)? = null
    private var onFavoriteClick: ((PopularStay) -> Unit)? = null

    fun setOnFavoriteClickListener(listener: (PopularStay) -> Unit) {
        onFavoriteClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularStayViewHolder {
        val binding = ItemPopularStayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PopularStayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularStayViewHolder, position: Int) {
        val item = popularStaysList[position]
        holder.bind(item)
    }

    override fun getItemCount() = popularStaysList.size

    fun submitList(newList: List<PopularStay>) {
        popularStaysList.clear()
        popularStaysList.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateItem(position: Int, stay: PopularStay) {
        if (position in 0 until popularStaysList.size) {
            popularStaysList[position] = stay
            notifyItemChanged(position)
        }
    }

    inner class PopularStayViewHolder(private val binding: ItemPopularStayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PopularStay) {
            Picasso.get().load(item.imageUrl).into(binding.imageView)
            binding.nameTextView.text = item.name

            // Cập nhật icon trái tim dựa trên trạng thái yêu thích
            val heartIcon = if (item.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            binding.favoriteButton.setImageResource(heartIcon)

            binding.favoriteButton.setOnClickListener {
                item.isFavorite = !item.isFavorite
                onFavoriteClick?.invoke(item)
                notifyItemChanged(adapterPosition)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, LocationActivity::class.java).apply {
                    putExtra("IMAGE_URL", item.imageUrl)
                    putExtra("NAME", item.name)
                    putExtra("LOCATION_ID", item.id)
                    putExtra("DESCRIPTION", item.description)
                }
                context.startActivity(intent)
            }
        }
    }
}



//class PopularStaysAdapter : RecyclerView.Adapter<PopularStaysAdapter.PopularStayViewHolder>() {
//    private var popularStaysList = mutableListOf<PopularStay>()
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularStayViewHolder {
//        val binding = ItemPopularStayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return PopularStayViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: PopularStayViewHolder, position: Int) {
//        val item = popularStaysList[position]
//        holder.bind(item)
//    }
//
//    override fun getItemCount() = popularStaysList.size
//
//    fun submitList(newList: List<PopularStay>) {
//        popularStaysList.clear()
//        popularStaysList.addAll(newList)
//        notifyDataSetChanged()
//    }
//
//    class PopularStayViewHolder(private val binding: ItemPopularStayBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(item: PopularStay) {
//            Picasso.get().load(item.imageUrl).into(binding.imageView)
//            binding.nameTextView.text = item.name
//
//            // Set click listener to navigate to ActivityLocation
//            itemView.setOnClickListener {
//                val context = itemView.context
//                val intent = Intent(context, LocationActivity::class.java)
//                intent.putExtra("IMAGE_URL", item.imageUrl)
//                intent.putExtra("NAME", item.name)
//
//                context.startActivity(intent)
//            }
//        }
//    }
//}
//1
//class PopularStaysAdapter : RecyclerView.Adapter<PopularStaysAdapter.PopularStayViewHolder>() {
//    private var popularStaysList = mutableListOf<PopularStay>()
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularStayViewHolder {
//        val binding = ItemPopularStayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return PopularStayViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: PopularStayViewHolder, position: Int) {
//        val item = popularStaysList[position]
//        holder.bind(item)
//    }
//
//    override fun getItemCount() = popularStaysList.size
//
//    fun submitList(newList: List<PopularStay>) {
//        popularStaysList.clear()
//        popularStaysList.addAll(newList)
//        notifyDataSetChanged()
//    }
//
//    class PopularStayViewHolder(private val binding: ItemPopularStayBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(item: PopularStay) {
//            Picasso.get().load(item.imageUrl).into(binding.imageView)
//            binding.nameTextView.text = item.name
//        }
//    }
//}

