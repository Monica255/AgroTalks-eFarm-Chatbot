package com.example.efarm.core.di

import com.example.efarm.core.data.source.repository.AuthRepository
import com.example.efarm.core.data.source.repository.ChatbotRepository
import com.example.efarm.core.data.source.repository.ForumRepository
import com.example.efarm.core.domain.repository.IAuthRepository
import com.example.efarm.core.domain.repository.IChatbotRepository
import com.example.efarm.core.domain.repository.IForumRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [FirebaseModule::class])
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideAuthRepository(authRepository: AuthRepository): IAuthRepository

    @Binds
    abstract fun provideChatbotRepository(chatbotRepository: ChatbotRepository): IChatbotRepository

    @Binds
    abstract fun provideForumRepository(forumRepository: ForumRepository): IForumRepository
}


