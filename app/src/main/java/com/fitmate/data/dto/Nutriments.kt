package com.fitmate.data.dto

import com.google.gson.annotations.SerializedName

data class Nutriments(

    @SerializedName("energy-kcal_100g")
    val calories: Double? = null,

    @SerializedName("proteins_100g")
    val protein: Double? = null,

    @SerializedName("fat_100g")
    val fat: Double? = null,

    @SerializedName("carbohydrates_100g")
    val carbs: Double? = null,

    @SerializedName("fiber_100g")
    val fiber: Double? = null,

    @SerializedName("sugars_100g")
    val sugar: Double? = null,

    @SerializedName("salt_100g")
    val salt: Double? = null,

    @SerializedName("sodium_100g")
    val sodium: Double? = null
)