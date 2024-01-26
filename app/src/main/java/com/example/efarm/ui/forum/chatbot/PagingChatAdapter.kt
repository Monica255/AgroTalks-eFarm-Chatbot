package com.example.efarm.ui.forum.chatbot

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.eFarm.databinding.ItemBubbleChatBinding
import com.example.efarm.core.data.source.remote.model.Chat
import com.google.firebase.auth.FirebaseAuth

class PagingChatAdapter : PagingDataAdapter<Chat, PagingChatAdapter.ForumVH>(Companion) {
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    inner class ForumVH(private val binding: ItemBubbleChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: Chat) {
        binding.itemChatBot.setData(chat)
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
            ItemBubbleChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForumVH(binding)
    }

    companion object : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Chat, newItem: Chat): Any? = Any()
    }

}