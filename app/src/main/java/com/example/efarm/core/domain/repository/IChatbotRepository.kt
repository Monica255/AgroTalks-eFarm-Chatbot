package com.example.efarm.core.domain.repository

import androidx.lifecycle.MutableLiveData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.data.source.remote.model.ChatBot
import com.example.efarm.core.data.source.remote.model.ResponseChatbot
import kotlinx.coroutines.flow.Flow

interface IChatbotRepository {
    suspend fun sendChat(data: Chat): MutableLiveData<Resource<String>>
    fun getChats(): MutableLiveData<List<Chat>?>
    suspend fun getResponseChatbot(msg: String): Flow<Resource<ResponseChatbot>>

    suspend fun getThreadChatbot(text: String): Flow<Resource<List<ChatBot>>>

}