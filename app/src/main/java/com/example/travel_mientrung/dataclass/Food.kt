package com.example.travel_mientrung

import com.google.firebase.database.PropertyName
import java.io.Serializable

data class Food(
    @get:PropertyName("image_url")
    @set:PropertyName("image_url")
    var imageUrl: String = "",

    val name: String = "",
    val description: String = "",
    val address: String = "",
    val map: String = "",
    var isFavorite: Boolean = false,

    @get:PropertyName("cuisine_id")
    @set:PropertyName("cuisine_id")
    var cuisine_id: String? = null,

    @get:PropertyName("location_id")
    @set:PropertyName("location_id")
    var location_id: String = ""
) : Serializable
