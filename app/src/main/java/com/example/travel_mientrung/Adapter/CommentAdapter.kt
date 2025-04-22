package com.example.travel_mientrung.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travel_mientrung.R
import com.example.travel_mientrung.dataclass.Comment
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(
    private var comments: List<Comment>,
    private val onCommentClick: (Comment) -> Unit,
    private val currentUserId: String
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val userNameCache = mutableMapOf<String, String>()
    private val userAvatarCache = mutableMapOf<String, String?>()

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUser: TextView = itemView.findViewById(R.id.tvUser)
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val btnOptions: View = itemView.findViewById(R.id.btnCommentOptions)
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)

        init {
            itemView.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION &&
                    comments[adapterPosition].user_id == currentUserId) {
                    onCommentClick(comments[adapterPosition])
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments.getOrNull(position) ?: return

        // Hiển thị nội dung comment
        holder.tvComment.text = comment.comment
        holder.tvTime.text = comment.created_at

        // Xử lý avatar
        val avatarUrl = comment.avatar_Url ?: userAvatarCache[comment.user_id]
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(holder.imgAvatar)
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_profile_placeholder)
            loadUserAvatar(comment.user_id, holder.imgAvatar)
        }

        // Hiển thị nút options
        holder.btnOptions.visibility = if (comment.user_id == currentUserId) View.VISIBLE else View.INVISIBLE
        holder.btnOptions.setOnClickListener { onCommentClick(comment) }

        // Xử lý tên người dùng
        val cachedName = userNameCache[comment.user_id]
        val userName = comment.user_name

        when {
            cachedName != null -> holder.tvUser.text = cachedName
            !userName.isNullOrEmpty() -> {
                holder.tvUser.text = userName
                userNameCache[comment.user_id] = userName
            }
            else -> {
                holder.tvUser.text = "Đang tải..."
                loadUserName(comment.user_id, holder.tvUser)
            }
        }
    }

    override fun getItemCount() = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }

    private fun loadUserName(userId: String, textView: TextView) {
        if (userNameCache.containsKey(userId)) {
            textView.text = userNameCache[userId]
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { documents ->
                val name = if (documents.isEmpty) {
                    "Ẩn danh"
                } else {
                    documents.documents[0].getString("name") ?: "Không tên"
                }
                userNameCache[userId] = name
                textView.text = name
            }
            .addOnFailureListener {
                textView.text = "Ẩn danh"
            }
    }

    private fun loadUserAvatar(userId: String, imageView: ImageView) {
        if (userAvatarCache.containsKey(userId)) {
            userAvatarCache[userId]?.let { url ->
                if (url.isNotEmpty()) {
                    Glide.with(imageView.context)
                        .load(url)
                        .into(imageView)
                }
            }
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener

                val avatarUrl = documents.documents[0].getString("avatar_url") ?: ""
                userAvatarCache[userId] = avatarUrl

                if (avatarUrl.isNotEmpty()) {
                    Glide.with(imageView.context)
                        .load(avatarUrl)
                        .circleCrop()
                        .into(imageView)
                }
            }
    }
}