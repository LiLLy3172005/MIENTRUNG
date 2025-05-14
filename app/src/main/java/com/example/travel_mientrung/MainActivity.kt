package com.example.travel_mientrung

import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ivNext = findViewById<ImageView>(R.id.ivNext)
        ivNext.setOnClickListener {
            // Mở WelcomeActivity khi nhấn vào mũi tên
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }
    }
}