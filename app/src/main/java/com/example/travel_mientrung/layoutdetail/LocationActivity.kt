package com.example.travel_mientrung.layoutdetail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_mientrung.Food
import com.example.travel_mientrung.FoodAdapter
import com.example.travel_mientrung.layoutdetail.FoodDetailActivity
import com.example.travel_mientrung.GridSpacingItemDecoration
import com.example.travel_mientrung.R
import com.example.travel_mientrung.ScenicSpot
import com.example.travel_mientrung.ScenicSpotAdapter
import com.example.travel_mientrung.layoutdetail.ScenicSpotDetailActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

//class LocationActivity : AppCompatActivity() {
//
//    private lateinit var imageView: ImageView
//    private lateinit var nameTextView: TextView
//    private lateinit var descriptionTextView: TextView
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: ScenicSpotAdapter
//
//    private lateinit var scrollViewOverview: ScrollView
//    private lateinit var scrollViewMaps: ScrollView
//    private lateinit var scrollViewFood: ScrollView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_location)
//
//        val locationId = intent.getStringExtra("LOCATION_ID") ?: return
//        val imageUrl = intent.getStringExtra("IMAGE_URL")
//        val name = intent.getStringExtra("NAME")
//        val description = intent.getStringExtra("DESCRIPTION")
//        imageView = findViewById(R.id.locationImageView)
//        nameTextView = findViewById(R.id.locationNameTextView)
//        descriptionTextView = findViewById(R.id.txtDescription)
//        recyclerView = findViewById(R.id.recyclerScenicSpots)
//
//        scrollViewOverview = findViewById(R.id.scrollViewOverview)
//        scrollViewMaps = findViewById(R.id.scrollViewMaps)
//        scrollViewFood = findViewById(R.id.scrollViewFood)
//
//        nameTextView.text = name
//        descriptionTextView.text = description
//        imageUrl?.let { Picasso.get().load(it).into(imageView) }
//
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        adapter = ScenicSpotAdapter()
//        recyclerView.adapter = adapter
//
//        findViewById<TextView>(R.id.tabOverview).setOnClickListener {
//            showTab("overview")
//        }
//
//        findViewById<TextView>(R.id.tabMaps).setOnClickListener {
//            showTab("maps")
//            loadScenicSpots(locationId)
//        }
//
//        findViewById<TextView>(R.id.tabPreview).setOnClickListener {
//            showTab("food")
//        }
//
//        showTab("overview") // mặc định tab Giới thiệu
//
//        val btnBack = findViewById<ImageButton>(R.id.btnBack)
//        btnBack.setOnClickListener {
//            finish() // quay lại màn hình trước đó
//        }
//
//    }
//
//    private fun showTab(tab: String) {
//        scrollViewOverview.visibility = if (tab == "overview") View.VISIBLE else View.GONE
//        scrollViewMaps.visibility = if (tab == "maps") View.VISIBLE else View.GONE
//        scrollViewFood.visibility = if (tab == "food") View.VISIBLE else View.GONE
//
//        findViewById<View>(R.id.underlineOverview).visibility = if (tab == "overview") View.VISIBLE else View.GONE
//        findViewById<View>(R.id.underlineMaps).visibility = if (tab == "maps") View.VISIBLE else View.GONE
//        findViewById<View>(R.id.underlinePreview).visibility = if (tab == "food") View.VISIBLE else View.GONE
//
//        // Cập nhật màu chữ
//        findViewById<TextView>(R.id.tabOverview).setTextColor(
//            if (tab == "overview") Color.parseColor("#000000") else Color.parseColor("#888888")
//        )
//        findViewById<TextView>(R.id.tabMaps).setTextColor(
//            if (tab == "maps") Color.parseColor("#000000") else Color.parseColor("#888888")
//        )
//        findViewById<TextView>(R.id.tabPreview).setTextColor(
//            if (tab == "food") Color.parseColor("#000000") else Color.parseColor("#888888")
//        )
//    }
//
//    private fun loadScenicSpots(locationId: String) {
//        val dbRef = FirebaseDatabase.getInstance().reference.child("15/data")
//        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val spots = mutableListOf<ScenicSpot>()
//                for (spotSnapshot in snapshot.children) {
//                    val spot = spotSnapshot.getValue(ScenicSpot::class.java)
//                    if (spot?.location_id == locationId) {
//                        spots.add(spot)
//                    }
//                }
//                adapter.setData(spots)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("Firebase", "Load failed: ${error.message}")
//            }
//        })
//    }
//}
class LocationActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScenicSpotAdapter

    private lateinit var scrollViewOverview: ScrollView
    private lateinit var scrollViewMaps: ScrollView
    private lateinit var scrollViewFood: ScrollView
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var recyclerFoods: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        val food = intent.getSerializableExtra("food_data") as? Food

        val locationId = intent.getStringExtra("LOCATION_ID") ?: return
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val name = intent.getStringExtra("NAME")
        val description = intent.getStringExtra("DESCRIPTION")
        imageView = findViewById(R.id.locationImageView)
        nameTextView = findViewById(R.id.locationNameTextView)
        descriptionTextView = findViewById(R.id.txtDescription)
        recyclerView = findViewById(R.id.recyclerScenicSpots)

        scrollViewOverview = findViewById(R.id.scrollViewOverview)
        scrollViewMaps = findViewById(R.id.scrollViewMaps)
        scrollViewFood = findViewById(R.id.scrollViewFood)

        recyclerFoods = findViewById(R.id.recyclerFoods)
        recyclerFoods.layoutManager = GridLayoutManager(this, 2)
        recyclerFoods.addItemDecoration(
            GridSpacingItemDecoration(2, 0, true)  // 2 columns, 32px spacing, include edges
        )
        foodAdapter = FoodAdapter(
            onItemClick = { food ->
                val intent = Intent(this, FoodDetailActivity::class.java)
                // Truyền từng trường riêng lẻ
                intent.putExtra("food_name", food.name)
                intent.putExtra("food_image", food.imageUrl)
                intent.putExtra("food_description", food.description)
                intent.putExtra("food_address", food.address)
                intent.putExtra("food_map_link", food.map)
                startActivity(intent)
            },
            onFavoriteClick = { food ->
                // Ví dụ: cập nhật trạng thái yêu thích
                Log.d("FAVORITE", "${food.name} yêu thích: ${food.isFavorite}")
            }
        )
        recyclerFoods.adapter = foodAdapter

        nameTextView.text = name
        descriptionTextView.text = description
        imageUrl?.let { Picasso.get().load(it).into(imageView) }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ScenicSpotAdapter { spot ->
            val intent = Intent(this, ScenicSpotDetailActivity::class.java)
            intent.putExtra("name", spot.name)
            intent.putExtra("description", spot.description)
            intent.putExtra("image_url", spot.image_url)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        findViewById<TextView>(R.id.tabOverview).setOnClickListener {
            showTab("overview")
        }

        findViewById<TextView>(R.id.tabMaps).setOnClickListener {
            showTab("maps")
            loadScenicSpots(locationId)
        }

        findViewById<TextView>(R.id.tabPreview).setOnClickListener {
            showTab("food")
            loadFoods(locationId)
        }

        showTab("overview") // mặc định tab Giới thiệu

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // quay lại màn hình trước đó
        }

    }

    private fun showTab(tab: String) {
        scrollViewOverview.visibility = if (tab == "overview") View.VISIBLE else View.GONE
        scrollViewMaps.visibility = if (tab == "maps") View.VISIBLE else View.GONE
        scrollViewFood.visibility = if (tab == "food") View.VISIBLE else View.GONE

        findViewById<View>(R.id.underlineOverview).visibility = if (tab == "overview") View.VISIBLE else View.GONE
        findViewById<View>(R.id.underlineMaps).visibility = if (tab == "maps") View.VISIBLE else View.GONE
        findViewById<View>(R.id.underlinePreview).visibility = if (tab == "food") View.VISIBLE else View.GONE

        // Cập nhật màu chữ
        findViewById<TextView>(R.id.tabOverview).setTextColor(
            if (tab == "overview") Color.parseColor("#000000") else Color.parseColor("#888888")
        )
        findViewById<TextView>(R.id.tabMaps).setTextColor(
            if (tab == "maps") Color.parseColor("#000000") else Color.parseColor("#888888")
        )
        findViewById<TextView>(R.id.tabPreview).setTextColor(
            if (tab == "food") Color.parseColor("#000000") else Color.parseColor("#888888")
        )
    }

    private fun loadScenicSpots(locationId: String) {
        val dbRef = FirebaseDatabase.getInstance().reference.child("15/data")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val spots = mutableListOf<ScenicSpot>()
                for (spotSnapshot in snapshot.children) {
                    val spot = spotSnapshot.getValue(ScenicSpot::class.java)
                    if (spot?.location_id == locationId) {
                        spots.add(spot)
                    }
                }
                adapter.setData(spots)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Load failed: ${error.message}")
            }
        })
    }
    private fun loadFoods(locationId: String) {
        val dbRef = FirebaseDatabase.getInstance().reference.child("10/data")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val foodList = mutableListOf<Food>()
                for (foodSnapshot in snapshot.children) {
                    val food = foodSnapshot.getValue(Food::class.java)
                    if (food?.location_id == locationId) {
                        foodList.add(food)
                    }
                }
                foodAdapter.submitList(foodList)  // ✅ Đúng
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Load foods failed: ${error.message}")
            }
        })
    }

}


