package com.example.efarm.core.data.source.remote.Network

import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.Content
import com.example.efarm.core.data.source.remote.model.ContentsRequest
import com.example.efarm.core.data.source.remote.model.Part
import com.example.efarm.core.data.source.remote.model.ResponseChatbot
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

class RemoteDataSource @Inject constructor(private val apiService: ApiService) {
    suspend fun getResponseChatbot(msg: String): Flow<Resource<ResponseChatbot>> = flow {
        val requestData = ContentsRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(
                            text = msg
                        )
                    )
                )
            )
        )
        emit(Resource.Loading())
        val call = apiService.getChat(body = requestData)

        try {
            val response = call.await()
            if (response.isSuccessful) {
                val success = response.body()?.candidates?.get(0)?.content?.parts?.get(0)?.text
                if (success != null && success.isNotEmpty()) {
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
//
//
//@OptIn(ExperimentalCoroutinesApi::class)
//suspend fun <T : Any> Call<T>.await(): Flow<Resource<T>> = flow {
//    try {
//        Log.d("chat","b")
//        val response = execute()
//        Log.d("chat","c")
//        if (response.isSuccessful) {
//            emit(Resource.Success(response.body()!!))
//        } else {
//            emit(Resource.Error("Unsuccessful response: ${response.code()}"))
//        }
//    } catch (e: Exception) {
//        emit(Resource.Error("Request failed: ${e.message.toString()}"))
//    }
//}
