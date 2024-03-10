package com.example.efarm.core.data.source.remote.Network

import com.example.efarm.core.data.source.remote.model.ChatBot
import com.example.efarm.core.data.source.remote.model.ChatRequest
import com.example.efarm.core.data.source.remote.model.ContentsRequest
import com.example.efarm.core.data.source.remote.model.ResponseChatbot
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("v1beta/models/gemini-pro:generateContent")
    fun getChat(
        @Query("key") key:String = KEY,
        @Body body: ContentsRequest
    ): Call<ResponseChatbot>

    @POST("get")
    fun getSimilarity(
        @Body body:ChatRequest
    ): Call<ChatBot>

    companion object{
        const val KEY="AIzaSyBG8z7tfHAsk1wYSLTvsu9KHMdFBFpBncY"
    }
}

