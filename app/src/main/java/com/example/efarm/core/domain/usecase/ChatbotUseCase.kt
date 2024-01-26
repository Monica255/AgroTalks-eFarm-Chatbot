package com.example.efarm.core.domain.usecase

import androidx.lifecycle.MutableLiveData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.data.source.remote.model.ResponseChatbot
import kotlinx.coroutines.flow.Flow

interface ChatbotUseCase {
    suspend fun sendChat(data: Chat): MutableLiveData<Resource<String>>
    fun getChats(): MutableLiveData<List<Chat>?>
    suspend fun getResponseChatbot(msg: String): Flow<Resource<ResponseChatbot>>
}