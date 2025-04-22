package com.example.travel_mientrung.authen

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.travel_mientrung.layoutmain.AdminActivity
import com.example.travel_mientrung.layoutmain.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object UserManager {
    private val db = FirebaseFirestore.getInstance()


    fun checkUserRoleAndRedirect(context: Context) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            if (user.isEmailVerified) {
                db.collection("users").document(user.uid).get()
                    .addOnSuccessListener { document ->
                        val role = document.getString("role")
                        when (role) {
                            "admin" -> {
                                context.startActivity(Intent(context, AdminActivity::class.java))
                            }
                            "user" -> {
                                context.startActivity(Intent(context, MainActivity::class.java))
                            }
                            else -> {
                                Toast.makeText(context, "Vai trò không xác định!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Không lấy được vai trò người dùng", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Vui lòng xác minh email trước", Toast.LENGTH_SHORT).show()
            }
        }
    }

}