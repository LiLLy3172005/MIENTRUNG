package com.example.travel_mientrung.layoutdetail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.example.travel_mientrung.R
import com.squareup.picasso.Picasso

class FoodDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        val name = intent.getStringExtra("food_name")
        val imageUrl = intent.getStringExtra("food_image")
        val description = intent.getStringExtra("food_description")
        val address = intent.getStringExtra("food_address")
        val mapLink = intent.getStringExtra("food_map_link")

        val nameTextView = findViewById<TextView>(R.id.foodNameDetailTextView)
        val imageView = findViewById<ImageView>(R.id.foodImageDetailView)
        val descriptionTextView = findViewById<TextView>(R.id.foodDescriptionDetailTextView)
        val addressTextView = findViewById<TextView>(R.id.foodAddressTextView)
        val mapLinkTextView = findViewById<TextView>(R.id.foodMapLinkTextView)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        nameTextView.text = name
        descriptionTextView.text = description
        addressTextView.text = address
        Picasso.get().load(imageUrl).into(imageView)

        // Thiết lập màu và hành vi khi bấm vào map link
        mapLinkTextView.text = "Xem bản đồ"
        mapLinkTextView.setTextColor(Color.parseColor("#1E88E5"))
        mapLinkTextView.paint.isUnderlineText = true

        mapLinkTextView.setOnClickListener {
            if (!mapLink.isNullOrEmpty() && mapLink.startsWith("http")) {
                try {
                    val uri = Uri.parse(mapLink)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps") // ưu tiên mở bằng Google Maps
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Nếu không có Google Maps, mở bằng trình duyệt
                    val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapLink))
                    startActivity(fallbackIntent)
                }
            } else {
                Toast.makeText(this, "Liên kết bản đồ không hợp lệ.", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}




