package com.example.efarm.core.data.source.remote.Network

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
        @Query("key") key:String = "AIzaSyBG8z7tfHAsk1wYSLTvsu9KHMdFBFpBncY",
        @Body body: ContentsRequest
    ): Call<ResponseChatbot>
}