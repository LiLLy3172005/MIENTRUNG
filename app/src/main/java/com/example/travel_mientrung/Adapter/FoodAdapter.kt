package com.example.travel_mientrung

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.squareup.picasso.Picasso
import com.example.travel_mientrung.databinding.ItemFoodBinding
import com.example.travel_mientrung.layoutdetail.FoodDetailActivity

class FoodAdapter(
    private val onItemClick: (Food) -> Unit,
    private val onFavoriteClick: (Food) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private val foodList = mutableListOf<Food>()

    // Sửa phương thức setData thành submitList
    fun submitList(newList: List<Food>) {
        foodList.clear()
        foodList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getFoodList(): List<Food> {
        return foodList
    }


    // Thêm phương thức updateItem
    fun updateItem(position: Int, food: Food) {
        if (position in 0 until foodList.size) {
            foodList[position] = food
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foodList[position])
    }

    override fun getItemCount(): Int = foodList.size

    inner class FoodViewHolder(private val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(food: Food) {
            binding.foodNameTextView.text = food.name
            Picasso.get().load(food.imageUrl).into(binding.imageViewFood)

            // Update favorite icon based on current state
            updateFavoriteIcon(food.isFavorite)

            binding.favoriteButton.setOnClickListener {
                val newFavoriteState = !food.isFavorite
                food.isFavorite = newFavoriteState
                updateFavoriteIcon(newFavoriteState)
                onFavoriteClick(food)
            }

            binding.root.setOnClickListener {
                onItemClick(food)
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            val heartIcon = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            binding.favoriteButton.setImageResource(heartIcon)
        }
    }
}

//class FoodAdapter(private val onItemClick: (Food) -> Unit) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {
//    private var foodList = mutableListOf<Food>()
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
//        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return FoodViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
//        val item = foodList[position]
//        holder.bind(item)
//        holder.itemView.setOnClickListener {
//            onItemClick(item)
//        }
//    }
//
//    override fun getItemCount() = foodList.size
//
//    fun submitList(newList: List<Food>) {
//        foodList.clear()
//        foodList.addAll(newList)
//        notifyDataSetChanged()
//    }
//
//    class FoodViewHolder(private val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(item: Food) {
//            Picasso.get().load(item.imageUrl).into(binding.imageViewFood)
//            binding.foodNameTextView.text = item.name
//        }
//    }
//}
