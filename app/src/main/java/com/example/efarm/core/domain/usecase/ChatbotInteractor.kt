package com.example.efarm.core.domain.usecase

import androidx.lifecycle.MutableLiveData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.data.source.remote.model.ChatBot
import com.example.efarm.core.data.source.remote.model.ResponseChatbot
import com.example.efarm.core.domain.repository.IChatbotRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatbotInteractor @Inject constructor(private val repo:IChatbotRepository):ChatbotUseCase {
    override suspend fun sendChat(data: Chat): MutableLiveData<Resource<String>> = repo.sendChat(data)
    override fun getChats(): MutableLiveData<List<Chat>?> = repo.getChats()
    override suspend fun getResponseChatbot(msg: String): Flow<Resource<ResponseChatbot>> = repo.getResponseChatbot(msg)
    override suspend fun getThreadChatbot(text: String): Flow<Resource<ChatBot>> = repo.getThreadChatbot(text)
}