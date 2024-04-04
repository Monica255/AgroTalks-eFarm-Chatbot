package com.example.efarm.core.util

import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost

const val FORUM_POST_ID="forum_post_id"

const val MIN_VERIFIED_POST=100

const val MIN_VERIFIED_COMMENT=100
const val ADMIN_ID="QTBV6YNiIhdYCiSTXUvSv9wYdJS2"
const val NUM_WORDS=200
const val START_IDX="start"
const val END_IDX="end"

enum class KategoriTopik(val printable:String) {
    SEMUA(""),COMMON("common topics"), COMMODITY("commodity")
}

enum class ChatActor(val printable: String){
    BOT("bot"), USER("user")
}

enum class VoteType(val printable: String){
    UP("upvotes"), DOWN("downvotes")
}

sealed class ViewEventsForumPost {
    data class Edit(val entity: ForumPost, val isLiked:Boolean) : ViewEventsForumPost()

    data class Edit2(val entity: ForumPost) : ViewEventsForumPost()
    data class Remove(val entity: ForumPost) : ViewEventsForumPost()
    data class Rebind(val entity: ForumPost) : ViewEventsForumPost()
}

sealed class ViewEventsVoteComment {
    data class Edit(val entity: CommentForumPost, val isVoted:Boolean,val voteType: VoteType) : ViewEventsVoteComment()
    data class Remove(val entity: CommentForumPost) : ViewEventsVoteComment()
    data class Rebind(val entity: CommentForumPost) : ViewEventsVoteComment()
}