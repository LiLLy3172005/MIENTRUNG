// File: AdminCreator.kt
package com.example.travel_mientrung.authen

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue

class AdminCreator {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Hàm tạo admin bằng email/password
    fun createAdminAccount(
        email: String,
        password: String,
        adminName: String,
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    user?.let { firebaseUser ->
                        // Cập nhật display name (tuỳ chọn)
                        updateUserProfile(firebaseUser, adminName)

                        // Lưu thông tin admin vào Firestore
                        saveAdminToFirestore(firebaseUser, adminName, onComplete)
                    } ?: run {
                        onComplete(false, "Không lấy được thông tin user sau khi tạo")
                    }
                } else {
                    onComplete(false, "Lỗi tạo tài khoản: ${task.exception?.message}")
                }
            }
    }

    private fun updateUserProfile(user: FirebaseUser, name: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates)
            .addOnFailureListener { e ->
                Log.e("AdminCreator", "Lỗi cập nhật profile: ${e.message}")
            }
    }

    private fun saveAdminToFirestore(
        user: FirebaseUser,
        adminName: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val adminData = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "name" to adminName,
            "role" to "admin", // Đánh dấu vai trò
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("admins").document(user.uid)
            .set(adminData)
            .addOnSuccessListener {
                // Gửi email xác minh (tuỳ chọn)
                user.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        val verifyMsg = if (task.isSuccessful) " | Đã gửi email xác minh" else ""
                        onComplete(true, "Tạo admin thành công!$verifyMsg")
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, "Lỗi lưu thông tin admin: ${e.message}")
            }
    }
}