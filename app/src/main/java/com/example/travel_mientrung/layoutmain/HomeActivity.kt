package com.example.travel_mientrung.layoutmain

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_mientrung.Adapter.TourPlanAdapter
import com.example.travel_mientrung.Festival
import com.example.travel_mientrung.FestivalAdapter
import com.example.travel_mientrung.Food
import com.example.travel_mientrung.FoodAdapter
import com.example.travel_mientrung.GridSpacingItemDecoration
import com.example.travel_mientrung.PopularStay
import com.example.travel_mientrung.PopularStaysAdapter
import com.example.travel_mientrung.R
import com.example.travel_mientrung.dataclass.TourDay
import com.example.travel_mientrung.dataclass.TourPlan
import com.example.travel_mientrung.layoutdetail.BlogActivity
import com.example.travel_mientrung.layoutdetail.FavoritesActivity
import com.example.travel_mientrung.layoutdetail.FoodDetailActivity
import com.example.travel_mientrung.layoutdetail.LocationActivity
import com.example.travel_mientrung.layoutdetail.ProfileActivity
import com.example.travel_mientrung.layoutdetail.ScenicSpotDetailActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var favoriteManager: FavoriteManager

    private lateinit var popularStaysRecyclerView: RecyclerView
    private lateinit var popularStaysAdapter: PopularStaysAdapter

    private lateinit var popularFoodRecyclerView: RecyclerView
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var festivalRecyclerView: RecyclerView
    private lateinit var festivalAdapter: FestivalAdapter
    private val festivalList = mutableListOf<Festival>()

    private var currentPage = 0
    private val itemsPerPage = 4
    private val foodList = mutableListOf<Food>()
    private lateinit var imageView: ImageView
    private val backgroundImages = arrayOf(
        R.drawable.bb2,
        R.drawable.bb3,
        R.drawable.bb5,
        R.drawable.bb6,
    )
    private var currentImageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        imageView = findViewById(R.id.imageView)
        auth = FirebaseAuth.getInstance()
        favoriteManager = FavoriteManager(auth.currentUser?.uid ?: "")
        changeBackgroundImagePeriodically()

        // Initialize RecyclerViews
        popularStaysRecyclerView = findViewById(R.id.popularStaysRecyclerView)
        popularFoodRecyclerView = findViewById(R.id.popularFoodRecyclerView)

        tourPlanRecyclerView = findViewById(R.id.tourPlanRecyclerView)
        tourPlanRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // Set up LayoutManagers
        popularStaysRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        popularFoodRecyclerView.layoutManager = GridLayoutManager(this, 2)
        popularFoodRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(2, 0, true)
        )

        // Initialize Adapters
        // Bằng:
        popularStaysAdapter = PopularStaysAdapter().also { adapter ->
            adapter.setOnFavoriteClickListener { stay ->
                val position = adapter.popularStaysList.indexOfFirst { item -> item.id == stay.id }
                if (stay.isFavorite) {
                    favoriteManager.addFavoriteLocation(stay) { success ->
                        if (!success) {
                            runOnUiThread {
                                stay.isFavorite = false
                                if (position != -1) {
                                    adapter.updateItem(position, stay)
                                }
                            }
                        }
                    }
                } else {
                    favoriteManager.removeFavoriteLocation(stay.id) { success ->
                        if (!success) {
                            runOnUiThread {
                                stay.isFavorite = true
                                if (position != -1) {
                                    adapter.updateItem(position, stay)
                                }
                            }
                        }
                    }
                }
            }
        }
        foodAdapter = FoodAdapter(
            onItemClick = { food ->
                // Ví dụ chuyển qua màn hình chi tiết món ăn
                val intent = Intent(this, FoodDetailActivity::class.java)
                intent.putExtra("food_name", food.name)
                intent.putExtra("food_image", food.imageUrl)
                intent.putExtra("food_description", food.description)
                intent.putExtra("food_address", food.address)
                intent.putExtra("food_map_link", food.map)
                startActivity(intent)
            },
            onFavoriteClick = { food ->
                val position = foodList.indexOf(food)
                if (food.isFavorite) {
                    favoriteManager.addFavoriteFood(food) { success ->
                        if (!success) {
                            food.isFavorite = false
                            runOnUiThread {
                                if (position != -1) {
                                    foodAdapter.updateItem(position, food)
                                }
                            }
                        }
                    }
                } else {
                    favoriteManager.removeFavoriteFood(food.name) { success ->
                        if (!success) {
                            food.isFavorite = true
                            runOnUiThread {
                                if (position != -1) {
                                    foodAdapter.updateItem(position, food)
                                }
                            }
                        }
                    }
                }
            }
        )

        festivalRecyclerView = findViewById(R.id.festivalRecyclerView)
        festivalAdapter = FestivalAdapter(festivalList)
        festivalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        festivalRecyclerView.adapter = festivalAdapter

        // Set the adapters to RecyclerViews
        popularStaysRecyclerView.adapter = popularStaysAdapter
        popularFoodRecyclerView.adapter = foodAdapter

        // Load data from Firebase
        loadPopularStaysData()
        loadPopularFoodData()
        loadFestivalData()
        loadTourPlansData()
        // Show welcome message
        showWelcomeMessage()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
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

        // Handle Next/Previous buttons
        findViewById<Button>(R.id.nextPageButton).setOnClickListener {
            if ((currentPage + 1) * itemsPerPage < foodList.size) {
                currentPage++
                updateFoodList()
            }
        }

        findViewById<Button>(R.id.previousPageButton).setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateFoodList()
            }
        }

        val searchButton = findViewById<ImageButton>(R.id.button2)
        val searchEditText = findViewById<EditText>(R.id.editTextText)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchContent(query)
            } else {
                Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeBackgroundImagePeriodically() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                imageView.setImageResource(backgroundImages[currentImageIndex])
                currentImageIndex = (currentImageIndex + 1) % backgroundImages.size
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(runnable)
    }

    private fun searchFood(query: String, callback: (Boolean) -> Unit) {
        val normalizedQuery = query.lowercase().replace(" ", "_")
        val foodRef = FirebaseDatabase.getInstance().getReference("10/data")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val normalizedName = name.lowercase().replace(" ", "_")

                    if (normalizedName.contains(normalizedQuery)) {
                        val imageUrl = child.child("image_url").getValue(String::class.java) ?: ""
                        val description = child.child("description").getValue(String::class.java) ?: ""
                        val address = child.child("address").getValue(String::class.java) ?: ""
                        val mapLink = child.child("map").getValue(String::class.java) ?: ""

                        val intent = Intent(this@HomeActivity, FoodDetailActivity::class.java)
                        intent.putExtra("food_name", name)
                        intent.putExtra("food_image", imageUrl)
                        intent.putExtra("food_description", description)
                        intent.putExtra("food_address", address)
                        intent.putExtra("food_map_link", mapLink)
                        startActivity(intent)
                        callback(true)
                        return
                    }
                }
                callback(false)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }


    private fun searchScenicSpot(query: String, callback: (Boolean) -> Unit) {
        val normalizedQuery = query.lowercase().replace(" ", "_")
        val spotRef = FirebaseDatabase.getInstance().getReference("15/data")

        spotRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val normalizedName = name.lowercase().replace(" ", "_")

                    if (normalizedName.contains(normalizedQuery)) {
                        val imageUrl = child.child("image_url").getValue(String::class.java) ?: ""
                        val description = child.child("description").getValue(String::class.java) ?: ""

                        val intent = Intent(this@HomeActivity, ScenicSpotDetailActivity::class.java)
                        intent.putExtra("name", name)
                        intent.putExtra("image_url", imageUrl)
                        intent.putExtra("description", description)
                        startActivity(intent)
                        callback(true)
                        return
                    }
                }
                callback(false)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    private fun searchContent(query: String) {
        val normalizedQuery = query.lowercase().replace(" ", "_")
        var found = false

        val locationRef = FirebaseDatabase.getInstance().getReference("11/data")
        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val normalizedName = name.lowercase().replace(" ", "_")

                    if (normalizedName.contains(normalizedQuery)) {
                        found = true
                        val id = child.child("location_id").getValue(String::class.java) ?: ""
                        val imageUrl = child.child("image_url").getValue(String::class.java) ?: ""
                        val description = child.child("description").getValue(String::class.java) ?: ""

                        val intent = Intent(this@HomeActivity, LocationActivity::class.java)
                        intent.putExtra("LOCATION_ID", id)
                        intent.putExtra("IMAGE_URL", imageUrl)
                        intent.putExtra("NAME", name)
                        intent.putExtra("DESCRIPTION", description)
                        startActivity(intent)
                        return
                    }
                }

                // Nếu không tìm thấy trong địa điểm, tìm trong món ăn
                searchFood(query) { foodFound ->
                    if (!foodFound) {
                        searchScenicSpot(query) { spotFound ->
                            if (!spotFound) {
                                Toast.makeText(this@HomeActivity, "Không tìm thấy kết quả phù hợp", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Lỗi khi tìm kiếm địa điểm", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showWelcomeMessage() {
        auth.currentUser?.email?.let { email ->
            Toast.makeText(this, "Chào mừng $email đến với trang chủ!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPopularStaysData() {
        val popularStaysList = mutableListOf<PopularStay>()
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("11/data")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val id = child.child("location_id").getValue(String::class.java) ?: ""
                    val name = child.child("name").getValue(String::class.java) ?: "Không rõ"
                    val imageUrl = child.child("image_url").getValue(String::class.java) ?: ""
                    val description =
                        child.child("description").getValue(String::class.java) ?: "Không có mô tả"

                    popularStaysList.add(PopularStay(id, name, imageUrl, description))
                }

                favoriteManager.checkFavoriteLocations(popularStaysList) { updatedList ->
                    popularStaysAdapter.submitList(updatedList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Lỗi tải danh sách popular stays", error.toException())
            }
        })
    }

    private fun loadPopularFoodData() {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("10/data")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                foodList.clear()
                for (child in snapshot.children) {
                    val name = child.child("name").getValue(String::class.java) ?: "Không rõ"
                    val imageUrl = child.child("image_url").getValue(String::class.java) ?: ""
                    val description =
                        child.child("description").getValue(String::class.java) ?: "Không có mô tả"
                    val address =
                        child.child("address").getValue(String::class.java) ?: "Không rõ địa chỉ"
                    val mapLink = child.child("map").getValue(String::class.java) ?: ""

                    foodList.add(Food(imageUrl, name, description, address, mapLink))
                }

                favoriteManager.checkFavoriteFoods(foodList) { updatedList ->
                    updateFoodList(updatedList)
                    val list = foodAdapter.getFoodList()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Lỗi tải danh sách popular food", error.toException())
            }
        })
    }

    private fun loadFestivalData() {
        val database = FirebaseDatabase.getInstance()
        val festivalRef = database.getReference("6/data")

        festivalRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                festivalList.clear()
                for (child in snapshot.children) {
                    val imageUrl = child.child("image_url").getValue(String::class.java) ?: ""
                    val name = child.child("name").getValue(String::class.java) ?: "Không rõ"
                    val description =
                        child.child("description").getValue(String::class.java) ?: "Không có mô tả"

                    festivalList.add(Festival(imageUrl, name, description))
                }
                festivalAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Lỗi tải danh sách lễ hội", error.toException())
            }
        })
    }

    private fun updateFoodList(list: List<Food> = foodList) {
        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, list.size)
        val currentPageItems = list.subList(startIndex, endIndex)
        foodAdapter.submitList(currentPageItems)
    }

    class FavoriteManager(private val userId: String) {
        private val db = FirebaseFirestore.getInstance()

        fun addFavoriteLocation(location: PopularStay, callback: (Boolean) -> Unit) {
            val favoriteData = hashMapOf(
                "id" to location.id,
                "name" to location.name,
                "imageUrl" to location.imageUrl,
                "description" to location.description,
                "type" to "location",
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("users").document(userId)
                .collection("favorites").document(location.id)
                .set(favoriteData)
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        }

        fun removeFavoriteLocation(locationId: String, callback: (Boolean) -> Unit) {
            db.collection("users").document(userId)
                .collection("favorites").document(locationId)
                .delete()
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        }

        fun addFavoriteFood(food: Food, callback: (Boolean) -> Unit) {
            val favoriteData = hashMapOf(
                "name" to food.name,
                "imageUrl" to food.imageUrl,
                "description" to food.description,
                "address" to food.address,
                "map" to food.map,
                "type" to "food",
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("users").document(userId)
                .collection("favorites").document(food.name)
                .set(favoriteData)
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        }

        fun removeFavoriteFood(foodName: String, callback: (Boolean) -> Unit) {
            db.collection("users").document(userId)
                .collection("favorites").document(foodName)
                .delete()
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        }

        fun checkFavoriteLocations(
            locations: List<PopularStay>,
            callback: (List<PopularStay>) -> Unit
        ) {
            if (userId.isEmpty()) {
                callback(locations)
                return
            }

            db.collection("users").document(userId)
                .collection("favorites")
                .whereEqualTo("type", "location")
                .get()
                .addOnSuccessListener { result ->
                    val favoriteIds = result.documents.map { it.id }
                    val updatedList = locations.map { location ->
                        location.copy(isFavorite = favoriteIds.contains(location.id))
                    }
                    callback(updatedList)
                }
                .addOnFailureListener { callback(locations) }
        }

        fun checkFavoriteFoods(foods: List<Food>, callback: (List<Food>) -> Unit) {
            if (userId.isEmpty()) {
                callback(foods)
                return
            }

            db.collection("users").document(userId)
                .collection("favorites")
                .whereEqualTo("type", "food")
                .get()
                .addOnSuccessListener { result ->
                    val favoriteNames = result.documents.map { it.id }
                    val updatedList = foods.map { food ->
                        food.copy(isFavorite = favoriteNames.contains(food.name))
                    }
                    callback(updatedList)
                }
                .addOnFailureListener { callback(foods) }
        }
    }




    private lateinit var tourPlanRecyclerView: RecyclerView
    private lateinit var tourPlanAdapter: TourPlanAdapter

    private fun loadTourPlansData() {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("18/data")  // ID bảng tour plans

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tourPlansList = mutableListOf<TourPlan>()
                for (child in snapshot.children) {
                    val planId = child.child("plan_id").getValue(String::class.java) ?: ""
                    val name = child.child("name").getValue(String::class.java) ?: "Không rõ"
                    val locationId = child.child("location_id").getValue(String::class.java) ?: ""
                    val imageUrl = child.child("image_url").getValue(String::class.java) ?: ""
                    val description = child.child("description").getValue(String::class.java) ?: "Không rõ"
                    val days = child.child("days").children.map { day ->
                        val dayNumber = day.child("day").getValue(Int::class.java) ?: 0
                        val dayTitle = day.child("title").getValue(String::class.java) ?: ""
                        val activities = day.child("activities").children.map { it.getValue(String::class.java) ?: "" }
                        TourDay(dayNumber, dayTitle, activities)
                    }

                    val tourPlan = TourPlan(planId, name, locationId, imageUrl, description , days)
                    tourPlansList.add(tourPlan)
                }
                tourPlanAdapter = TourPlanAdapter(tourPlansList)
                tourPlanRecyclerView.adapter = tourPlanAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Lỗi tải dữ liệu tour plans", error.toException())
            }
        })
    }

}