package com.example.travel_mientrung.dataclass

data class Comment(
    val comment_id: String = "",
    val user_id: String = "",
    val post_id: String = "",
    var comment: String = "",
    val created_at: String = "",
    val updated_at: String = "",
    var user_name: String? = null,  // Thêm trường này để lưu tên
    val avatar_Url: String? = null, // Thêm dòng này

)