package com.example.travel_mientrung.layoutdetail

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_mientrung.Adapter.CommentAdapter
import com.example.travel_mientrung.R
import com.example.travel_mientrung.dataclass.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentActivity : AppCompatActivity() {
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var recyclerView: RecyclerView // Sửa tên biến cho phù hợp
    private lateinit var edtComment: EditText
    private lateinit var btnSend: Button
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        // Ánh xạ view từ layout
        recyclerView = findViewById(R.id.commentRecyclerView) // Sửa ID cho đúng
        edtComment = findViewById(R.id.edtComment)
        btnSend = findViewById(R.id.btnSend)

        // Khởi tạo adapter
        commentAdapter = CommentAdapter(
            comments = emptyList(),
            onCommentClick = { comment ->
                if (comment.user_id == currentUserId) {
                    showCommentOptions(comment)
                }
            },
            currentUserId = currentUserId
        )

        // Thiết lập RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = commentAdapter

        // Xử lý sự kiện gửi bình luận
        btnSend.setOnClickListener {
            val commentText = edtComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                sendComment(commentText)
                edtComment.text.clear()
            } else {
                Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show()
            }
        }

        loadComments()
    }

    private fun sendComment(text: String) {
        val commentId = FirebaseDatabase.getInstance().reference.child("comments").push().key
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Lấy thông tin user trước khi tạo comment
        FirebaseFirestore.getInstance().collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: "Ẩn danh"
                val avatarUrl = document.getString("avatar_url") ?: ""

                val comment = Comment(
                    comment_id = commentId!!,
                    post_id = "post_id_here", // Thay bằng postId thực tế
                    user_id = currentUserId,
                    user_name = userName,
                    avatar_Url = avatarUrl,
                    comment = text,
                    created_at = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )

                FirebaseDatabase.getInstance().reference.child("comments").child(commentId)
                    .setValue(comment)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Bình luận đã được gửi", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gửi bình luận thất bại", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                // Nếu không lấy được user info, vẫn tạo comment với thông tin tối thiểu
                val comment = Comment(
                    comment_id = commentId!!,
                    post_id = "post_id_here",
                    user_id = currentUserId,
                    comment = text,
                    created_at = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )

                FirebaseDatabase.getInstance().reference.child("comments").child(commentId)
                    .setValue(comment)
            }
    }

    private fun showCommentOptions(comment: Comment) {
        // ... giữ nguyên phần hiển thị dialog xóa/sửa ...
    }

    private fun loadComments() {
        FirebaseDatabase.getInstance().reference.child("comments")
            .orderByChild("post_id").equalTo("post_id_here")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val comments = mutableListOf<Comment>()
                    for (data in snapshot.children) {
                        val comment = data.getValue(Comment::class.java)
                        comment?.let {
                            // Nếu comment thiếu thông tin user, thử load bổ sung
                            if (it.user_name.isNullOrEmpty() || it.avatar_Url.isNullOrEmpty()) {
                                loadUserInfoForComment(it)
                            }
                            comments.add(it)
                        }
                    }
                    commentAdapter.updateComments(comments)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CommentActivity, "Lỗi tải bình luận", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadUserInfoForComment(comment: Comment) {
        FirebaseFirestore.getInstance().collection("users")
            .document(comment.user_id)
            .get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: "Ẩn danh"
                val avatarUrl = document.getString("avatar_url") ?: ""

                // Cập nhật comment trong database
                FirebaseDatabase.getInstance().reference.child("comments").child(comment.comment_id)
                    .child("user_name").setValue(userName)
                FirebaseDatabase.getInstance().reference.child("comments").child(comment.comment_id)
                    .child("avatar_Url").setValue(avatarUrl)
            }
    }
}