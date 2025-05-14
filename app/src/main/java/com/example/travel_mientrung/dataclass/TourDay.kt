package com.example.travel_mientrung.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//@Parcelize
//data class TourDay(
//    val day: Int,
//    val title: String,
//    val activities: List<String>
//) : Parcelable
@Parcelize
data class TourDay(
    val dayNumber: Int = 0, // Thêm để giữ thứ tự
    val title: String = "",
    val activities: List<String> = listOf()
) : Parcelable

