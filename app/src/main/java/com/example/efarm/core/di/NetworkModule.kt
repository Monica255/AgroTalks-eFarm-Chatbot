package com.example.efarm.core.di

import com.example.efarm.core.data.source.remote.Network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }
    @Provides
    @DefaultBaseUrl
    fun provideDefaultApiService(client: OkHttpClient): ApiService {
        return createApiService(client, "https://generativelanguage.googleapis.com/")
    }
    @Provides
    @CustomBaseUrl
    fun provideCustomApiService(client: OkHttpClient): ApiService {
        return createApiService(client, "https://terbul255-6886dd53-57eb-4791-9ee2-366685f1de78.socketxp.com/")
    }
    private fun createApiService(client: OkHttpClient, baseUrl: String): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(ApiService::class.java)
    }
}
