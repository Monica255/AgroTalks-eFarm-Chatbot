package com.example.efarm.core.data.source.remote.model

import android.net.Uri


data class ForumPost(
    var id_forum_post:String="",
    var user_id:String="",
    var title:String="",
    var img_header:String?=null,
    var timestamp: Long=0,
    var likes:MutableList<String>?= null,
    var comments:MutableList<String>?=null,
    var topics:List<String>?=null,
    var verified:String?=null,
    var link:String?=null,
    var thread:Thread?=null
)

data class Thread(
    val thread:String="",
    var images:MutableList<Images>?=null
)

data class Images(
    val position:Int=0,
    var image:String?=null
)

data class CommentForumPost(
    var id_comment:String="",
    var id_forum_post: String="",
    var content:String="",
    var user_id:String="",
    var timestamp: Long=0,
    var upvotes:MutableList<String>?=null,
    var downvotes:MutableList<String>?=null
)

data class Topic(
    val topic_id:String="",
    val topic_name:String="",
    val topic_category:String="",
    val topic_desc:String=""
)