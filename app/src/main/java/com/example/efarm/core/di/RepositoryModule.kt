package com.example.efarm.core.di

import com.example.efarm.core.data.source.repository.AuthRepository
import com.example.efarm.core.domain.repository.IAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [FirebaseModule::class])
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideAuthRepository(authRepository: AuthRepository): IAuthRepository

}