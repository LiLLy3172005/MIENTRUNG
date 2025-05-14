package com.example.travel_mientrung.dataclass

data class TourPlan(
    val plan_id: String = "",
    val name: String = "",
    val location_id: String = "",
    val image_url: String = "",
    val description: String = "",
    val days: List<TourDay> = listOf()
)



