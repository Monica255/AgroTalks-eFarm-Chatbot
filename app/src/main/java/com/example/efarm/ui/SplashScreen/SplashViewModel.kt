package com.example.efarm.ui.SplashScreen

import androidx.lifecycle.ViewModel
import com.example.efarm.core.domain.usecase.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(useCase: AuthUseCase): ViewModel(){
    val currentUser=useCase.getCurrentUser()
}