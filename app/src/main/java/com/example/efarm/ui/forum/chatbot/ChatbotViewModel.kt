package com.example.efarm.ui.forum.chatbot

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.domain.usecase.AuthUseCase
import com.example.efarm.core.domain.usecase.ChatbotUseCase
import com.example.efarm.core.domain.usecase.ForumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val chatbotUseCase: ChatbotUseCase
) : ViewModel(){
    suspend fun sendChat(data: Chat) = chatbotUseCase.sendChat(data)
    fun getChats() = chatbotUseCase.getChats()
    suspend fun getResponseChatbot(msg: String) = chatbotUseCase.getResponseChatbot(msg).asLiveData()
}