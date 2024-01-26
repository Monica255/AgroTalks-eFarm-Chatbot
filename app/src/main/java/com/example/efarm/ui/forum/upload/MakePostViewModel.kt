package com.example.efarm.ui.forum.upload

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.domain.usecase.AuthUseCase
import com.example.efarm.core.domain.usecase.ForumUseCase
import com.example.efarm.core.util.KategoriTopik
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MakePostViewModel @Inject constructor(
    private val forumUseCase: ForumUseCase, private val authUseCase: AuthUseCase
) : ViewModel() {
    var tempThread=""
    var topics = MutableLiveData<Set<Topic>>()
    var topicsCommodity= MutableLiveData<MutableList<Topic>>()
    var topicsCommon= MutableLiveData<MutableList<Topic>>()

    val currentUser = forumUseCase.currentUser

    suspend fun getListTopik(kategori: KategoriTopik): LiveData<Resource<List<Topic>>> =
        forumUseCase.getListTopikForum(kategori).asLiveData()

    suspend fun uploadThread(data: ForumPost,file: Uri?)= forumUseCase.uploadThread(data,file).asLiveData()
}