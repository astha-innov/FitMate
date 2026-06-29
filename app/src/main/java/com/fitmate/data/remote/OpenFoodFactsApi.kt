package com.fitmate.data.remote

import com.fitmate.data.dto.FoodResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {

    @GET("api/v0/product/{barcode}.json")
    suspend fun getFood(

        @Path("barcode")
        barcode: String

    ): FoodResponse
}