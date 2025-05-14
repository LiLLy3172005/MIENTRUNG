package com.example.travel_mientrung.layoutdetail

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_mientrung.R
import com.example.travel_mientrung.dataclass.TourDay
import com.example.travel_mientrung.Adapter.TourDayAdapter
class TourPlanDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_plan_detail)

        val title = intent.getStringExtra("title")
        val imageUrl = intent.getStringExtra("image_url")
        val description =intent.getStringExtra("description")
        val days = intent.getParcelableArrayListExtra<TourDay>("days") ?: arrayListOf()

        val imageView = findViewById<ImageView>(R.id.tourImageView)
        val titleText = findViewById<TextView>(R.id.tourTitleTextView)
        val descriptiontext = findViewById<TextView>(R.id.tourDescriptionTextView)
        val recyclerView = findViewById<RecyclerView>(R.id.daysRecyclerView)

        titleText.text = title
        descriptiontext.text=description
        Glide.with(this).load(imageUrl).into(imageView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TourDayAdapter(days)
    }
}
