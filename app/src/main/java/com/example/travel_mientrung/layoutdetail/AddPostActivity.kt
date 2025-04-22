package com.example.travel_mientrung.layoutdetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travel_mientrung.Interface.CloudinaryService
import com.example.travel_mientrung.R
import com.example.travel_mientrung.dataclass.CloudinaryResponse
import com.example.travel_mientrung.dataclass.Post
import com.example.travel_mientrung.services.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class AddPostActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var selectedImageView: ImageView
    private lateinit var postButton: Button
    private lateinit var selectImageButton: Button

    private val IMAGE_PICK_CODE = 1000
    private var imageUri: Uri? = null

    private var userId: String? = null  // ✅ Khai báo userId ở đây

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        selectedImageView = findViewById(R.id.selectedImageView)
        postButton = findViewById(R.id.postButton)
        selectImageButton = findViewById(R.id.selectImageButton)

        selectImageButton.setOnClickListener {
            openImagePicker()
        }

        postButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val email = currentUser?.email
            val db = FirebaseFirestore.getInstance()

            if (title.isEmpty() || content.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Vui lòng điền đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.first()
                        userId = document.getString("user_id")
                        Log.d("USER_ID", "User ID là: $userId")

                        // Sau khi có userId, tiếp tục upload ảnh và tạo bài viết
                        uploadToCloudinary(this, imageUri!!) { imageUrl ->
                            if (imageUrl != null && userId != null) {
                                val post = Post(
                                    title = title,
                                    content = content,
                                    status = "APPROVED",
                                    user_id = userId!!,
                                    image_url = imageUrl,
                                    post_id = System.currentTimeMillis().toString(),
                                    created_at = System.currentTimeMillis().toString(),
                                    updated_at = System.currentTimeMillis().toString()
                                )

                                FirebaseService.addPost(post) { success ->
                                    if (success) {
                                        Toast.makeText(this, "Bài viết đã được đăng!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Có lỗi xảy ra khi đăng bài", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            } else {
                                Toast.makeText(this, "Upload ảnh hoặc lấy user_id thất bại!", Toast.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        Log.w("USER_ID", "Không tìm thấy user trong Firestore")
                        Toast.makeText(this, "Không tìm thấy user!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("USER_ID", "Lỗi khi truy vấn user_id", exception)
                    Toast.makeText(this, "Lỗi truy vấn user!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data?.data
            selectedImageView.setImageURI(imageUri)
        }
    }

    private fun uploadToCloudinary(context: Context, uri: Uri, callback: (String?) -> Unit) {
        val file = getFileFromUri(context, uri)
        if (file == null) {
            callback(null)
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val uploadPreset = "my_unsigned_preset".toRequestBody("text/plain".toMediaTypeOrNull())

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/v1_1/dyib68zv7/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(CloudinaryService::class.java)

        service.uploadImage(body, uploadPreset).enqueue(object : Callback<CloudinaryResponse> {
            override fun onResponse(call: Call<CloudinaryResponse>, response: Response<CloudinaryResponse>) {
                if (response.isSuccessful) {
                    callback(response.body()?.secure_url)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<CloudinaryResponse>, t: Throwable) {
                callback(null)
            }
        })
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            tempFile.outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
