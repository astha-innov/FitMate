package com.fitmate.data.dto

import com.google.gson.annotations.SerializedName

data class Product(

    @SerializedName("product_name")
    val productName: String?,

    @SerializedName("brands")
    val brands: String?,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("nutriments")
    val nutriments: Nutriments?,

    @SerializedName("ingredients_text")
    val ingredientsText: String? = null,

    @SerializedName("nutriscore_grade")
    val nutriscoreGrade: String? = null,

    @SerializedName("nova_group")
    val novaGroup: Int? = null,

    @SerializedName("quantity")
    val quantity: String? = null
)