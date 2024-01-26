package com.example.efarm.core.domain.usecase

import android.net.Uri
import androidx.paging.PagingData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.util.KategoriTopik
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface ForumUseCase {
    val currentUser: FirebaseUser?
    fun getPagingForum(
        topic: Topic?
    ): Flow<PagingData<ForumPost>>

    suspend fun getListTopikForum(kategoriTopik: KategoriTopik): Flow<Resource<List<Topic>>>

    fun likeForumPost(forumPost: ForumPost): Flow<Resource<Pair<Boolean, String?>>>

    fun getDetailForum(idForum:String): Flow<Resource<ForumPost>>

    suspend fun getTopics(topics:List<String>): Flow<Resource<List<Topic>>>

    fun getComments(idForum:String,idBestComment:CommentForumPost?): Flow<PagingData<CommentForumPost>>

    suspend fun getBestComment(idComment:String):Flow<Resource<CommentForumPost>>

    suspend fun sendComment(comment:CommentForumPost):Flow<Resource<String>>
    suspend fun uploadThread(data:ForumPost,file: Uri?):Flow<Resource<String>>
}