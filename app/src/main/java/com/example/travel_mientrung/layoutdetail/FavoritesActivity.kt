package com.example.travel_mientrung.layoutdetail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_mientrung.Adapter.FavoriteManager
import com.example.travel_mientrung.Food
import com.example.travel_mientrung.PopularStay
import com.example.travel_mientrung.R
import com.example.travel_mientrung.layoutmain.HomeActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class FavoritesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritesAdapter
    private lateinit var favoriteManager: FavoriteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            finish()
            return
        }

        favoriteManager = FavoriteManager(currentUser.uid)

        recyclerView = findViewById(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FavoritesAdapter()
        recyclerView.adapter = adapter

        loadFavorites()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_favorites // Làm icon Search tím lên

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_search -> {
                    startActivity(Intent(this, BlogActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }

    }


    private fun loadFavorites() {
        favoriteManager.getFavorites { favorites ->
            adapter.submitList(favorites)
        }
    }

    inner class FavoritesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var favorites = listOf<Pair<String, Any>>()

        fun submitList(newList: List<Pair<String, Any>>) {
            favorites = newList
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return when (favorites[position].first) {
                "location" -> 0
                "food" -> 1
                else -> -1
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_favorite_location, parent, false)
                    LocationViewHolder(view)
                }
                1 -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_favorite_food, parent, false)
                    FoodViewHolder(view)
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is LocationViewHolder -> {
                    val stay = favorites[position].second as PopularStay
                    holder.bind(stay)
                }
                is FoodViewHolder -> {
                    val food = favorites[position].second as Food
                    holder.bind(food)
                }
            }
        }

        override fun getItemCount(): Int = favorites.size

        inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            private val imageView: ImageView = itemView.findViewById(R.id.imageView)
            private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)

            fun bind(stay: PopularStay) {
                nameTextView.text = stay.name
                Picasso.get().load(stay.imageUrl).into(imageView)
                favoriteButton.setImageResource(R.drawable.ic_heart_filled)

                favoriteButton.setOnClickListener {
                    favoriteManager.removeFavoriteLocation(stay.id) { success ->
                        if (success) {
                            val newList = favorites.toMutableList()
                            newList.removeAt(adapterPosition)
                            submitList(newList)
                        } else {
                            Toast.makeText(this@FavoritesActivity, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                itemView.setOnClickListener {
                    val intent = Intent(this@FavoritesActivity, LocationActivity::class.java)
                    intent.putExtra("LOCATION_ID", stay.id)
                    intent.putExtra("IMAGE_URL", stay.imageUrl)
                    intent.putExtra("NAME", stay.name)
                    intent.putExtra("DESCRIPTION", stay.description)
                    startActivity(intent)
                }
            }
        }

        inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            private val imageView: ImageView = itemView.findViewById(R.id.imageView)
            private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)

            fun bind(food: Food) {
                nameTextView.text = food.name
                Picasso.get().load(food.imageUrl).into(imageView)
                favoriteButton.setImageResource(R.drawable.ic_heart_filled)

                favoriteButton.setOnClickListener {
                    favoriteManager.removeFavoriteFood(food.name) { success ->
                        if (success) {
                            val newList = favorites.toMutableList()
                            newList.removeAt(adapterPosition)
                            submitList(newList)
                        } else {
                            Toast.makeText(this@FavoritesActivity, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                itemView.setOnClickListener {
                    val intent = Intent(this@FavoritesActivity, FoodDetailActivity::class.java)
                    intent.putExtra("food_name", food.name)
                    intent.putExtra("food_image", food.imageUrl)
                    intent.putExtra("food_description", food.description)
                    intent.putExtra("food_address", food.address)
                    intent.putExtra("food_map_link", food.map)
                    startActivity(intent)
                }
            }
        }
    }
}