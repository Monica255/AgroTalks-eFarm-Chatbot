package com.example.efarm.ui.forum.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityProfileBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.UserData
import com.example.efarm.core.util.ADMIN_ID
import com.example.efarm.core.util.FORUM_POST_ID
import com.example.efarm.core.util.MIN_VERIFIED_POST
import com.example.efarm.core.util.ViewEventsForumPost
import com.example.efarm.ui.forum.ForumViewModel
import com.example.efarm.ui.forum.PagingForumAdapter
import com.example.efarm.ui.forum.detail.DetailForumPostActivity
import com.example.efarm.ui.loginsignup.LoginSignupActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    lateinit var binding:ActivityProfileBinding
    private val viewModel: ForumViewModel by viewModels()
    private lateinit var adapterForum: PagingForumAdapter
    private var tempPost:ForumPost?=null
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
                            if(likes>= MIN_VERIFIED_POST &&post.verified==null){
                                verified(post,"content")
                            }else if (likes<= MIN_VERIFIED_POST &&post.verified!=null){
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()

        viewModel.currentUser?.uid?.let {
            viewModel.getUserdata(it).observe(this){
                if(it!=null){
                    binding.cvProfile.visibility=View.VISIBLE
                    setDataProfile(it)
                }else{
                    binding.cvProfile.visibility=View.GONE
                }
            }
        }

        val layoutManagerForumPost = LinearLayoutManager(this)
        binding.rvForumPost.layoutManager = layoutManagerForumPost
        adapterForum = PagingForumAdapter(onCLick,onCheckChanged,onDelete,viewModel,this@ProfileActivity)

        binding.rvForumPost.adapter = adapterForum

        viewModel.getData(self=true)
        viewModel.pagingData.observe(this) { it ->
            binding.rvForumPost.smoothScrollToPosition(0)
            it.observe(this) {
                if (it != null) {
                    binding.swipeRefresh.isRefreshing = false
                    adapterForum.submitData(lifecycle, it)
                }
            }
        }


        binding.btnLogout.setOnClickListener{
            showConfirmDialog()
        }

        lifecycleScope.launch {
            adapterForum.loadStateFlow.collectLatest { loadStates ->
                showLoading(loadStates.refresh is LoadState.Loading)
            }
        }

    }

    private fun setDataProfile(userData: UserData){
        binding.tvName.text = userData.name?:"Unknown"
        binding.tvEmail.text = userData.email?:"-"
        binding.tvNumber.text = userData.phone_number?:"-"
    }

    private fun setActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
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
                    setResult(RESULT_OK)
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
        Toast.makeText(binding.root.context, msg, Toast.LENGTH_SHORT).show()
        tempPost?.let { it ->
            viewModel.onViewEvent(ViewEventsForumPost.Rebind(it))
            tempPost=null
        }
    }
    private fun verified(post:ForumPost,verified:String?){
        viewModel.verifyForumPost(post,verified).observe(this){
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}