package com.example.efarm.core.util

import com.example.efarm.core.data.source.remote.model.ForumPost

const val FORUM_POST_ID="forum_post_id"

const val MIN_VERIFIED_POST=2

const val MIN_VERIFIED_COMMENT=100

enum class KategoriTopik(val printable:String) {
    SEMUA(""),COMMON("common topics"), COMMODITY("commodity")
}

enum class ChatActor(val printable: String){
    BOT("bot"), USER("user")
}

sealed class ViewEventsForumPost {
    data class Edit(val entity: ForumPost, val isLiked:Boolean) : ViewEventsForumPost()

    data class Edit2(val entity: ForumPost) : ViewEventsForumPost()
    data class Remove(val entity: ForumPost) : ViewEventsForumPost()
    data class Rebind(val entity: ForumPost) : ViewEventsForumPost()
}