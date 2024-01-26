package com.example.efarm.core.data.source.remote.model

data class Chat(
    val id: String?=null,
    val actor:String="bot",
    val message: String="",
    val timestamp: Long=0
)
