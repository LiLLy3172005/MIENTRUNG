package com.example.travel_mientrung.layoutdetail

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.travel_mientrung.R
import com.squareup.picasso.Picasso

class ScenicSpotDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scenic_spot_detail)

        val imageUrl = intent.getStringExtra("image_url")
        val name = intent.getStringExtra("name")
        val description = intent.getStringExtra("description")

        val img = findViewById<ImageView>(R.id.detailImageView)
        val txtName = findViewById<TextView>(R.id.detailNameTextView)
        val txtDescription = findViewById<TextView>(R.id.detailDescriptionTextView)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        txtName.text = name
        txtDescription.text = description
        Picasso.get().load(imageUrl).into(img)
        backButton.setOnClickListener {
            finish()
        }
    }
}
