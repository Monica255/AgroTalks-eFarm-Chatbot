package com.example.efarm.ui.forum

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import com.example.eFarm.databinding.ItemForumPostBinding
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.util.TextFormater
import com.google.firebase.auth.FirebaseAuth


class PagingForumAdapter(
    private val onClick: ((ForumPost) -> Unit),
    private val onCheckChanged: ((ForumPost) -> Unit),
    private val viewModel: ForumViewModel,
    private val activity: HomeForumActivity
) : PagingDataAdapter<ForumPost, PagingForumAdapter.ForumVH>(Companion) {
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    inner class ForumVH(private val binding: ItemForumPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: ForumPost) {
            Log.d("like", "adp "+post.likes.toString())
            binding.tvPostTitle.text = post.title
            binding.tvPostContent.text = post.content
            binding.tvLikeCount.text = TextFormater.formatLikeCounts(post.likes?.size?:0)
            binding.tvTimestamp.text = TextFormater.toPostTime(post.timestamp, binding.root.context)
            binding.tvKomentar.text =
                binding.root.context.getString(R.string.komentar, post.comments?.size ?: 0)

            viewModel.getUserdata(post.user_id).observe(activity) {it ->
                it?.let {
                    binding.tvUserName.text = it.name
                    Glide.with(itemView)
                        .load(it.img_profile)
                        .placeholder(R.drawable.placeholder)
                        .into(binding.imgProfilePicture)
                }
            }


            binding.iconVerified.visibility= if (post.verified!=null) View.VISIBLE else View.GONE


            val isLiked=post.likes?.let { it.contains(uid) }?:false
            if (post.likes != null && uid != null) {
                binding.cbLike.isChecked = isLiked
            } else {
                binding.cbLike.isChecked = false
            }

            val doLike: ((Unit) -> Unit) = {
                onCheckChanged.invoke(post)
                if (uid != null) {
                    binding.cbLike.isChecked = !isLiked
                    if (!isLiked) {
                        val show = ObjectAnimator.ofFloat(binding.imgLike, View.ALPHA, 0.8f).setDuration(600)
                        val disappear = ObjectAnimator.ofFloat(binding.imgLike, View.ALPHA, 0f).setDuration(800)
                        AnimatorSet().apply {
                            playSequentially(show, disappear)
                            start()
                        }
                    } else {
                        //post.likes?.remove(uid)
                    }
                } else {
                    binding.cbLike.isChecked = false
                }
            }

            var doubleClick = 0
            binding.root.setOnClickListener {
                doubleClick +=1
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        Log.d("TAG",doubleClick.toString())
                        if(doubleClick==2){
                            if (!isLiked) doLike(Unit)
                            doubleClick =0
                            return@postDelayed
                        }else if(doubleClick==1){
                            onClick.invoke(post)
                            doubleClick =0
                            return@postDelayed
                        }
                    }, 250)
            }

            if (post.img_header != null&&post.img_header?.trim()!="") {
                binding.imgHeaderPost.visibility = View.VISIBLE
                Glide.with(itemView)
                    .load(post.img_header)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.imgHeaderPost)
            } else {
                binding.imgHeaderPost.visibility = View.GONE
            }


            binding.cbLike.setOnClickListener {
                doLike(Unit)
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
            ItemForumPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForumVH(binding)
    }

    companion object : DiffUtil.ItemCallback<ForumPost>() {
        override fun areItemsTheSame(oldItem: ForumPost, newItem: ForumPost): Boolean {
            return oldItem.id_forum_post == newItem.id_forum_post
        }

        override fun areContentsTheSame(oldItem: ForumPost, newItem: ForumPost): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: ForumPost, newItem: ForumPost): Any? = Any()
    }

}