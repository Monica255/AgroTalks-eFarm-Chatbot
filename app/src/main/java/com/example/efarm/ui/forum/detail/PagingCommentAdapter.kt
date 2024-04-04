package com.example.efarm.ui.forum.detail

import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.example.eFarm.R.drawable
import com.example.eFarm.databinding.ItemCommentForumBinding
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.util.TextFormater
import com.example.efarm.core.util.VoteType
import com.example.efarm.ui.forum.ForumViewModel
import com.google.firebase.auth.FirebaseAuth


class PagingCommentAdapter(
    private val verifiedId: String?,
    private val viewModel: ForumViewModel,
    private val activity: DetailForumPostActivity,
    private val onCheckChanged: ((CommentForumPost,VoteType) -> Unit),
) : PagingDataAdapter<CommentForumPost, PagingCommentAdapter.ForumVH>(Companion) {
    private val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()


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

            Log.d("cv","uid"+uid.toString())
            if(comment.upvotes?.contains(uid) == true){
                Log.d("cv","a")
                binding.rbUp.isChecked=true
//                binding.rbUp.background = activity.getDrawable(R.drawable.icon_up_selected)
//                binding.rbDown.background = activity.getDrawable(R.drawable.icon_down)
                binding.rbDown.isChecked=false
            }else if (comment.downvotes?.contains(uid) == true){
                Log.d("cv","b")
                binding.rbUp.isChecked=false
//                binding.rbUp.background = activity.getDrawable(R.drawable.icon_up)
//                binding.rbDown.background = activity.getDrawable(R.drawable.icon_down_selected)
                binding.rbDown.isChecked=true
            }else{
                Log.d("cv","c")

                binding.rbUp.isChecked=false
                binding.rbDown.isChecked=false
//                binding.rbUp.background = activity.getDrawable(R.drawable.icon_up)
//                binding.rbDown.background = activity.getDrawable(R.drawable.icon_down)
            }
            val down =comment?.downvotes?.size?:0
            val up = comment?.upvotes?.size?:0
            val votes = up - down

            binding.tvVote.text=TextFormater.formatLikeCounts(votes)

            binding.rbUp.setOnClickListener {
                onCheckChanged.invoke(comment,VoteType.UP)
            }

            binding.rbDown.setOnClickListener {
                onCheckChanged.invoke(comment,VoteType.DOWN)
            }

            viewModel.getUserdata(comment.user_id).observe(activity) {it ->
                it?.let {
                    binding.tvUserName.text = it.name
                    Glide.with(itemView)
                        .load(it.img_profile)
                        .placeholder(drawable.placeholder)
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