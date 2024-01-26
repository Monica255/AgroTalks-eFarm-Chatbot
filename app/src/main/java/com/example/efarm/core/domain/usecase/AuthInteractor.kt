package com.example.efarm.core.domain.usecase

import androidx.lifecycle.MutableLiveData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.UserData
import com.example.efarm.core.domain.repository.IAuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthInteractor@Inject constructor(private val repo: IAuthRepository):AuthUseCase {
    override fun getCurrentUser(): FirebaseUser?=repo.getCurrentUser()
    override suspend fun registerAccount(
        email: String,
        pass: String,
        name: String,
        telepon: String
    ): Flow<Resource<String>> =repo.registerAccount(email, pass, name, telepon)

    override suspend fun login(email: String, pass: String): Flow<Resource<String>> = repo.login(email,pass)
    override fun getUserData(uid: String?): MutableLiveData<UserData?> = repo.getUserData(uid)

    override fun signOut() {
        repo.signOut()
    }

}