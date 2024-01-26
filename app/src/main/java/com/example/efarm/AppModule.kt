package com.example.efarm

import com.example.efarm.core.data.source.repository.AuthRepository
import com.example.efarm.core.data.source.repository.ChatbotRepository
import com.example.efarm.core.data.source.repository.ForumRepository
import com.example.efarm.core.domain.usecase.AuthInteractor
import com.example.efarm.core.domain.usecase.AuthUseCase
import com.example.efarm.core.domain.usecase.ChatbotInteractor
import com.example.efarm.core.domain.usecase.ChatbotUseCase
import com.example.efarm.core.domain.usecase.ForumInteractor
import com.example.efarm.core.domain.usecase.ForumUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthUseCase(authRepository: AuthRepository): AuthUseCase=AuthInteractor(authRepository)
    @Provides
    @Singleton
    fun provideForumUseCase(forumRepository: ForumRepository): ForumUseCase = ForumInteractor(forumRepository)

    @Provides
    @Singleton
    fun provideChatbotUseCase(chatbotRepository: ChatbotRepository): ChatbotUseCase = ChatbotInteractor(chatbotRepository)


}