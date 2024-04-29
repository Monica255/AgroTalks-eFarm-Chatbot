package com.example.efarm.ui.forum

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.example.eFarm.databinding.ItemForumPostBinding
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.util.ADMIN_ID
import com.example.efarm.core.util.MIN_VERIFIED_POST
import com.example.efarm.core.util.TextFormater
import com.example.efarm.ui.loginsignup.LoginSignupActivity
import com.google.firebase.auth.FirebaseAuth
import org.apache.poi.sl.draw.geom.Context


class PagingForumAdapter(
    private val onClick: ((ForumPost) -> Unit),
    private val onCheckChanged: ((ForumPost) -> Unit),
    private val onDelete: ((ForumPost)->Unit),
    private val viewModel: ForumViewModel,
    private val context: LifecycleOwner
) : PagingDataAdapter<ForumPost, PagingForumAdapter.ForumVH>(Companion) {
    private val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private var likes = 0
    inner class ForumVH(private val binding: ItemForumPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: ForumPost) {
            Log.d("TAG",post.toString())
            binding.tvPostTitle.text = post.title
            binding.tvPostContent.text = post.thread?.thread?.trim()
            binding.tvLikeCount.text = TextFormater.formatLikeCounts(post.likes?.size?:0)
            binding.tvTimestamp.text = TextFormater.toPostTime(post.timestamp, binding.root.context)
            binding.tvKomentar.text =
                binding.root.context.getString(R.string.komentar, post.comments?.size ?: 0)
            viewModel.getUserdata(post.user_id).observe(context) {it ->
                it?.let {
                    binding.tvUserName.text = it.name
                    Glide.with(itemView)
                        .load(it.img_profile)
                        .placeholder(R.drawable.placeholder)
                        .into(binding.imgProfilePicture)
                }
            }
            binding.tvDelete.visibility = if(post.user_id==uid||uid== ADMIN_ID)View.VISIBLE else View.GONE
            binding.iconVerified.visibility= if (post.verified!=null) View.VISIBLE else View.GONE
            post.likes?.size?.let {
                likes=it
            }

            val isLiked=post.likes?.let { it.contains(uid) }?:false
            if (post.likes != null && uid != null) {
                binding.cbLike.isChecked = isLiked
            } else {
                binding.cbLike.isChecked = false
            }

            val doLike: ((Unit) -> Unit) = {
                onCheckChanged.invoke(post)
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
            }

            var doubleClick = 0
            binding.llItemPost.setOnClickListener {
                doubleClick +=1
                Handler(Looper.getMainLooper()).postDelayed(
                    {
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

            binding.tvDelete.setOnClickListener {
                onDelete.invoke(post)
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
