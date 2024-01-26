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
import com.example.efarm.core.util.FORUM_POST_ID
import com.example.efarm.core.util.KategoriTopik
import com.example.efarm.core.util.ViewEventsForumPost
import com.example.efarm.ui.forum.chatbot.ChatActivity
import com.example.efarm.ui.forum.detail.DetailForumPostActivity
import com.example.efarm.ui.forum.upload.MakePostActivity
import com.example.efarm.ui.loginsignup.LoginSignupActivity
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

    private val onCLick: ((ForumPost) -> Unit) = { post ->
        val intent = Intent(this, DetailForumPostActivity::class.java)
        intent.putExtra(FORUM_POST_ID, post.id_forum_post)
        startActivity(intent)

    }

    private val onCheckChanged: ((ForumPost) -> Unit) = { post ->
        tempPost=post
        Log.d("like","occ "+post.likes.toString())
        viewModel.likeForumPost(post).observe(this){
            when(it){
                is Resource.Success->{
                    it.data?.let {
                        Log.d("like", it.first.toString())
                        if(tempPost!=null)viewModel.onViewEvent(ViewEventsForumPost.Edit(tempPost!!,it.first));tempPost=null
                    }
                }
                is Resource.Error->{
                    Log.d("like", it.message.toString())
                    Toast.makeText(binding.root.context, it.message.toString(),Toast.LENGTH_SHORT).show()
                    tempPost?.let { it ->
                        viewModel.onViewEvent(ViewEventsForumPost.Rebind(it))
                        tempPost=null
                    }
                }
                else -> {
                    Log.d("like", "else")
                }
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
    private var goToDetail=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeForumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()


        val layoutManagerForumPost = LinearLayoutManager(this)
        binding.rvForumPost.layoutManager = layoutManagerForumPost


        adapterForum = PagingForumAdapter(onCLick,onCheckChanged,viewModel,this)

        binding.rvForumPost.adapter = adapterForum

        binding.btnTopikPostForum.setOnClickListener {
            val topicFragment= ForumTopicFragment()
            topicFragment.show(supportFragmentManager,"topic_dialog")
        }

        lifecycleScope.launch {
            adapterForum.loadStateFlow.collectLatest { loadStates ->
                showLoading(loadStates.refresh is LoadState.Loading)
            }
        }

        viewModel.getData()
        viewModel.pagingData.observe(this) { it ->
            binding.rvForumPost.smoothScrollToPosition(0)
            it.observe(this) {
                if (it != null) {
                    adapterForum.submitData(lifecycle, it)
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        binding.btnLogout.setOnClickListener{
            showConfirmDialog()
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

        lifecycleScope.launch {
            viewModel.getListTopik(KategoriTopik.SEMUA).observe(this@HomeForumActivity) {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if(it.data==null||it.data.isEmpty()){
                            Toast.makeText(this@HomeForumActivity, "Failed to get topics",Toast.LENGTH_SHORT).show()
                            Log.d("TAG","common null")
                        }

                        it.data?.toMutableList()?.let { it1 ->
                            val common=it1.filter { it.topic_category.trim()=="common topics"  }
                            val commodity= it1.filter { it.topic_category.trim()=="commodity" }
                            viewModel.topicsCommon.value = common.toMutableList()
                            viewModel.topicsCommodity.value=commodity.toMutableList()
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@HomeForumActivity, "Failed to get topics",Toast.LENGTH_SHORT).show()
                    }
                }
            }

//            viewModel.getListTopik(KategoriTopik.COMMODITY).observe(this@HomeForumActivity) {
//                when (it) {
//                    is Resource.Loading -> {
//                        Log.d("TAG","Loading commodity")
//                    }
//                    is Resource.Success -> {
//                        if(it.data==null||it.data.isEmpty()){
//                            Log.d("TAG","commodity nul")
//                        }
//                        it.data?.toMutableList()?.let { it1 ->
//                            viewModel.topicsCommodity.value=it1
//                        }
//                    }
//                    is Resource.Error -> {
//                        Toast.makeText(this@HomeForumActivity, "Failed to get topics",Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }

        }

    }

    private fun showConfirmDialog() {
        val builder = AlertDialog.Builder(this)
        val mConfirmDialog = builder.create()
        builder.setTitle(getString(R.string.keluar))
        builder.setMessage(getString(R.string.yakin_ingin_keluar))
        builder.create()

        builder.setPositiveButton(getString(R.string.ya)) { _, _ ->
            viewModel.signOut()
            val intent = Intent(this, LoginSignupActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            showLoading(false)
        }

        builder.setNegativeButton(getString(R.string.tidak)) { _, _ ->
            mConfirmDialog.cancel()
        }
        builder.show()
    }


    private fun showLoading(isShowLoading: Boolean) {
        binding.pbLoading.visibility = if (isShowLoading) View.VISIBLE else View.GONE
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