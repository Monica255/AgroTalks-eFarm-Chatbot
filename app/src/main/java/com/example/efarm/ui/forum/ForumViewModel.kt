package com.example.efarm.ui.forum

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.domain.usecase.AuthUseCase
import com.example.efarm.core.domain.usecase.ForumUseCase
import com.example.efarm.core.util.KategoriTopik
import com.example.efarm.core.util.MIN_VERIFIED_POST
import com.example.efarm.core.util.ViewEventsForumPost
import com.example.efarm.core.util.ViewEventsVoteComment
import com.example.efarm.core.util.VoteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val forumUseCase: ForumUseCase,
    private val authUseCase: AuthUseCase
) : ViewModel() {

    suspend fun prepopulate(){
        forumUseCase.prepopulate()
    }

    var topicsCommodity = MutableLiveData<MutableList<Topic>>()
    var topicsCommon = MutableLiveData<MutableList<Topic>>()
    var mTopics: Topic? = null
    val currentUser = forumUseCase.currentUser

    fun signOut() {
        authUseCase.signOut()
    }

    fun getUserdata(uid: String?) = authUseCase.getUserData(uid)

    fun likeForumPost(forumPost: ForumPost) = forumUseCase.likeForumPost(forumPost).asLiveData()

    private lateinit var modificationEventsForumPost: MutableStateFlow<List<ViewEventsForumPost>>

    private var modificationEventsComment: MutableStateFlow<List<ViewEventsVoteComment>> = MutableStateFlow(emptyList())


    //Paging
    val pagingData = MutableLiveData<LiveData<PagingData<ForumPost>>>()

    fun getData(topic: Topic? = mTopics) {
        if (mTopics != topic) mTopics = topic
        modificationEventsForumPost = MutableStateFlow(emptyList())
        pagingData.value = forumUseCase.getPagingForum(mTopics)
            .cachedIn(viewModelScope)
            .combine(modificationEventsForumPost) { pagingData, modifications ->
                modifications.fold(pagingData) { acc, event ->
                    applyEventsForumPost(acc, event)
                }
            }.asLiveData()
    }

    suspend fun getListTopik(kategori: KategoriTopik): LiveData<Resource<List<Topic>>> =
        forumUseCase.getListTopikForum(kategori).asLiveData()


    fun onViewEvent(sampleViewEvents: ViewEventsForumPost) {
        modificationEventsForumPost.value += sampleViewEvents
    }

    fun onViewEventComment(sampleViewEvents: ViewEventsVoteComment) {
        modificationEventsComment.value += sampleViewEvents
    }

    suspend fun getTopics(topics: List<String>) = forumUseCase.getTopics(topics).asLiveData()
    fun getDetailForum(idForum: String) = forumUseCase.getDetailForum(idForum).asLiveData()

    fun getComments(idForum: String, idBestComment: CommentForumPost?) =
        forumUseCase.getComments(idForum, idBestComment).cachedIn(viewModelScope)
            .combine(modificationEventsComment) { pagingData, modifications ->
                modifications.fold(pagingData) { acc, event ->
                    applyEventsComment(acc, event)
                }
            }.asLiveData()

    suspend fun getBestComment(idComment: String) =
        forumUseCase.getBestComment(idComment).asLiveData()

    suspend fun sendComment(comment: CommentForumPost) =
        forumUseCase.sendComment(comment).asLiveData()

    private fun applyEventsForumPost(
        paging: PagingData<ForumPost>,
        ViewEvents: ViewEventsForumPost
    ): PagingData<ForumPost> {
        return when (ViewEvents) {
            is ViewEventsForumPost.Remove -> {
                paging
                    .filter { ViewEvents.entity.id_forum_post != it.id_forum_post }
            }

            is ViewEventsForumPost.Edit -> {
                paging
                    .map {
                        if (ViewEvents.entity.id_forum_post == it.id_forum_post) {
                            val likes = if (currentUser != null) {
                                var list = mutableListOf<String>()
                                it.likes?.let { it1 -> list.addAll(it1) }
                                if (ViewEvents.isLiked) {
                                    list.add(currentUser.uid)
                                } else list.remove(currentUser.uid)
                                list
                            } else it.likes

                            likes?.size ?: 0
                            return@map it.copy(
                                likes = likes,
//                                verified = if(it.verified==null&&x>= MIN_VERIFIED_POST) "content" else null
                            )
                        } else return@map it
                    }
            }

            is ViewEventsForumPost.Edit2 -> {
                paging
                    .map {
                        if (ViewEvents.entity.id_forum_post == it.id_forum_post) {
                            var x = it.likes?.size ?: 0
                            return@map it.copy(
                                verified = if (it.verified == null && x >= MIN_VERIFIED_POST||it.user_id=="admin") "content" else null
                            )
                        } else return@map it
                    }
            }

            is ViewEventsForumPost.Rebind -> {
                paging.map {
                    if (ViewEvents.entity.id_forum_post == it.id_forum_post) return@map it
                    else return@map it
                }
            }
        }
    }

    fun verifyForumPost(forumPost: ForumPost, verify: String?) =
        forumUseCase.verifyForumPost(forumPost, verify).asLiveData()

    fun voteComment(comment: CommentForumPost, voteType: VoteType) =
        forumUseCase.voteComment(comment, voteType).asLiveData()

    private fun applyEventsComment(
        paging: PagingData<CommentForumPost>,
        ViewEvents: ViewEventsVoteComment
    ): PagingData<CommentForumPost> {
        return when (ViewEvents) {
            is ViewEventsVoteComment.Remove -> {
                paging
                    .filter { ViewEvents.entity.id_comment != it.id_comment }
            }

            is ViewEventsVoteComment.Edit -> {
                Log.d("cv",ViewEvents.entity.id_comment)

                paging
                    .map {
                        if (ViewEvents.entity.id_comment == it.id_comment) {
                            var list: MutableList<String>? = mutableListOf<String>()
                            var list2: MutableList<String>? = mutableListOf<String>()
                            (if (ViewEvents.voteType == VoteType.UP) it.upvotes else it.downvotes)?.let { it1 ->
                                list?.addAll(
                                    it1
                                )
                            }
                            (if (ViewEvents.voteType == VoteType.UP) it.downvotes else it.upvotes)?.let { it1 ->
                                list2?.addAll(
                                    it1
                                )
                            }


                            list = if (currentUser != null) {
                                if (ViewEvents.isVoted) {
                                    list?.add(currentUser.uid)
                                    list2?.remove(currentUser.uid)
                                } else list?.remove(currentUser.uid)
                                list
                            } else if (ViewEvents.voteType == VoteType.UP) it.upvotes else it.downvotes


                            return@map it.copy(
                                upvotes = if (ViewEvents.voteType == VoteType.UP) list else list2,
                                downvotes = if (ViewEvents.voteType == VoteType.DOWN) list else list2
                            )
                        } else return@map it
                    }
            }

            is ViewEventsVoteComment.Rebind -> {
                paging.map {
                    if (ViewEvents.entity.id_comment == it.id_comment) return@map it
                    else return@map it
                }
            }
        }
    }
}