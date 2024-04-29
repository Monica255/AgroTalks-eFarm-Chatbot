package com.example.efarm.core.domain.usecase

import android.net.Uri
import androidx.paging.PagingData
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.domain.repository.IForumRepository
import com.example.efarm.core.util.KategoriTopik
import com.example.efarm.core.util.VoteType
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ForumInteractor @Inject constructor(
    private val repo: IForumRepository
) :ForumUseCase{
//    override val currentUser: FirebaseUser?=repo.currentUser

    override fun getPagingForum(topic: Topic?,self:Boolean): Flow<PagingData<ForumPost>> =repo.getPagingForum(topic,self)

    override suspend fun getListTopikForum(kategoriTopik: KategoriTopik): Flow<Resource<List<Topic>>> =repo.getListTopikForum(kategoriTopik)

    override fun likeForumPost(forumPost: ForumPost): Flow<Resource<Pair<Boolean, String?>>> =repo.likeForumPost(forumPost)
    override fun deleteForumPost(forumPost: ForumPost): Flow<Resource<String?>> = repo.deleteForumPost(forumPost)

    override fun getDetailForum(idForum: String): Flow<Resource<ForumPost>> = repo.getDetailForum(idForum)
    override suspend fun getTopics(topics: List<String>) = repo.getTopics(topics)
    override fun getComments(idForum:String,idBestComment:CommentForumPost?): Flow<PagingData<CommentForumPost>> = repo.getComments(idForum,idBestComment)
    override suspend fun getBestComment(idComment: String): Flow<Resource<CommentForumPost>> = repo.getBestComment(idComment)
    override suspend fun sendComment(comment: CommentForumPost): Flow<Resource<String>> = repo.sendComment(comment)
    override suspend fun uploadThread(data: ForumPost,file: Uri?): Flow<Resource<String>> = repo.uploadThread(data,file)
    override fun verifyForumPost(
        forumPost: ForumPost,
        verify: String?
    ): Flow<Resource<Pair<String?, String?>>> = repo.verifyForumPost(forumPost, verify)

    override fun voteComment(
        comment: CommentForumPost,
        voteType: VoteType
    ): Flow<Resource<Pair<Boolean, String?>>> = repo.voteComment(comment, voteType)

    override suspend fun prepopulate() {
        repo.prepopulate()
    }

}