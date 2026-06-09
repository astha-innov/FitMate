package com.fitmate.ui.coach.repository

import com.fitmate.ui.coach.network.Content
import com.fitmate.ui.coach.network.GeminiClient
import com.fitmate.ui.coach.network.GeminiRequest
import com.fitmate.ui.coach.network.Part

class GeminiRepository {

    suspend fun askCoach(
        apiKey: String,
        question: String
    ): String {

        val response =
            GeminiClient.api.generateContent(
                apiKey = apiKey,

                request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(
                                    text =
                                        """
                                    You are FitMate AI Coach.
                                    Give practical fitness advice.
                                    
                                    User Question:
                                    $question
                                    """.trimIndent()
                                )
                            )
                        )
                    )
                )
            )

        return response
            .candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: "No response"
    }
}
