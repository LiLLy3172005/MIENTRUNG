package com.example.travel_mientrung.authen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travel_mientrung.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Random

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    // Gửi email xác minh
                    authResult.user?.sendEmailVerification()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Email xác minh đã được gửi", Toast.LENGTH_SHORT).show()
                            }
                        }

                    // Generate unique user_id
                    generateUniqueUserId { userId ->
                        // Lưu thông tin user vào Firestore
                        val user = hashMapOf(
                            "email" to email,
                            "role" to "user",
                            "user_id" to userId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        db.collection("users").document(authResult.user!!.uid)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Lỗi lưu thông tin người dùng: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnForgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun generateUniqueUserId(callback: (Long) -> Unit) {
        val userId = generateRandomUserId()

        // Check if user_id already exists
        db.collection("users")
            .whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // User ID is unique
                    callback(userId)
                } else {
                    // If exists, generate a new one recursively
                    generateUniqueUserId(callback)
                }
            }
            .addOnFailureListener {
                // In case of error, we'll still use the generated ID
                // (small chance of collision but better than failing registration)
                callback(userId)
            }
    }

    private fun generateRandomUserId(): Long {
        // Generate a random 10-digit number (between 1000000000 and 9999999999)
        return (1_000_000_000L + random.nextInt(5_000_000_000.toInt())).toLong()
    }
}