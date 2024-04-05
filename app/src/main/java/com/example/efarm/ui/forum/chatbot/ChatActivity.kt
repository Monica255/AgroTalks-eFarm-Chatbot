package com.example.efarm.ui.forum.chatbot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityChatBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.util.ChatActor
import com.example.efarm.core.util.END_IDX
import com.example.efarm.core.util.FORUM_POST_ID
import com.example.efarm.core.util.NUM_WORDS
import com.example.efarm.core.util.START_IDX
import com.example.efarm.ui.forum.detail.DetailForumPostActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    private val viewModel: ChatbotViewModel by viewModels()


    private val onCLick: ((String,String) -> Unit) = { postid, post->
//        var start :Int?= null
//        var end :Int?= null
//        val data = post.split("||")
//        try {
//            start=data[0].toInt()
//            end=data[1].toInt()
//        }catch (e:Exception){
//            Log.d("detail","No index")
//        }
        val intent = Intent(this, DetailForumPostActivity::class.java)
        intent.putExtra(FORUM_POST_ID, postid)
        intent.putExtra(START_IDX, post)
//        intent.putExtra(END_IDX, end)
        startActivity(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        this.binding.rvChats.layoutManager=layoutManager

        val chatAdapter=ChatAdapter(onCLick)
        this.binding.rvChats.adapter=chatAdapter

        binding.btnSend.setOnClickListener{
            val msg = binding.etInputChat.text.trim().toString()
            if(msg.isNotEmpty()){
                lifecycleScope.launch {
                    val chat=Chat(null,ChatActor.USER.printable,msg,System.currentTimeMillis())
                    viewModel.sendChat(chat).observe(this@ChatActivity) { it ->
                        binding.etInputChat.setText("")
                        when (it) {
                            is Resource.Success -> {
                                getThreadChatbot(msg)
                            }

                            is Resource.Error -> {
                                binding.btnSend.isClickable=true
                            }

                            is Resource.Loading -> {
                                binding.btnSend.isClickable=false
                            }
                        }
                    }
                }

            }
        }

        viewModel.getChats().observe(this@ChatActivity) {
            it?.let {
                if (it.isNotEmpty()) {
                    chatAdapter.list = it.toMutableList()
                    chatAdapter.notifyDataSetChanged()
                    val lastIndex = it.size - 1
                    binding.rvChats.scrollToPosition(lastIndex)
                    binding.etInputChat.isEnabled=true
                } else {
                    binding.etInputChat.isEnabled=true
                    sendInitialChat()
                }
            } ?: sendInitialChat().also { binding.etInputChat.isEnabled=true }
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }
    private fun sendInitialChat(){
        val time = System.currentTimeMillis()
        val initialChat =
            Chat(null, ChatActor.BOT.printable, getString(R.string.initial_chat), time)
        sendChat(initialChat)
    }

    private fun getThreadChatbot(msg:String){
        lifecycleScope.launch {
            viewModel.getThreadChatbot(msg).observe(this@ChatActivity){
                when(it){
                    is Resource.Loading -> {
                        showLoading(true)
                    }
                    is Resource.Error ->{
                        showLoading(false)
//                        it.message?.let{
//                            Toast.makeText(this@ChatActivity,
//                              it,Toast.LENGTH_SHORT).show()
//                        }
                        getResponseChatbot(msg)
                    }
                    is Resource.Success ->{
                        it.data?.let {chatbot->
                            if(chatbot.thread==null) showLoading(false)
                            chatbot.thread?.let{thread->
                                lifecycleScope.launch {
                                    viewModel.getResponseChatbot(makePromt(msg,thread)).observe(this@ChatActivity){
                                        when(it){
                                            is Resource.Success->{
                                                showLoading(false)

                                                it.data?.let {
                                                    val chat=Chat(null,ChatActor.BOT.printable,it.candidates[0].content.parts[0].text,System.currentTimeMillis(),chatbot)
                                                    sendChat(chat)
                                                }
                                            }
                                            is Resource.Loading->{
                                                showLoading(true)
                                            }
                                            is Resource.Error->{
                                                showLoading(false)
                                                Toast.makeText(this@ChatActivity,
                                                    getString(R.string.error_getting_respon),Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
//                                val chat=Chat(null,ChatActor.BOT.printable,thread,System.currentTimeMillis(),chatbot)
//                                sendChat(chat)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun makePromt(questions:String,context:String): String {
        return """Give me the answer for this question \"${questions}\" from this context bellow using Indonesia language with maximum $NUM_WORDS words!
            
            $context
            
            if you cannot find the answer in the context, just summarize the context back as a response with maximum $NUM_WORDS words in bahasa indonesia!
            
            Do not say anything about the prompt! just do your job.
        """.trimMargin()
    }

    private fun getResponseChatbot(msg: String){
        lifecycleScope.launch {
            viewModel.getResponseChatbot(msg).observe(this@ChatActivity){
                when(it){
                    is Resource.Success->{
                        showLoading(false)
                        it.data?.let {
                            val chat=Chat(null,ChatActor.BOT.printable,it.candidates[0].content.parts[0].text,System.currentTimeMillis())
                            sendChat(chat)
                        }
                    }
                    is Resource.Loading->{
                        showLoading(true)
                    }
                    is Resource.Error->{
                        showLoading(false)
                        Toast.makeText(this@ChatActivity,
                            getString(R.string.error_getting_respon),Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }

    private fun showLoading(isLoading:Boolean){
        binding.btnSend.isClickable=!isLoading
        binding.etInputChat.isEnabled = !isLoading
        binding.loading.visibility= if(isLoading) View.VISIBLE else View.GONE
    }
    private fun sendChat(data:Chat){
        lifecycleScope.launch {
            viewModel.sendChat(data).observe(this@ChatActivity) {
                when (it) {
                    is Resource.Success -> {
                        showLoading(false)
                    }

                    is Resource.Error -> {
                        showLoading(false)
                    }

                    is Resource.Loading -> {
                        showLoading(true)
                    }
                }
            }
        }
    }
}