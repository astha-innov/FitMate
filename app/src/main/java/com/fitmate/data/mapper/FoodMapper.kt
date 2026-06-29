package com.fitmate.data.mapper

import com.fitmate.data.dto.Product
import com.fitmate.domain.model.FoodItem

fun Product.toFoodItem(): FoodItem {

    return FoodItem(

        name = productName?.takeIf { it.isNotBlank() } ?: "Unknown Product",

        brand = brands?.takeIf { it.isNotBlank() } ?: "Unknown Brand",

        calories = nutriments?.calories ?: 0.0,

        protein = nutriments?.protein ?: 0.0,

        fat = nutriments?.fat ?: 0.0,

        carbs = nutriments?.carbs ?: 0.0,

        fiber = nutriments?.fiber ?: 0.0,

        sugar = nutriments?.sugar ?: 0.0,

        // Some OpenFoodFacts entries only provide sodium, not salt.
        // Salt (g) ≈ Sodium (g) × 2.5 — used only as a fallback.
        salt = nutriments?.salt ?: nutriments?.sodium?.times(2.5) ?: 0.0,

        ingredients = ingredientsText?.takeIf { it.isNotBlank() },

        nutriScore = nutriscoreGrade?.takeIf { it.isNotBlank() }?.uppercase(),

        novaGroup = novaGroup,

        quantity = quantity?.takeIf { it.isNotBlank() },

        imageUrl = imageUrl
    )
}