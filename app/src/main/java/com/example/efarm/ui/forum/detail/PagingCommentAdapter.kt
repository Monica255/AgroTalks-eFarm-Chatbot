package com.example.efarm.ui.forum.detail

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.example.eFarm.databinding.ItemCommentForumBinding
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.util.TextFormater
import com.example.efarm.ui.forum.ForumViewModel


class PagingCommentAdapter(
    private val verifiedId: String?,
    private val viewModel: ForumViewModel,
    private val activity: DetailForumPostActivity
) : PagingDataAdapter<CommentForumPost, PagingCommentAdapter.ForumVH>(Companion) {

    inner class ForumVH(private val binding: ItemCommentForumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(comment: CommentForumPost) {
            binding.tvTimestamp.text = TextFormater.toPostTime(comment.timestamp, binding.root.context)

            if (comment.content.length > 80) {
                val sub: String = comment.content.substring(0, 80)
                binding.tvComment.setText(Html.fromHtml("$sub<font color='#F39322'><b> ....</b></font>"))
                binding.tvComment.setOnClickListener { view ->
                    if (!binding.tvComment.text.equals(comment.content)) {
                        binding.tvComment.setText(comment.content)
                    } else {
                        binding.tvComment.setText(Html.fromHtml("$sub<font color='#F39322'><b> ....</b></font>"))
                    }
                }
            } else {
                binding.tvComment.text=comment.content
            }
            binding.tvBestAnswer.visibility=if(verifiedId==comment.id_comment)View.VISIBLE else View.GONE
            viewModel.getUserdata(comment.user_id).observe(activity) {it ->
                it?.let {
                    binding.tvUserName.text = it.name
                    Glide.with(itemView)
                        .load(it.img_profile)
                        .placeholder(R.drawable.placeholder)
                        .into(binding.imgProfilePicture)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ForumVH, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumVH {
        val binding =
            ItemCommentForumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForumVH(binding)
    }

    companion object : DiffUtil.ItemCallback<CommentForumPost>() {
        override fun areItemsTheSame(oldItem: CommentForumPost, newItem: CommentForumPost): Boolean {
            return oldItem.id_comment == newItem.id_comment
        }

        override fun areContentsTheSame(oldItem: CommentForumPost, newItem: CommentForumPost): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: CommentForumPost, newItem: CommentForumPost): Any? = Any()
    }

}