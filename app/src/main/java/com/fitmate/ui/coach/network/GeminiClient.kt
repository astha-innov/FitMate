package com.fitmate.ui.coach.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiClient {

    private val retrofit =
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()

    val api: GeminiApi =
        retrofit.create(GeminiApi::class.java)
}