package com.example.travel_mientrung.Adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_mientrung.R
import com.example.travel_mientrung.dataclass.Comment
import com.example.travel_mientrung.dataclass.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private var posts: List<Post>,
    private val onItemClick: (Post) -> Unit ,
    private val isProfileView: Boolean // Thêm dòng này

) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val userNameCache = mutableMapOf<String, String>()
    private val userAvatarCache = mutableMapOf<String, String?>()
    private val commentsMap = mutableMapOf<String, MutableList<Comment>>()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val contentTextView: TextView = view.findViewById(R.id.contentTextView)
        val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        val commentsRecyclerView: RecyclerView = view.findViewById(R.id.commentsRecyclerView)
        val edtComment: EditText = view.findViewById(R.id.edtComment)
        val btnSend: View = view.findViewById(R.id.btnSendComment)
        val commentCountTextView: TextView = view.findViewById(R.id.commentCountTextView)
        val userAvatar: ImageView = view.findViewById(R.id.userAvatar)
        val btnDeletePost: ImageView = view.findViewById(R.id.btnDeletePost) // Thêm nút xóa

        init {
            commentsRecyclerView.layoutManager = LinearLayoutManager(view.context)
            view.setOnClickListener { onItemClick(posts[adapterPosition]) }



        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.titleTextView.text = post.title
        holder.contentTextView.text = post.content

        // Load post image
        Glide.with(holder.itemView.context)
            .load(post.image_url)
            .into(holder.imageView)

        // Ẩn/hiện thông tin user tùy thuộc vào isProfileView
        if (isProfileView) {
            holder.userAvatar.visibility = View.GONE
            holder.usernameTextView.visibility = View.GONE
            // Hiển thị nút xóa nếu là bài viết của người dùng hiện tại
        } else {
            holder.userAvatar.visibility = View.VISIBLE
            holder.usernameTextView.visibility = View.VISIBLE
            loadUserInfo(post.user_id, holder.usernameTextView, holder.userAvatar)
        }

        // Load comments
        loadComments(post.post_id, holder.commentsRecyclerView, holder.commentCountTextView)

        // Xử lý sự kiện gửi comment
        holder.btnSend.setOnClickListener {
            val text = holder.edtComment.text.toString().trim()
            if (text.isNotEmpty()) {
                sendComment(post.post_id, text, holder.itemView.context)
                holder.edtComment.text.clear()
            } else {
                Toast.makeText(holder.itemView.context, "Nhập bình luận trước đã nha cưng 🥺", Toast.LENGTH_SHORT).show()
            }
        }

        // Xử lý sự kiện xóa bài viết
        // Xử lý hiển thị nút xóa
        holder.btnDeletePost.visibility = if (isProfileView && post.user_id == currentUserId) {
            View.VISIBLE
        } else {
            View.GONE
        }

        holder.btnDeletePost.setOnClickListener {
            showDeletePostDialog(post, holder.itemView.context)
        }
    }

    private fun loadUserInfo(userId: String, textView: TextView, avatarView: ImageView) {
        textView.text = "Đang tải..."
        avatarView.setImageResource(R.drawable.ic_profile_placeholder)

        if (userNameCache.containsKey(userId)) {
            textView.text = userNameCache[userId]
            userAvatarCache[userId]?.let { url ->
                if (url.isNotEmpty()) {
                    loadAvatar(url, avatarView)
                }
            }
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    textView.text = "Ẩn danh"
                    return@addOnSuccessListener
                }

                val userDoc = documents.documents[0]
                val name = userDoc.getString("name") ?: "Không tên"
                val avatarUrl = userDoc.getString("avatar_url") ?: ""

                userNameCache[userId] = name
                userAvatarCache[userId] = avatarUrl
                textView.text = name

                if (avatarUrl.isNotEmpty()) {
                    loadAvatar(avatarUrl, avatarView)
                }
            }
            .addOnFailureListener {
                textView.text = "Lỗi tải thông tin"
            }
    }

    private fun loadAvatar(avatarUrl: String, imageView: ImageView) {
        Glide.with(imageView.context)
            .load(avatarUrl)
            .circleCrop()
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(imageView)
    }

    private fun sendComment(postId: String, text: String, context: Context) {
        val commentRef = FirebaseDatabase.getInstance().getReference("4/data")
        val commentId = commentRef.push().key ?: return

        // Lấy thông tin user hiện tại từ Firestore trước khi gửi comment
        FirebaseFirestore.getInstance().collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                // Tạo comment object với đầy đủ thông tin user
                val comment = Comment(
                    comment_id = commentId,
                    post_id = postId,
                    user_id = currentUserId,
                    user_name = document.getString("name") ?: "Ẩn danh",
                    avatar_Url = document.getString("avatar_url") ?: "",
                    comment = text,
                    created_at = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )

                // Optimistic UI update
                commentsMap[postId]?.add(comment) ?: run {
                    commentsMap[postId] = mutableListOf(comment)
                }
                notifyItemChanged(posts.indexOfFirst { it.post_id == postId })

                // Lưu comment vào Realtime Database
                commentRef.child(commentId).setValue(comment)
                    .addOnFailureListener { e ->
                        // Rollback nếu thất bại
                        commentsMap[postId]?.remove(comment)
                        notifyItemChanged(posts.indexOfFirst { it.post_id == postId })
                        Toast.makeText(context, "Lỗi gửi bình luận: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadComments(postId: String, recyclerView: RecyclerView, countTextView: TextView) {
        // Show cached comments immediately
        commentsMap[postId]?.let { comments ->
            setupCommentAdapter(recyclerView, comments, postId)
            updateCommentCount(countTextView, comments.size)
        }

        // Load from Firebase
        FirebaseDatabase.getInstance().getReference("4/data")
            .orderByChild("post_id").equalTo(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val comments = mutableListOf<Comment>()
                    for (child in snapshot.children) {
                        val comment = child.getValue(Comment::class.java)
                        comment?.let { comments.add(it) }
                    }

                    // Update cache
                    commentsMap[postId] = comments.toMutableList()
                    setupCommentAdapter(recyclerView, comments, postId)
                    updateCommentCount(countTextView, comments.size)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Keep cached comments on error
                }
            })
    }

    private fun setupCommentAdapter(recyclerView: RecyclerView, comments: List<Comment>, postId: String) {
        val sortedComments = comments.sortedByDescending {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it.created_at)
        }
        recyclerView.adapter = CommentAdapter(sortedComments, { comment ->
            showCommentOptions(comment, postId, recyclerView.context)
        }, currentUserId)

    }

    private fun updateCommentCount(textView: TextView, count: Int) {
        textView.text = if (count > 0) {
            "$count bình luận"
        } else {
            "0"
        }
    }

    private fun showCommentOptions(comment: Comment, postId: String, context: Context) {
        if (comment.user_id != currentUserId) return

        AlertDialog.Builder(context)
            .setTitle("Tùy chọn bình luận")
            .setItems(arrayOf("Sửa", "Xóa")) { _, which ->
                when (which) {
                    0 -> showEditCommentDialog(comment, postId, context)
                    1 -> showDeleteCommentDialog(comment, postId, context)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditCommentDialog(comment: Comment, postId: String, context: Context) {
        val editText = EditText(context).apply {
            setText(comment.comment)
        }

        AlertDialog.Builder(context)
            .setTitle("Sửa bình luận")
            .setView(editText)
            .setPositiveButton("Lưu") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty() && newText != comment.comment) {
                    updateComment(comment, newText, postId, context)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateComment(comment: Comment, newText: String, postId: String, context: Context) {
        val ref = FirebaseDatabase.getInstance().getReference("4/data/${comment.comment_id}")
        val updatedComment = comment.copy(comment = newText)

        // Optimistic update
        commentsMap[postId]?.firstOrNull { it.comment_id == comment.comment_id }?.comment = newText
        notifyItemChanged(posts.indexOfFirst { it.post_id == postId })

        ref.setValue(updatedComment)
            .addOnSuccessListener {
                Toast.makeText(context, "Đã cập nhật bình luận", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Rollback
                commentsMap[postId]?.firstOrNull { it.comment_id == comment.comment_id }?.comment = comment.comment
                notifyItemChanged(posts.indexOfFirst { it.post_id == postId })
                Toast.makeText(context, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteCommentDialog(comment: Comment, postId: String, context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Xóa bình luận")
            .setMessage("Bạn chắc chắn muốn xóa?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteComment(comment, postId, context)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteComment(comment: Comment, postId: String, context: Context) {
        val ref = FirebaseDatabase.getInstance().getReference("4/data/${comment.comment_id}")

        // Optimistic delete
        commentsMap[postId]?.removeIf { it.comment_id == comment.comment_id }
        notifyItemChanged(posts.indexOfFirst { it.post_id == postId })

        ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Đã xóa bình luận", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Rollback
                commentsMap[postId]?.add(comment)
                notifyItemChanged(posts.indexOfFirst { it.post_id == postId })
                Toast.makeText(context, "Lỗi khi xóa", Toast.LENGTH_SHORT).show()
            }
    }

    // Thêm hàm xử lý xóa bài viết
    private fun showDeletePostDialog(post: Post, context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Xóa bài viết")
            .setMessage("Bạn chắc chắn muốn xóa bài viết này?")
            .setPositiveButton("Xóa") { _, _ ->
                deletePost(post, context)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun deletePost(post: Post, context: Context) {
        val postRef = FirebaseDatabase.getInstance().getReference("posts/${post.post_id}")

        postRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Đã xóa bài viết", Toast.LENGTH_SHORT).show()
                // Cập nhật danh sách
                val newList = posts.toMutableList().apply { remove(post) }
                updatePosts(newList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi khi xóa bài viết", Toast.LENGTH_SHORT).show()
            }
    }

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}