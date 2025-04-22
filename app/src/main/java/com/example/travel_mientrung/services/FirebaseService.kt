package com.example.travel_mientrung.services

import android.net.Uri
import android.util.Log
import com.example.travel_mientrung.dataclass.Post
import com.google.firebase.database.*
import android.content.Context
import com.example.travel_mientrung.Interface.CloudinaryService
import com.example.travel_mientrung.dataclass.CloudinaryResponse
import com.example.travel_mientrung.dataclass.Comment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object FirebaseService {

    // Lấy danh sách bài viết
    fun getPosts(onResult: (List<Post>) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("14/data")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.mapNotNull { it.getValue(Post::class.java) }
                onResult(posts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseService", "loadPosts error: ${error.message}")
                onResult(emptyList())
            }
        })
    }

    // Thêm bài viết mới vào Firebase
    fun addPost(post: Post, onResult: (Boolean) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("14/data")

        // Sử dụng ID của bài viết làm khóa cho bài viết trong Firebase
        ref.child(post.post_id).setValue(post)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true)  // Thành công
                } else {
                    onResult(false)  // Thất bại
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error adding post: ${e.message}")
                onResult(false)  // Thất bại
            }
    }

    // Lấy file thực từ URI
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

    // Hàm tải ảnh lên Cloudinary
    fun uploadToCloudinary(context: Context, uri: Uri, callback: (String?) -> Unit) {
        val file = getFileFromUri(context, uri)
        if (file == null) {
            callback(null)
            return
        }


        fun addComment(comment: Comment, onResult: (Boolean) -> Unit) {
            val ref = FirebaseDatabase.getInstance().getReference("4/data")
            ref.child(comment.comment_id).setValue(comment)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { e ->
                    Log.e("FirebaseService", "Error saving comment: ${e.message}")
                    onResult(false)
                }
        }


        // Tạo request để upload
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val uploadPreset = "my_unsigned_preset".toRequestBody("text/plain".toMediaTypeOrNull())

        // Khởi tạo Retrofit và gọi API Cloudinary
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/v1_1/dyib68zv7/") // Cloud name của bạn
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(CloudinaryService::class.java)

        // Thực hiện request upload ảnh
        service.uploadImage(body, uploadPreset).enqueue(object : Callback<CloudinaryResponse> {
            override fun onResponse(call: Call<CloudinaryResponse>, response: Response<CloudinaryResponse>) {
                if (response.isSuccessful) {
                    callback(response.body()?.secure_url) // Trả về URL ảnh sau khi upload thành công
                } else {
                    callback(null) // Nếu có lỗi xảy ra
                }
            }

            override fun onFailure(call: Call<CloudinaryResponse>, t: Throwable) {
                callback(null) // Nếu có lỗi kết nối
            }
        })
    }
}
