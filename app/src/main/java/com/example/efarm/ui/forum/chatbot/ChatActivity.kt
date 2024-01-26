package com.example.efarm.ui.forum.chatbot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityChatBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.util.ChatActor
import com.example.efarm.ui.forum.ForumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    private val viewModel: ChatbotViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
//        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        this.binding.rvChats.layoutManager=layoutManager

        val chatAdapter=ChatAdapter()
        this.binding.rvChats.adapter=chatAdapter

        binding.btnSend.setOnClickListener{
            val msg = binding.etInputChat.text.trim().toString()
            if(msg.isNotEmpty()){
                lifecycleScope.launch {
                    val chat=Chat(null,ChatActor.USER.printable,msg,System.currentTimeMillis())
                    viewModel.sendChat(chat).observe(this@ChatActivity) {
                        binding.etInputChat.setText("")
                        when (it) {
                            is Resource.Success -> {
                                lifecycleScope.launch {
                                    viewModel.getResponseChatbot(msg).observe(this@ChatActivity){
                                        when(it){
                                            is Resource.Success->{
                                                binding.btnSend.isClickable=true
                                                binding.etInputChat.isEnabled = true
                                                binding.loading.visibility= View.GONE
                                                it.data?.let {
                                                    val chat=Chat(null,ChatActor.BOT.printable,it.candidates[0].content.parts[0].text,System.currentTimeMillis())
                                                    sendChat(chat)
                                                }
                                            }
                                            is Resource.Loading->{
                                                Log.d("chat","loading")
                                                binding.loading.visibility= View.VISIBLE
                                            }
                                            is Resource.Error->{
                                                binding.btnSend.isClickable=true
                                                binding.etInputChat.isEnabled = true
                                                binding.loading.visibility= View.GONE
                                                Toast.makeText(this@ChatActivity,"Error mendapatkan respon",Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                                }
                            }

                            is Resource.Error -> {
                                binding.btnSend.isClickable=true
                            }

                            is Resource.Loading -> {
                                binding.btnSend.isClickable=false
                            }

                            else -> {}
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
            Chat(null, ChatActor.BOT.printable, "Selamat datang di chatbot. Ada yang ingin ditanyakan?", time)
        sendChat(initialChat)
    }

    private fun sendChat(data:Chat){
        lifecycleScope.launch {
            viewModel.sendChat(data).observe(this@ChatActivity) {
                when (it) {
                    is Resource.Success -> {
                        binding.etInputChat.isEnabled = true
                        binding.loading.visibility= View.GONE
                    }

                    is Resource.Error -> {
                        binding.loading.visibility= View.GONE
                    }

                    is Resource.Loading -> {
                        binding.loading.visibility= View.VISIBLE
                    }

                    else -> {}
                }
            }
        }
    }
}