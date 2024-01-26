package com.example.efarm.core.data.source.remote.model


data class ForumPost(
    var id_forum_post:String="",
    var user_id:String="",
    var title:String="",
    var content:String="",
    var img_header:String?=null,
    var timestamp: Long=0,
    var likes:MutableList<String>?= null,
    var comments:MutableList<String>?=null,
    var topics:List<String>?=null,
    var verified:String?=null
)

data class CommentForumPost(
    var id_comment:String="",
    var id_forum_post: String="",
    var content:String="",
    var user_id:String="",
    var timestamp: Long=0,
)

data class Topic(
    val topic_id:String="",
    val topic_name:String="",
    val topic_category:String="",
    val topic_desc:String=""
)