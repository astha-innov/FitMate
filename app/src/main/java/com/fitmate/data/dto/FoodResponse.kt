package com.fitmate.data.dto

import com.google.gson.annotations.SerializedName

data class FoodResponse(

    @SerializedName("status")
    val status: Int,

    @SerializedName("product")
    val product: Product?
)