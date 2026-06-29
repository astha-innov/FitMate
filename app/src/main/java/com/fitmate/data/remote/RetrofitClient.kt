package com.fitmate.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    val api: OpenFoodFactsApi by lazy {

        Retrofit.Builder()

            .baseUrl("https://world.openfoodfacts.org/")

            .client(client)

            .addConverterFactory(GsonConverterFactory.create())

            .build()

            .create(OpenFoodFactsApi::class.java)
    }
}