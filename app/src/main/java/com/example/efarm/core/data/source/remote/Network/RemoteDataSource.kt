package com.example.efarm.core.data.source.remote.Network

import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.ChatBot
import com.example.efarm.core.data.source.remote.model.ChatRequest
import com.example.efarm.core.data.source.remote.model.Content
import com.example.efarm.core.data.source.remote.model.ContentsRequest
import com.example.efarm.core.data.source.remote.model.Part
import com.example.efarm.core.data.source.remote.model.ResponseChatbot
import com.example.efarm.core.di.CustomBaseUrl
import com.example.efarm.core.di.DefaultBaseUrl
import com.example.efarm.core.util.NUM_WORDS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton

class RemoteDataSource @Inject constructor(
    @DefaultBaseUrl private val defaultApiService: ApiService,
    @CustomBaseUrl private val customApiService: ApiService
) {

    suspend fun getChatThread(text: String): Flow<Resource<ChatBot>> = flow {
        val req = ChatRequest(text)
        emit(Resource.Loading())
        val call = customApiService.getSimilarity(req)

        try {
            val response = call.await()
            if (response.isSuccessful) {
                val success = response.body()
                success?.confidence?.let {
                    if (success.confidence.toFloat() >= 0.8) {
                        emit(Resource.Success(response.body()!!))
                    } else {
                        emit(Resource.Error("Empty response"))
                    }
                }

            } else {
                emit(Resource.Error("Unsuccessful response: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error processing response: ${e.message}"))
        }
    }

    suspend fun getResponseChatbot(msg: String): Flow<Resource<ResponseChatbot>> = flow {
        val requestData = ContentsRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(
                            text = preProcess(msg)
                        )
                    )
                )
            )
        )
        emit(Resource.Loading())
        val call = defaultApiService.getChat(body = requestData)

        try {
            val response = call.await()
            if (response.isSuccessful) {
                val success = response.body()?.candidates?.get(0)?.content?.parts?.get(0)?.text
                if (!success.isNullOrEmpty()) {
                    emit(Resource.Success(response.body()!!))
                } else {
                    emit(Resource.Error("Empty response"))
                }
            } else {
                emit(Resource.Error("Unsuccessful response: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error processing response: ${e.message}"))
        }
    }

    private fun preProcess(msg: String):String{
        return msg + """\n
            Response using Indonesia language with maximum $NUM_WORDS words!
        """.trimIndent()
    }
    private suspend fun <T : Any> Call<T>.await(): Response<T> {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}
