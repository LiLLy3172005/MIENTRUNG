package com.example.travel_mientrung.layoutdetail

import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_mientrung.Adapter.PostAdapter
import com.example.travel_mientrung.R
import com.example.travel_mientrung.layoutmain.HomeActivity
import com.example.travel_mientrung.services.FirebaseService
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BlogActivity : AppCompatActivity() {

    private lateinit var postRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var toolbar: MaterialToolbar
    private val handler = Handler()
    private var currentBackgroundIndex = 0

    // Danh sách các background bạn muốn hiển thị
    private val backgroundDrawables = intArrayOf(
        R.drawable.cocowave,
        R.drawable.goicuon,  // Thêm các drawable của bạn ở đây
        R.drawable.banhmi,
        R.drawable.banhloc,
        R.drawable.ocxao


    )

    private val backgroundChanger = object : Runnable {
        override fun run() {
            currentBackgroundIndex = (currentBackgroundIndex + 1) % backgroundDrawables.size
            changeBackgroundWithTransition(backgroundDrawables[currentBackgroundIndex])
            handler.postDelayed(this, 3000) // Lặp lại sau 3 giây
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog)

        // Khởi tạo toolbar
        toolbar = findViewById(R.id.toolbar)
        startBackgroundRotation()

        postRecyclerView = findViewById(R.id.postRecyclerView)
        postRecyclerView.layoutManager = LinearLayoutManager(this)

        // Khởi tạo adapter
        postAdapter = PostAdapter(emptyList(), onItemClick = { post ->
            // Xử lý sự kiện khi click vào bài viết
        }, isProfileView = false)
        postRecyclerView.adapter = postAdapter

        loadPosts()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_search // Làm icon Search tím lên

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_favorites -> {
                    Toast.makeText(this, "Favorites sắp có nha 😚", Toast.LENGTH_SHORT).show()
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

        bottomNav.selectedItemId = R.id.navigation_search

        val fabAddPost = findViewById<FloatingActionButton>(R.id.fabAddPost)
        fabAddPost.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startBackgroundRotation() {
        handler.postDelayed(backgroundChanger, 3000)
    }

    private fun changeBackgroundWithTransition(newBackgroundRes: Int) {
        val transition = TransitionDrawable(
            arrayOf(
                toolbar.background,
                ContextCompat.getDrawable(this, newBackgroundRes)!!
            )
        )
        toolbar.background = transition
        transition.startTransition(500) // Hiệu ứng chuyển đổi trong 0.5 giây
    }

    override fun onDestroy() {
        super.onDestroy()
        // Dừng việc thay đổi background khi Activity bị hủy
        handler.removeCallbacks(backgroundChanger)
    }

    private fun loadPosts() {
        FirebaseService.getPosts { postList ->
            val pendingPosts = postList.filter {
                it.status.equals("PENDING", ignoreCase = true)
            }

            Log.d("BLOG_DEBUG", "Đã lấy được ${pendingPosts.size} bài viết PENDING")

            runOnUiThread {
                if (pendingPosts.isEmpty()) {
                    Toast.makeText(this, "Không có bài viết nào đang chờ duyệt", Toast.LENGTH_SHORT).show()
                }
                postAdapter.updatePosts(pendingPosts)
            }
        }
    }
}