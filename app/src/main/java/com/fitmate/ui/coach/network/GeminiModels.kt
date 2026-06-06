package com.fitmate.ui.coach.network

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: GeminiContent?
)

data class GeminiContent(
    val parts: List<GeminiPart>?
)

data class GeminiPart(
    val text: String?
)