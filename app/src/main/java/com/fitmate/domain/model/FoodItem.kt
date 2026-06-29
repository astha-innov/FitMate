package com.fitmate.domain.model

data class FoodItem(

    val name: String,

    val brand: String,

    val calories: Double,

    val protein: Double,

    val fat: Double,

    val carbs: Double,

    val fiber: Double = 0.0,

    val sugar: Double = 0.0,

    val salt: Double = 0.0,

    val ingredients: String? = null,

    val nutriScore: String? = null,

    val novaGroup: Int? = null,

    val quantity: String? = null,

    val imageUrl: String?
)