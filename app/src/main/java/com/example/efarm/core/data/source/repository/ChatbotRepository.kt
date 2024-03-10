package com.example.efarm.core.data.source.repository

import androidx.lifecycle.MutableLiveData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.Network.RemoteDataSource
import com.example.efarm.core.data.source.remote.firebase.FirebaseDataSource
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.data.source.remote.model.ChatBot
import com.example.efarm.core.data.source.remote.model.ResponseChatbot
import com.example.efarm.core.domain.repository.IChatbotRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatbotRepository @Inject constructor(
    private var firebaseDataSource: FirebaseDataSource,
    private val remoteDataSource: RemoteDataSource
):IChatbotRepository {
    override suspend fun sendChat(data: Chat) = firebaseDataSource.sendChat(data)
    override fun getChats(): MutableLiveData<List<Chat>?> = firebaseDataSource.getChats()
    override suspend fun getResponseChatbot(msg: String): Flow<Resource<ResponseChatbot>> = remoteDataSource.getResponseChatbot(msg)
    override suspend fun getThreadChatbot(text: String): Flow<Resource<ChatBot>> = remoteDataSource.getChatThread(text)

}