package com.example.efarm.ui.loginsignup

import com.example.efarm.core.domain.usecase.AuthUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.efarm.core.data.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class LoginSignupViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
) : ViewModel() {

//    var currentUser = authUseCase.getCurrentUser()

    suspend fun login(email: String, pass: String) = authUseCase.login(email, pass).asLiveData()

    suspend fun registerAccount(email: String, pass: String, name: String, telepon: String) =
        authUseCase.registerAccount(email, pass, name, telepon).asLiveData()

    suspend fun firebaseAuthWithGoogle(idToken: String)= authUseCase.firebaseAuthWithGoogle(idToken).asLiveData()

}
