package com.example.travel_mientrung.dataclass
import java.io.Serializable

data class Post(
    val post_id: String = "",
    val user_id: String = "",
    val title: String = "",
    val content: String = "",
    val image_url: String = "",
    val status: String = "APPROVED",
    val created_at: String = "",
    val updated_at: String = ""
): Serializable
