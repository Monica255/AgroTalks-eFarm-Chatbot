package com.example.efarm.ui.forum

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityHomeForumBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.util.ADMIN_ID
import com.example.efarm.core.util.FORUM_POST_ID
import com.example.efarm.core.util.KategoriTopik
import com.example.efarm.core.util.MIN_VERIFIED_POST
import com.example.efarm.core.util.ViewEventsForumPost
import com.example.efarm.ui.forum.chatbot.ChatActivity
import com.example.efarm.ui.forum.detail.DetailForumPostActivity
import com.example.efarm.ui.forum.profile.ProfileActivity
import com.example.efarm.ui.forum.upload.MakePostActivity
import com.example.efarm.ui.loginsignup.LoginSignupActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

interface OnGetDataTopic {
    fun handleDataTopic(data: Topic?)
}
@AndroidEntryPoint
class HomeForumActivity : AppCompatActivity(),OnGetDataTopic {
    private lateinit var binding: ActivityHomeForumBinding
    private lateinit var adapterForum: PagingForumAdapter
    private val viewModel: ForumViewModel by viewModels()
    private var tempPost:ForumPost?=null
    private var goToDetail=false
    private val onCLick: ((ForumPost) -> Unit) = { post ->
        val intent = Intent(this, DetailForumPostActivity::class.java)
        intent.putExtra(FORUM_POST_ID, post.id_forum_post)
        startActivity(intent)

    }

    private val onDelete: ((ForumPost)->Unit) ={post->
        showConfirmDialogDelete(post)
    }

    private val onCheckChanged: ((ForumPost) -> Unit) = { post ->
        tempPost=post
        viewModel.likeForumPost(post).observe(this){
            when(it){
                is Resource.Success->{
                    it.data?.let {
                        if(tempPost!=null)viewModel.onViewEvent(ViewEventsForumPost.Edit(tempPost!!,it.first))
                        var likes = post.likes?.size?:0
                        if (it.first) likes+=1 else likes-=1
                        if(post.user_id!= ADMIN_ID){
                            if(likes>= MIN_VERIFIED_POST&&post.verified==null){
                                verified(post,"content")
                            }else if (likes<= MIN_VERIFIED_POST&&post.verified!=null){
                                verified(post,null)
                            } else tempPost==null
                        }else tempPost==null

                    }
                }
                is Resource.Error->{
                    showError(it.message.toString())
                }
                is Resource.Loading->{}
            }
        }
    }

    private val launcherMakePost = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {result ->
        goToDetail=false
        if (result.resultCode == RESULT_OK) {
            viewModel.getData()
        }
    }

    private val launcherProfile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {result ->
        Log.d("TAG",result.resultCode.toString())
        if (result.resultCode == RESULT_OK) {
            viewModel.getData()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeForumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()

//        lifecycleScope.launch {
//            viewModel.prepopulate()
//        }

        val layoutManagerForumPost = LinearLayoutManager(this)
        binding.rvForumPost.layoutManager = layoutManagerForumPost
        adapterForum = PagingForumAdapter(onCLick,onCheckChanged,onDelete,viewModel,this)

        binding.rvForumPost.adapter = adapterForum

        viewModel.getData()
        viewModel.pagingData.observe(this) { it ->
            it.observe(this) {
                if (it != null) {
                    adapterForum.submitData(lifecycle, it)
//                    binding.rvForumPost.smoothScrollToPosition(1)
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        lifecycleScope.launch {
            adapterForum.loadStateFlow.collectLatest { loadStates ->
                showLoading(loadStates.refresh is LoadState.Loading)
            }
        }

        binding.btnProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            launcherProfile.launch(intent)
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getData()
        }

        binding.fabAdd.setOnClickListener{
            if(!goToDetail){
                val intent = Intent(this, MakePostActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                launcherMakePost.launch(intent)
            }
        }

        binding.fabChatbot.setOnClickListener{
            val intent = Intent(this, ChatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }

        binding.btnTopikPostForum.setOnClickListener {
            val topicFragment= ForumTopicFragment()
            topicFragment.show(supportFragmentManager,"topic_dialog")
        }

        lifecycleScope.launch {
            viewModel.getListTopik(KategoriTopik.SEMUA).observe(this@HomeForumActivity) {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if(it.data==null||it.data.isEmpty()){
                            Toast.makeText(this@HomeForumActivity, "Gagal mendapatkan topik",Toast.LENGTH_SHORT).show()
                        }

                        it.data?.toMutableList()?.let { it1 ->
                            val common=it1.filter { it.topic_category.trim()=="common topics"  }
                            val commodity= it1.filter { it.topic_category.trim()=="commodity" }
                            viewModel.topicsCommon.value = common.toMutableList()
                            viewModel.topicsCommodity.value=commodity.toMutableList()
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@HomeForumActivity, "Gagal mendapatkan topik",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }


    private fun verified(post:ForumPost,verified:String?){
        viewModel.verifyForumPost(post,verified).observe(this@HomeForumActivity){
            when(it){
                is Resource.Success->{
                    viewModel.onViewEvent(ViewEventsForumPost.Edit2(tempPost!!));tempPost=null
                }
                is Resource.Error->{
                    showError(it.message.toString())
                }
                is Resource.Loading->{}
            }
        }
    }

    private fun showConfirmDialogDelete(post: ForumPost) {
        val builder = AlertDialog.Builder(this)
        val mConfirmDialog = builder.create()
        builder.setTitle(getString(R.string.hapus))
        builder.setMessage(getString(R.string.yakin_ingin_hapus))
        builder.create()

        builder.setPositiveButton(getString(R.string.ya)) { _, _ ->
            delete(post)
            showLoading(false)
        }

        builder.setNegativeButton(getString(R.string.tidak)) { _, _ ->
            mConfirmDialog.cancel()
        }
        builder.show()
    }

    private fun delete(post:ForumPost){
        tempPost=post
        viewModel.deleteForumPost(post).observe(this){
            when(it){
                is Resource.Success->{
                    showLoading(false)
                    it.data?.let {
                        if(tempPost!=null)viewModel.onViewEvent(ViewEventsForumPost.Remove(tempPost!!));tempPost==null
                    }
                }
                is Resource.Error->{
                    showLoading(false)
                    showError(it.message.toString())
                }
                is Resource.Loading->{
                    showLoading(true)
                }
            }
        }
    }
    private fun showLoading(isShowLoading: Boolean) {
        binding.pbLoading.visibility = if (isShowLoading) View.VISIBLE else View.GONE
    }
    private fun showError(msg:String){
        Toast.makeText(binding.root.context, msg,Toast.LENGTH_SHORT).show()
        tempPost?.let { it ->
            viewModel.onViewEvent(ViewEventsForumPost.Rebind(it))
            tempPost=null
        }
    }
    private fun setActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
    }

    override fun handleDataTopic(data: Topic?) {
        binding.btnTopikPostForum.text= data?.topic_name ?: getString(R.string.semua)
        viewModel.mTopics=data
        viewModel.getData(data)
    }
}