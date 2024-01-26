package com.example.efarm.core.data.source.remote.firebase

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class ChatPagingStore (private val query: Query): PagingSource<QuerySnapshot, Chat>(){
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Chat>): QuerySnapshot? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Chat> {
        TODO("Not yet implemented")
    }
}