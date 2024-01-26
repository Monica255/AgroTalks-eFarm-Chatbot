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
import com.example.efarm.core.util.ViewEventsForumPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val forumUseCase: ForumUseCase,
    private val authUseCase: AuthUseCase
) : ViewModel() {
    var topicsCommodity=MutableLiveData<MutableList<Topic>>()
    var topicsCommon=MutableLiveData<MutableList<Topic>>()
    var mTopics: Topic? = null
    val currentUser = forumUseCase.currentUser

    fun signOut(){authUseCase.signOut()}

    fun getUserdata(uid: String?) = authUseCase.getUserData(uid)

    fun likeForumPost(forumPost: ForumPost) = forumUseCase.likeForumPost(forumPost).asLiveData()

    private lateinit var modificationEventsForumPost :MutableStateFlow<List<ViewEventsForumPost>>

    //Paging
    val pagingData = MutableLiveData<LiveData<PagingData<ForumPost>>>()

    fun getData(topic: Topic? = mTopics) {
        if (mTopics != topic) mTopics = topic
        modificationEventsForumPost=MutableStateFlow(emptyList())
        pagingData.value = forumUseCase.getPagingForum(mTopics)
            .cachedIn(viewModelScope)
            .combine(modificationEventsForumPost) { pagingData, modifications ->
                modifications.fold(pagingData) { acc, event ->
                    //Log.d("TAG","modf "+modifications.toString())
                    applyEventsForumPost(acc, event)
                }
            }.asLiveData()
    }

    suspend fun getListTopik(kategori: KategoriTopik): LiveData<Resource<List<Topic>>> =
        forumUseCase.getListTopikForum(kategori).asLiveData()


    fun onViewEvent(sampleViewEvents: ViewEventsForumPost) {
        modificationEventsForumPost.value += sampleViewEvents
    }

    suspend fun getTopics(topics: List<String>) = forumUseCase.getTopics(topics).asLiveData()
    fun getDetailForum(idForum:String)=forumUseCase.getDetailForum(idForum).asLiveData()

    fun getComments(idForum:String,idBestComment:CommentForumPost?)=forumUseCase.getComments(idForum,idBestComment).asLiveData()

    suspend fun getBestComment(idComment:String)=forumUseCase.getBestComment(idComment).asLiveData()

    suspend fun sendComment(comment:CommentForumPost)=forumUseCase.sendComment(comment).asLiveData()
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
//                Log.d("like",ViewEvents.entity.likes.toString())
                paging
                    .map {
                        if (ViewEvents.entity.id_forum_post == it.id_forum_post) return@map it.copy(
//                            like_count = if (ViewEvents.isLiked) it.like_count + 1 else it.like_count - 1,
                            likes = if (currentUser != null) {
                                var list = mutableListOf<String>()
                                it.likes?.let { it1 -> list.addAll(it1) }
                                if(ViewEvents.isLiked){
                                    list.add(currentUser.uid)
                                }else list.remove(currentUser.uid)
                                list
                            } else it.likes
                        )
                        else return@map it
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
}