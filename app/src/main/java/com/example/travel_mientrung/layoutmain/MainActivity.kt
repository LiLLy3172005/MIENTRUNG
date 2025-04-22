package com.example.travel_mientrung.layoutmain

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.travel_mientrung.R

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