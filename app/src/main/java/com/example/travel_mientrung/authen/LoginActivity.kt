package com.example.travel_mientrung.authen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travel_mientrung.layoutmain.AdminActivity
import com.example.travel_mientrung.layoutmain.HomeActivity
import com.example.travel_mientrung.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    // Thông tin admin mặc định (nên thay bằng giá trị thực tế hoặc lưu trong Remote Config)
    private val defaultAdminEmail = "huynhluulily@gmail.com"
    private val defaultAdminPassword = "123456"
    private val defaultAdminName = "Quản Trị Viên"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Tự động tạo admin nếu chưa tồn tại khi mở LoginActivity
        checkAndCreateDefaultAdmin()

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            handleUserLogin()
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<Button>(R.id.btnForgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun checkAndCreateDefaultAdmin() {
        auth.signInWithEmailAndPassword(defaultAdminEmail, defaultAdminPassword)
            .addOnFailureListener {
                // Nếu đăng nhập thất bại -> tạo mới admin
                createDefaultAdminAccount()
            }
            .addOnSuccessListener {
                // Đã tồn tại admin -> kiểm tra trong Firestore
                verifyAdminInFirestore(it.user?.uid)
            }
    }

    private fun createDefaultAdminAccount() {
        auth.createUserWithEmailAndPassword(defaultAdminEmail, defaultAdminPassword)
            .addOnSuccessListener { authResult ->
                authResult.user?.let { user ->
                    // Lưu thông tin admin vào Firestore
                    val adminData = hashMapOf(
                        "email" to defaultAdminEmail,
                        "name" to defaultAdminName,
                        "role" to "admin",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "isDefault" to true // Đánh dấu là admin mặc định
                    )

                    db.collection("admins").document(user.uid)
                        .set(adminData)
                        .addOnSuccessListener {
                            Log.d("AdminSetup", "Tạo admin mặc định thành công")
                            user.sendEmailVerification() // Gửi email xác minh
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AdminSetup", "Lỗi tạo admin: ${e.message}")
            }
    }

    private fun verifyAdminInFirestore(uid: String?) {
        uid?.let {
            db.collection("admins").document(uid).get()
                .addOnFailureListener {
                    // Nếu không tìm thấy trong admins -> tự động tạo lại
                    createDefaultAdminAccount()
                }
        }
    }

    private fun handleUserLogin() {
        val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                authResult.user?.let { user ->
                    checkUserRole(user.uid)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Đăng nhập thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserRole(uid: String) {
        // Kiểm tra trong collection admins trước
        db.collection("admins").document(uid).get()
            .addOnSuccessListener { adminDoc ->
                if (adminDoc.exists()) {
                    // Đảm bảo document có trường role
                    if (adminDoc.getString("role") == "admin") {
                        startActivity(Intent(this, AdminActivity::class.java))
                    } else {
                        Toast.makeText(this, "Tài khoản không có quyền admin", Toast.LENGTH_SHORT).show()
                        auth.signOut()
                    }
                } else {
                    // Nếu không phải admin, kiểm tra collection users
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                startActivity(Intent(this, HomeActivity::class.java))
                            } else {
                                Toast.makeText(this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show()
                                auth.signOut()
                            }
                        }
                }
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi kiểm tra quyền: ${e.message}", Toast.LENGTH_SHORT).show()
                auth.signOut()
            }
    }

    // Hàm tạo admin bằng code (có thể gọi từ nơi khác)
    fun createAdminAccount(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                authResult.user?.let { user ->
                    val adminData = hashMapOf(
                        "email" to email,
                        "name" to name,
                        "role" to "admin",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "isCustom" to true // Đánh dấu admin tạo thủ công
                    )

                    db.collection("admins").document(user.uid)
                        .set(adminData)
                        .addOnSuccessListener {
                            user.sendEmailVerification()
                            Toast.makeText(this, "Tạo admin thành công", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi tạo admin: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}