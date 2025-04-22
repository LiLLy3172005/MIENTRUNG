package com.example.travel_mientrung.Adapter

import com.example.travel_mientrung.Food
import com.example.travel_mientrung.PopularStay
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FavoriteManager(private val userId: String) {
    private val db = FirebaseFirestore.getInstance()

    fun addFavoriteLocation(location: PopularStay, callback: (Boolean) -> Unit) {
        val favoriteData = hashMapOf(
            "id" to location.id,
            "name" to location.name,
            "imageUrl" to location.imageUrl,
            "description" to location.description,
            "type" to "location",
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(userId)
            .collection("favorites").document(location.id)
            .set(favoriteData)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun removeFavoriteLocation(locationId: String, callback: (Boolean) -> Unit) {
        db.collection("users").document(userId)
            .collection("favorites").document(locationId)
            .delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun addFavoriteFood(food: Food, callback: (Boolean) -> Unit) {
        val favoriteData = hashMapOf(
            "name" to food.name,
            "imageUrl" to food.imageUrl,
            "description" to food.description,
            "address" to food.address,
            "map" to food.map,
            "type" to "food",
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Using food name as ID (you might want to use a unique ID if available)
        db.collection("users").document(userId)
            .collection("favorites").document(food.name)
            .set(favoriteData)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun removeFavoriteFood(foodName: String, callback: (Boolean) -> Unit) {
        db.collection("users").document(userId)
            .collection("favorites").document(foodName)
            .delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getFavorites(callback: (List<Pair<String, Any>>) -> Unit) {
        db.collection("users").document(userId)
            .collection("favorites")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val favorites = mutableListOf<Pair<String, Any>>()
                for (document in result) {
                    val type = document.getString("type") ?: continue
                    if (type == "location") {
                        val stay = PopularStay(
                            id = document.getString("id") ?: "",
                            name = document.getString("name") ?: "",
                            imageUrl = document.getString("imageUrl") ?: "",
                            description = document.getString("description") ?: "",
                            isFavorite = true
                        )
                        favorites.add("location" to stay)
                    } else if (type == "food") {
                        val food = Food(
                            imageUrl = document.getString("imageUrl") ?: "",
                            name = document.getString("name") ?: "",
                            description = document.getString("description") ?: "",
                            address = document.getString("address") ?: "",
                            map = document.getString("map") ?: "",
                            isFavorite = true
                        )
                        favorites.add("food" to food)
                    }
                }
                callback(favorites)
            }
            .addOnFailureListener { callback(emptyList()) }
    }
}