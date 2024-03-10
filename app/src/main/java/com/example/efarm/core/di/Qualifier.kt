package com.example.efarm.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CustomBaseUrl