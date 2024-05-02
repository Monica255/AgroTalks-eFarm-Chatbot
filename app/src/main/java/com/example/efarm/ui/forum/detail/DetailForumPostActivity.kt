package com.example.efarm.ui.forum.detail

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
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
import com.example.efarm.core.util.ViewEventsVoteComment
import com.example.efarm.core.util.VoteType
import com.example.efarm.ui.forum.FilterTopicAdapter
import com.example.efarm.ui.forum.ForumViewModel
import com.example.efarm.ui.forum.upload.Helper
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
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
    var bestComment: CommentForumPost? = null
    private var tempPost: CommentForumPost? = null
    lateinit var image: Drawable
    val textSpan = SpannableStringBuilder()
    val downloadImageTask =
        Helper.Companion.DownloadImageTask(object :
            Helper.Companion.DownloadImageTask.OnImageDownloadedListener {
            override fun onImageDownloaded(drawable: List<Pair<Drawable?, Int>?>?) {
                val maxWidth =
                    binding.tvContentPost.width - binding.tvContentPost.paddingStart - binding.tvContentPost.paddingEnd
                drawable?.let { pair ->
                    pair.forEach {
                        it?.let {img->
                            img?.first?.let {
                                image = it
                                image?.let {
                                    val drawableWidth = image.intrinsicWidth
                                    val drawableHeight = image.intrinsicHeight
                                    if (drawableHeight <= drawableWidth) {
                                        val scaledHeight =
                                            (maxWidth.toFloat() / drawableWidth.toFloat() * drawableHeight.toFloat()).toInt()
                                        image?.setBounds(0, 0, maxWidth, scaledHeight)
                                    } else {
                                        val scaledWidth =
                                            (maxWidth.toFloat() / drawableHeight.toFloat() * drawableWidth.toFloat()).toInt()
                                        image?.setBounds(0, 0, scaledWidth, maxWidth)
                                    }
                                    val imageSpan =
                                        image?.let { it1 -> ImageSpan(it1, ImageSpan.ALIGN_BOTTOM) }
//                                    textSpan.insert(img.second, "\n \n")
                                    textSpan.setSpan(
                                        imageSpan, img.second + 1, img.second + 2,
                                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                                    )

                                }
                            }


                        }
                    }
                    binding.tvContentPost.text = textSpan
                    showLoading(false)
                }
            }
        },this)


    private val onCheckChanged: ((CommentForumPost, VoteType) -> Unit) = { post, votetype ->
        tempPost = post
        viewModel.voteComment(post, votetype).observe(this) {
            when (it) {
                is Resource.Success -> {
                    it.data?.let {
                        if (tempPost != null) viewModel.onViewEventComment(
                            ViewEventsVoteComment.Edit(
                                tempPost!!,
                                it.first,
                                votetype
                            )
                        );tempPost == null
                    }
                }

                is Resource.Error -> {
                    showError(it.message.toString())
                }

                is Resource.Loading -> {}
            }
        }
    }

    private fun showError(msg: String) {
        Toast.makeText(binding.root.context, msg, Toast.LENGTH_SHORT).show()
        tempPost?.let { it ->
            viewModel.onViewEventComment(ViewEventsVoteComment.Rebind(it))
            tempPost = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailForumPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapterTopic = FilterTopicAdapter(false) { }

        val layoutManagerCommonTopic = FlexboxLayoutManager(this)
        layoutManagerCommonTopic.flexDirection = FlexDirection.ROW
        binding.rvTopic.layoutManager = layoutManagerCommonTopic
        binding.rvTopic.adapter = adapterTopic

        val layoutManager = LinearLayoutManager(this)
        binding.rvKomentar.layoutManager = layoutManager

        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.customaView.setOnClickListener {
//            binding.customaView.onClick()
        }

        val id = intent.getStringExtra(FORUM_POST_ID)
        id?.let {
            viewModel.getDetailForum(it).observe(this) {
                when (it) {
                    is Resource.Loading -> {
                        showLoading(true)
                    }
                    is Resource.Error -> {
                        showLoading(false)
                    }
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
                    .error(R.drawable.cracked_img)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.imgProfilePicture)
            }
        }

        binding.iconVerified.visibility = if (data.verified != null) View.VISIBLE else View.GONE

        val isLiked = data.likes?.let { it.contains(uid) } ?: false
        if (data.likes != null && uid != null) {
            binding.cbLike.isChecked = isLiked
        } else {
            binding.cbLike.isChecked = false
        }

        if (data.img_header == null || data.img_header == "" ) {
            binding.customaView.visibility = View.GONE
        } else {
            binding.customaView.showMedia(data.img_header!!)
        }

//        binding.vvHeader.setOnClickListener {
//            binding.vvHeader.start()
//        }

        binding.tvTimastamp.text = TextFormater.toPostTime(data.timestamp, this)

        binding.tvPostTitle.text = data.title
        textSpan.append(data.thread?.thread)
        binding.tvContentPost.text = textSpan
        Log.d("detail", "title "+data.title)
        Log.d("detail", "thread "+data.thread?.thread?.length.toString())

        data.thread?.images?.let { imgs ->
            downloadImageTask.execute(imgs.toList())
        }

        adapterComment = PagingCommentAdapter(data.verified, viewModel, this, onCheckChanged)
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
                    DateConverter.getCurrentTimestamp(),
                    null,
                    null
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
//    private fun showMedia(filePath:String){
//        val type=filePath.split("/")
//        if (filePath != null) {
//            if(!displayMedia(type.last(),filePath)){
//                val storage = FirebaseStorage.getInstance()
//                val storageRef = storage.getReferenceFromUrl(filePath.toString())
//                storageRef.metadata.addOnSuccessListener { storageMetadata ->
//                    val contentType = storageMetadata.contentType
//                    displayMedia(contentType,filePath)
//                }.addOnFailureListener { e ->
//                    e.printStackTrace()
//                }
//            }
//        }
//    }
//
//    private fun displayMedia(contentType:String?,filePath: String):Boolean{
//        if (contentType != null && contentType.startsWith("image")) {
//            binding.vvHeader.visibility=View.GONE
//            binding.imgHeader.visibility=View.VISIBLE
//            Glide.with(this)
//                .load(filePath)
//                .error(R.drawable.cracked_img)
//                .placeholder(R.drawable.placeholder_img)
//                .into(binding.imgHeader)
//            return true
//        } else if (contentType != null && contentType.startsWith("video")) {
//            binding.vvHeader.visibility = View.VISIBLE
//            binding.imgHeader.visibility = View.GONE
//            binding.vvHeader.setVideoPath(filePath.toString())
//            binding.vvHeader.start()
//            return true
//        }else return false
//    }

    private fun getComments(data: ForumPost) {
        lifecycleScope.launch {
            if (data.verified != null && data.verified != "content") {
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
                            bestComment = it.data
                            showLoading(false)
                            getComments(data.id_forum_post, bestComment)
                        }
                    }
                }
            } else {
                getComments(data.id_forum_post, null)

            }
        }

    }

    private fun getComments(idForum: String, bestCommnet: CommentForumPost?) {
        viewModel.getComments(idForum, bestCommnet).observe(this) {
            adapterComment.submitData(lifecycle, it)
        }
    }

    private fun showLoading(isShowLoading: Boolean) {
        binding.pbLoading.visibility = if (isShowLoading) View.VISIBLE else View.GONE
    }
}