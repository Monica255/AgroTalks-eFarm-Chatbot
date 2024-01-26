package com.example.efarm.core.data.source.repository

import androidx.lifecycle.MutableLiveData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.firebase.FirebaseDataSource
import com.example.efarm.core.data.source.remote.model.UserData
import com.example.efarm.core.domain.repository.IAuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) : IAuthRepository {
    override fun getCurrentUser(): FirebaseUser? = firebaseDataSource.currentUser
    override suspend fun registerAccount(
        email: String,
        pass: String,
        name: String,
        telepon: String
    ): Flow<Resource<String>> = firebaseDataSource.registerAccount(email, pass, name, telepon)

    override suspend fun login(email: String, pass: String): Flow<Resource<String>> = firebaseDataSource.login(email, pass)
    override fun getUserData(uid: String?): MutableLiveData<UserData?> = firebaseDataSource.getUserData(uid)
    override fun signOut() {
        firebaseDataSource.signOut()
    }

}