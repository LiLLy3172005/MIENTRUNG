package com.example.travel_mientrung.layoutdetail

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_mientrung.Adapter.PostAdapter
import com.example.travel_mientrung.Interface.CloudinaryService
import com.example.travel_mientrung.R
import com.example.travel_mientrung.dataclass.CloudinaryResponse
import com.example.travel_mientrung.dataclass.Post
import com.example.travel_mientrung.layoutmain.HomeActivity
import com.example.travel_mientrung.services.FirebaseService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat

class ProfileActivity : AppCompatActivity() {
    private lateinit var avatarImageView: CircleImageView
    private lateinit var emailTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var bioTextView: TextView
    private var selectedAvatarUri: Uri? = null
    private val PICK_IMAGE_CODE = 101
    private lateinit var postAdapter: PostAdapter
    private lateinit var postRecyclerView: RecyclerView
    private lateinit var postSection: LinearLayout
    private lateinit var detailsSection: View
    private lateinit var postTabTextView: TextView
    private lateinit var detailsTabTextView: TextView
    private lateinit var postProgressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        initViews()
        setupRecyclerView()
        setupBottomNavigation()
        setupTabSelection()
        loadUserData()

        loadMyPosts()
    }

    private fun initViews() {
        avatarImageView = findViewById(R.id.avatarImageView)
        emailTextView = findViewById(R.id.emailTextView)
        nameTextView = findViewById(R.id.nameTextView)
        bioTextView = findViewById(R.id.bioTextView)
        postRecyclerView = findViewById(R.id.postRecyclerView)
        postSection = findViewById(R.id.postSection)
        detailsSection = findViewById(R.id.nestedScrollView)
        postTabTextView = findViewById(R.id.postTabTextView)
        detailsTabTextView = findViewById(R.id.detailsTabTextView)
        postProgressBar = findViewById(R.id.postProgressBar)

        findViewById<Button>(R.id.editProfileButton).setOnClickListener {
            showEditProfileDialog()
        }

        avatarImageView.setOnClickListener {
            pickImageFromGallery()
        }
    }
    private fun loadMyPosts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy người dùng 🥲", Toast.LENGTH_SHORT).show()
            return
        }

        // Thực hiện lấy bài viết từ Firebase, thay vì dùng service
        val ref = FirebaseDatabase.getInstance().getReference("14/data")

        ref.orderByChild("user_id").equalTo(userId) // Lọc theo user_id
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val myPosts = snapshot.children.mapNotNull { it.getValue(Post::class.java) }

                    Log.d("PROFILE_DEBUG", "Bài viết của bé ($userId): ${myPosts.size}")

                    runOnUiThread {
                        if (myPosts.isEmpty()) {
                            Toast.makeText(this@ProfileActivity, "Bé chưa có bài đăng nào hết á 😥", Toast.LENGTH_SHORT).show()
                        }
                        postAdapter.updatePosts(myPosts) // Cập nhật lại adapter với danh sách bài viết
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseService", "loadPosts error: ${error.message}")
                    Toast.makeText(this@ProfileActivity, "Đã xảy ra lỗi khi tải bài viết.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(emptyList(), { post ->
            showPostDetail(post)
        }, isProfileView = true) // Thêm tham số true để chỉ định đang ở profile view

        postRecyclerView.layoutManager = LinearLayoutManager(this)
        postRecyclerView.adapter = postAdapter
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_search -> {
                    startActivity(Intent(this, BlogActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }

    private fun setupTabSelection() {
        postTabTextView.setOnClickListener {
            showPostTab()
            loadMyPosts()
        }

        detailsTabTextView.setOnClickListener {
            showDetailsTab()
        }

        // Mặc định hiển thị tab Details
        showDetailsTab()
    }

    private fun showPostTab() {
        postSection.visibility = View.VISIBLE
        detailsSection.visibility = View.GONE
        postTabTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
        postTabTextView.textSize = 18f
        detailsTabTextView.setTextColor(ContextCompat.getColor(this, R.color.gray))
        detailsTabTextView.textSize = 16f
    }

    private fun showDetailsTab() {
        postSection.visibility = View.GONE
        detailsSection.visibility = View.VISIBLE
        detailsTabTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
        detailsTabTextView.textSize = 18f
        postTabTextView.setTextColor(ContextCompat.getColor(this, R.color.gray))
        postTabTextView.textSize = 16f
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        val dialog = AlertDialog.Builder(this, R.style.MyDialogTheme)
            .setView(dialogView)
            .create()

        val dialogAvatarImageView = dialogView.findViewById<CircleImageView>(R.id.dialogAvatarImageView)
        val dialogNameEditText = dialogView.findViewById<TextInputEditText>(R.id.dialogNameEditText)
        val dialogBioEditText = dialogView.findViewById<TextInputEditText>(R.id.dialogBioEditText)
        val changePhotoText = dialogView.findViewById<TextView>(R.id.changePhotoText)
        val dialogCancelButton = dialogView.findViewById<Button>(R.id.dialogCancelButton)
        val dialogSaveButton = dialogView.findViewById<Button>(R.id.dialogSaveButton)

        dialogNameEditText.setText(nameTextView.text.toString())
        dialogBioEditText.setText(bioTextView.text.toString())

        Glide.with(this)
            .load(avatarImageView.drawable)
            .into(dialogAvatarImageView)

        changePhotoText.setOnClickListener {
            pickImageFromGalleryForDialog(dialogAvatarImageView)
        }

        dialogCancelButton.setOnClickListener { dialog.dismiss() }

        dialogSaveButton.setOnClickListener {
            val newName = dialogNameEditText.text.toString()
            val newBio = dialogBioEditText.text.toString()

            if (newName.isNotEmpty()) {
                saveProfileChanges(newName, newBio) {
                    loadUserData() // Refresh data after saving
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Vui lòng nhập tên của bạn", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showPostDetail(post: Post) {
        val intent = Intent(this, BlogActivity::class.java).apply {
            putExtra("post_data", post)
        }
        startActivity(intent)
    }

    private fun pickImageFromGallery() {
        Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            startActivityForResult(this, PICK_IMAGE_CODE)
        }
    }

    private fun pickImageFromGalleryForDialog(imageView: CircleImageView) {
        Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            startActivityForResult(this, PICK_IMAGE_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK && data != null) {
            selectedAvatarUri = data.data
            selectedAvatarUri?.let {
                avatarImageView.setImageURI(it)
            }
        }
    }

    private fun saveProfileChanges(newName: String, newBio: String, callback: () -> Unit = {}) {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            val userInfo = hashMapOf<String, Any>(
                "name" to newName,
                "bio" to newBio
            )

            selectedAvatarUri?.let { uri ->
                uploadToCloudinary(this, uri) { avatarUrl ->
                    avatarUrl?.let {
                        userInfo["avatar_url"] = it
                        saveToFirestore(db, uid, userInfo, callback)
                    } ?: run {
                        saveToFirestore(db, uid, userInfo, callback)
                    }
                }
            } ?: run {
                saveToFirestore(db, uid, userInfo, callback)
            }

            nameTextView.text = newName
            bioTextView.text = newBio
        }
    }

    private fun saveToFirestore(db: FirebaseFirestore, uid: String, userInfo: HashMap<String, Any>, callback: () -> Unit = {}) {
        db.collection("users").document(uid)
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show()
                callback()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi khi cập nhật hồ sơ", Toast.LENGTH_SHORT).show()
            }
    }



    private fun loadUserData() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            emailTextView.text = user.email ?: "Chưa có email"

            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    nameTextView.text = document.getString("name") ?: user.displayName ?: "Chưa có tên"
                    document.getString("bio")?.let { bioTextView.text = it }

                    document.getString("avatar_url")?.let { avatarUrl ->
                        Glide.with(this)
                            .load(avatarUrl)
                            .circleCrop()
                            .into(avatarImageView)
                    }
                }
                .addOnFailureListener {
                    nameTextView.text = user.displayName ?: "Chưa có tên"
                }
        }
    }

    private fun uploadToCloudinary(context: Context, uri: Uri, callback: (String?) -> Unit) {
        getFileFromUri(context, uri)?.let { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val uploadPreset = "my_unsigned_preset".toRequestBody("text/plain".toMediaTypeOrNull())

            Retrofit.Builder()
                .baseUrl("https://api.cloudinary.com/v1_1/dyib68zv7/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CloudinaryService::class.java)
                .uploadImage(body, uploadPreset)
                .enqueue(object : Callback<CloudinaryResponse> {
                    override fun onResponse(call: Call<CloudinaryResponse>, response: Response<CloudinaryResponse>) {
                        callback(response.body()?.secure_url)
                    }

                    override fun onFailure(call: Call<CloudinaryResponse>, t: Throwable) {
                        callback(null)
                    }
                })
        } ?: callback(null)
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                File.createTempFile("avatar", ".jpg", context.cacheDir).apply {
                    outputStream().use { inputStream.copyTo(it) }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}