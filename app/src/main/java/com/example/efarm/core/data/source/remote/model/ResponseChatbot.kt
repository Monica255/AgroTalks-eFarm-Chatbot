package com.example.efarm.core.data.source.remote.model


data class Candidate(
    val content: Content,
)

data class ResponseChatbot(
    val candidates: List<Candidate>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class ContentsRequest(
    val contents: List<Content>
)
