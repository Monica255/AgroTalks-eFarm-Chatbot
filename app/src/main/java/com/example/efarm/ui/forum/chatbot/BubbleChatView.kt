package com.example.efarm.ui.forum.chatbot

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.data.source.remote.model.ChatBot
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.util.ChatActor
import com.google.firebase.auth.FirebaseAuth

class BubbleChatView : RelativeLayout {

    private lateinit var ll_bot: LinearLayout
    private lateinit var ll_user: LinearLayout
    private lateinit var ll_thread: LinearLayout
    private lateinit var tv_bot: TextView
    private lateinit var tv_user: TextView
    private lateinit var tv_title: TextView
    private lateinit var img_header: ImageView
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private var onThreadClickListener: ((String) -> Unit)? = null

    fun setOnThreadClickListener(listener: (String) -> Unit) {
        onThreadClickListener = listener
        ll_thread.setOnClickListener {
            val chatbotId = (ll_thread.tag as? String) ?: return@setOnClickListener
            onThreadClickListener?.invoke(chatbotId)
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
        //setBackgroundResource(R.drawable.bg_card_home)
        gravity = Gravity.CENTER
    }

    private fun init(context: Context) {
        val rootView = inflate(context, R.layout.view_chat, this)
        ll_thread = rootView.findViewById(R.id.ll_thread)
        ll_bot = rootView.findViewById(R.id.ll_chat_bot)
        ll_user = rootView.findViewById(R.id.ll_chat_user)

        tv_bot = rootView.findViewById(R.id.tv_chatbot)
        tv_user = rootView.findViewById(R.id.tv_user)
        tv_title = rootView.findViewById(R.id.tv_title)
        img_header = rootView.findViewById(R.id.img_header)

    }

    fun setData(data: Chat) {
        if (data.actor == ChatActor.USER.printable) {
            tv_user.text = data.message
            ll_bot.visibility = View.GONE
            ll_user.visibility = View.VISIBLE
        } else {
            tv_bot.text = data.message
            ll_bot.visibility = View.VISIBLE
            ll_user.visibility = View.GONE
            ll_thread.visibility = if (data.thread != null) View.VISIBLE else View.GONE
            data.thread?.let { chatbot ->
                if (chatbot.id != null && onThreadClickListener != null) {
                    ll_thread.tag = chatbot.id
                }
                chatbot.confidence?.let {
                    tv_title.text = chatbot.title
                    Glide.with(rootView.context).load(chatbot.img)
                        .placeholder(R.drawable.placeholder).into(img_header)

                }

            }
        }
    }
}