package com.example.efarm.core.data.source.remote.firebase

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await


class CommentForumPagingStore (
    private val query: Query,
    private val bestComment:CommentForumPost?
) : PagingSource<QuerySnapshot, CommentForumPost>() {
    var list= mutableListOf<CommentForumPost>()
    override fun getRefreshKey(state: PagingState<QuerySnapshot, CommentForumPost>): QuerySnapshot? {
        return null
    }
    var firstPage=true
    var specificItem:CommentForumPost?=null
    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, CommentForumPost> {
        return try {
            val currentPage = params.key ?: query.get().await()
            var lastVisibleProduct: DocumentSnapshot
            var nextPage: QuerySnapshot?
            if(currentPage.size()!=0){
//                Log.d("TAG","cps "+currentPage.size())
                lastVisibleProduct = currentPage.documents[currentPage.size() - 1]
                nextPage = query.startAfter(lastVisibleProduct).get().await()
            }else{
                nextPage=null
            }
            list= mutableListOf()
            list.addAll(currentPage.toObjects(CommentForumPost::class.java))

            bestComment?.let{
                specificItem = list.find { it.id_comment == bestComment.id_comment }
                specificItem?.let {
                    list.remove(specificItem)
                }
                if(firstPage){
                    list.add(0,it)
                    firstPage=false
                }
            }
            LoadResult.Page(
                data = list,
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            Log.d("TAG","error "+e.message.toString())
            LoadResult.Error(e)
        }
    }
}