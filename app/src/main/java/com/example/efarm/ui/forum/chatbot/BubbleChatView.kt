package com.example.efarm.ui.forum.chatbot

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.example.eFarm.databinding.ViewChatBinding
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.data.source.remote.model.ChatBot
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.util.ChatActor
import com.google.firebase.auth.FirebaseAuth
import io.noties.markwon.Markwon

class BubbleChatView : RelativeLayout {
    private var onThreadClickListener: ((String,String) -> Unit)? = null

    fun setOnThreadClickListener(listener: (String, String) -> Unit) {
        onThreadClickListener = listener
        binding.llThread1.setOnClickListener {
            val chatbot= (binding.llThread1.tag as? Pair<String,String>) ?: return@setOnClickListener
            onThreadClickListener?.invoke(chatbot.first,chatbot.second)
            Log.d("chatbot","1 clicked "+chatbot)

        }
        binding.llThread2.setOnClickListener {
            val chatbot= (binding.llThread2.tag as? Pair<String,String>) ?: return@setOnClickListener
            onThreadClickListener?.invoke(chatbot.first,chatbot.second)
            Log.d("chatbot","2 clicked "+chatbot)

        }
        binding.llThread3.setOnClickListener {
            val chatbot= (binding.llThread3.tag as? Pair<String,String>) ?: return@setOnClickListener
            onThreadClickListener?.invoke(chatbot.first,chatbot.second)
            Log.d("chatbot","3 clicked "+chatbot)

        }
    }


    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        gravity = Gravity.CENTER
    }
    lateinit var binding:ViewChatBinding
    private fun init(context: Context) {
        val inflater = LayoutInflater.from(context)
        binding = ViewChatBinding.inflate(inflater, this, true)
    }

    fun setData(data: Chat) {
        if (data.actor == ChatActor.USER.printable) {
            binding.tvUser.text = data.message
            binding.llChatBot.visibility = View.GONE
            binding.llChatUser.visibility = View.VISIBLE
        } else {
            val markwon = Markwon.create(context)
            binding.tvChatbot1.text = data.message
            markwon.setMarkdown(binding.tvChatbot1, data.message)
            binding.llChatBot.visibility = View.VISIBLE
            binding.llChatUser.visibility = View.GONE
            if(data.thread.isNullOrEmpty()) {
                binding.llThread1.visibility=View.GONE
                binding.llThread2.visibility=View.GONE
                binding.llThread3.visibility=View.GONE
                binding.tvChatbot2.visibility=View.GONE
                binding.tvChatbot3.visibility=View.GONE
                binding.lable.visibility=View.GONE
            }
            data.thread?.forEachIndexed { index, chatbot ->
                if (chatbot.id != null && onThreadClickListener != null) {
                    when(index){
                        0->binding.llThread1.tag = Pair(chatbot.id,data.message)
                        1->binding.llThread2.tag = Pair(chatbot.id,data.message)
                        2->binding.llThread3.tag = Pair(chatbot.id,data.message)
                    }

                }
                chatbot.confidence?.let {
                    when(index){
                        0->{
                            binding.llThread1.visibility=View.VISIBLE
                            binding.tvTitle1.text = chatbot.title
                            Glide.with(rootView.context).load(chatbot.img)
                                .placeholder(R.drawable.placeholder).into(binding.imgHeader1)
                        }
                        1->{
                            binding.lable.visibility=View.VISIBLE
                            binding.llThread2.visibility=View.VISIBLE
                            binding.tvChatbot2.visibility=View.VISIBLE
                            binding.tvTitle2.text = chatbot.title
                            binding.tvChatbot2.text= chatbot.thread?.trimMargin()
                            Glide.with(rootView.context).load(chatbot.img)
                                .placeholder(R.drawable.placeholder).into(binding.imgHeader2)
                        }
                        2->{
                            binding.llThread3.visibility=View.VISIBLE
                            binding.tvChatbot3.visibility=View.VISIBLE
                            binding.tvTitle3.text = chatbot.title
                            binding.tvChatbot3.text= chatbot.thread?.trimMargin()
                            Glide.with(rootView.context).load(chatbot.img)
                                .placeholder(R.drawable.placeholder).into(binding.imgHeader3)
                        }
                    }
                }
            }
        }
    }
}