package com.example.travel_mientrung

data class PopularStay(
    val id: String,         // location_id
    val name: String,
    val imageUrl: String,
    val description: String,
    var isFavorite: Boolean = false

)

