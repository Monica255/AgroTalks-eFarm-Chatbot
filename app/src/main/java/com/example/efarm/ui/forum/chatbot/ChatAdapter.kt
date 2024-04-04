package com.example.efarm.ui.forum.chatbot

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.eFarm.databinding.ItemBubbleChatBinding
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.data.source.remote.model.ForumPost

class ChatAdapter(private val onClick: ((String,String) -> Unit)): RecyclerView.Adapter<ChatAdapter.ItemViewHolder>() {

    var list= mutableListOf<Chat>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.ItemViewHolder {
        val binding =
            ItemBubbleChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ChatAdapter.ItemViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount()=list.size

    inner class ItemViewHolder(private val binding: ItemBubbleChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: Chat) {
            chat.thread?.let {
                binding.itemChatBot.setOnThreadClickListener(onClick)
            }
            binding.itemChatBot.setData(chat)
        }
    }

}