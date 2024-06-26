package com.example.efarm.core.data.source.repository

import android.net.Uri
import androidx.paging.PagingData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.local.FirebasePrepopulate
import com.example.efarm.core.data.source.remote.firebase.FirebaseDataSource
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.domain.repository.IForumRepository
import com.example.efarm.core.util.KategoriTopik
import com.example.efarm.core.util.VoteType
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ForumRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
    private val firebasePrepopulate: FirebasePrepopulate
):IForumRepository {
//    override val currentUser: FirebaseUser?=firebaseDataSource.currentUser
    override fun getPagingForum(topic: Topic?,self:Boolean): Flow<PagingData<ForumPost>> =firebaseDataSource.getPagingForum(topic,self)
    override suspend fun getListTopikForum(kategoriTopik: KategoriTopik): Flow<Resource<List<Topic>>> =firebaseDataSource.getListTopikForum(kategoriTopik)
    override fun likeForumPost(forumPost: ForumPost): Flow<Resource<Pair<Boolean, String?>>> =firebaseDataSource.likeForumPost(forumPost)
    override fun deleteForumPost(forumPost: ForumPost): Flow<Resource<String?>> = firebaseDataSource.deleteForumPost(forumPost)

    override fun getDetailForum(idForum: String): Flow<Resource<ForumPost>> = firebaseDataSource.getDetailForum(idForum)
    override suspend fun getTopics(topics: List<String>): Flow<Resource<List<Topic>>> = firebaseDataSource.getTopics(topics)
    override fun getComments(idForum:String,idBestComment:CommentForumPost?): Flow<PagingData<CommentForumPost>> = firebaseDataSource.getComments(idForum,idBestComment)
    override suspend fun getBestComment(idComment: String): Flow<Resource<CommentForumPost>> = firebaseDataSource.getBestComment(idComment)
    override suspend fun sendComment(comment: CommentForumPost): Flow<Resource<String>> = firebaseDataSource.sendComment(comment)
    override suspend fun uploadThread(data: ForumPost,file: Uri?): Flow<Resource<String>> = firebaseDataSource.uploadThread(data,file)
    override fun verifyForumPost(
        forumPost: ForumPost,
        verify: String?
    ): Flow<Resource<Pair<String?, String?>>> = firebaseDataSource.verifyForumPost(forumPost, verify)

    override fun voteComment(
        comment: CommentForumPost,
        voteType: VoteType
    ): Flow<Resource<Pair<Boolean, String?>>> = firebaseDataSource.voteComment(comment,voteType)

    override suspend fun prepopulate() {
        firebasePrepopulate.prepopulate()
    }
}