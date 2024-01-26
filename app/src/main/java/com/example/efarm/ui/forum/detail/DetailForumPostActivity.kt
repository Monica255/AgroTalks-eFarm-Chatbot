package com.example.efarm.ui.forum.detail

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityDetailForumPostBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.util.DateConverter
import com.example.efarm.core.util.FORUM_POST_ID
import com.example.efarm.core.util.TextFormater
import com.example.efarm.ui.forum.FilterTopicAdapter
import com.example.efarm.ui.forum.ForumViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailForumPostActivity : AppCompatActivity() {
    lateinit var binding: ActivityDetailForumPostBinding
    private lateinit var adapterTopic: FilterTopicAdapter
    private lateinit var adapterComment: PagingCommentAdapter
    private val viewModel: ForumViewModel by viewModels()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var bestComment:CommentForumPost?=null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailForumPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapterTopic = FilterTopicAdapter (false){ }

        val layoutManagerCommonTopic = FlexboxLayoutManager(this)
        layoutManagerCommonTopic.flexDirection = FlexDirection.ROW
        binding.rvTopic.layoutManager = layoutManagerCommonTopic
        binding.rvTopic.adapter = adapterTopic

        val layoutManager = LinearLayoutManager(this)
        binding.rvKomentar.layoutManager = layoutManager

        binding.btnClose.setOnClickListener {
            finish()
        }

        val id = intent.getStringExtra(FORUM_POST_ID)
        id?.let {
            viewModel.getDetailForum(it).observe(this) {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Error -> {}
                    is Resource.Success -> {
                        it.data?.let {
                            setData(it)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setData(data: ForumPost) {
        viewModel.getUserdata(data.user_id).observe(this) { it ->
            it?.let {
                binding.tvUserName.text = it.name
                Glide.with(this)
                    .load(it.img_profile)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.imgProfilePicture)
            }
        }
        binding.tvPostTitle.text = data.title
        binding.tvContentPost.text = data.content
        binding.iconVerified.visibility = if (data.verified != null) View.VISIBLE else View.GONE


        val isLiked = data.likes?.let { it.contains(uid) } ?: false
        if (data.likes != null && uid != null) {
            binding.cbLike.isChecked = isLiked
        } else {
            binding.cbLike.isChecked = false
        }

//        binding.tvLikeCount.text = TextFormater.formatLikeCounts(data.likes?.size?:0)


        if(data.img_header==null||data.img_header==""){
            binding.imgHeaderPost.visibility=View.GONE
        }else{
            Glide.with(this)
                .load(data.img_header)
                .placeholder(R.drawable.placeholder)
                .into(binding.imgHeaderPost)
            binding.imgHeaderPost.visibility=View.VISIBLE
        }


        binding.tvTimastamp.text = TextFormater.toPostTime(data.timestamp, this)


        adapterComment = PagingCommentAdapter(data.verified, viewModel, this)
        binding.rvKomentar.adapter = adapterComment

        lifecycleScope.launch {
            adapterComment.loadStateFlow.collectLatest { loadStates ->
                showLoading(loadStates.refresh is LoadState.Loading)
            }
        }

        lifecycleScope.launch {
            data.topics?.let {
                viewModel.getTopics(it).observe(this@DetailForumPostActivity) {
                    when (it) {
                        is Resource.Loading -> {}
                        is Resource.Error -> {
                            it.message?.let {
                                binding.tvLabelTopic.visibility = View.GONE
                            }
                        }

                        is Resource.Success -> {
                            binding.tvLabelTopic.visibility = View.VISIBLE
                            it.data?.let {
                                adapterTopic.submitList(it.toMutableList())
                            }
                            getComments(data)
                        }
                    }

                }
            }

        }

        binding.btnSend.setOnClickListener {
            uid?.let { uid ->
                val content = binding.etKomentar.text.toString().trim()
                val comment = CommentForumPost(
                    "",
                    data.id_forum_post,
                    content,
                    uid,
                    DateConverter.getCurrentTimestamp()
                )
                if (content != "") {
                    lifecycleScope.launch {
                        viewModel.sendComment(comment).observe(this@DetailForumPostActivity) {
                            when (it) {
                                is Resource.Loading -> showLoading(true)
                                is Resource.Error -> {
                                    it.message?.let {
                                        Toast.makeText(
                                            this@DetailForumPostActivity,
                                            it,
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                }

                                is Resource.Success -> {
                                    showLoading(false)
                                    it.data?.let {
                                        Toast.makeText(
                                            this@DetailForumPostActivity,
                                            it,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        binding.etKomentar.setText("")
                                        getComments(data)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getComments(data: ForumPost) {
        lifecycleScope.launch {
            if(data.verified!=null && data.verified!="content") {
                viewModel.getBestComment(data.verified!!).observe(this@DetailForumPostActivity) {
                    when (it) {
                        is Resource.Loading -> {
                            showLoading(true)
                        }

                        is Resource.Error -> {
                            showLoading(false)
                            it.message?.let {
//                                Log.d("CMT", "error bc " + it)
                            }
                        }

                        is Resource.Success -> {
                            bestComment=it.data
                            showLoading(false)
                            getComments(data.id_forum_post, bestComment)
                        }
                    }
                }
            }else{
                getComments(data.id_forum_post, null)

            }
        }

    }

    private fun getComments(idForum:String, bestCommnet: CommentForumPost?) {
        viewModel.getComments(idForum, bestCommnet).observe(this) {
            adapterComment.submitData(lifecycle, it)
        }
    }

    private fun showLoading(isShowLoading: Boolean) {
        binding.pbLoading.visibility = if (isShowLoading) View.VISIBLE else View.GONE
    }
}