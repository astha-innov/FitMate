package com.fitmate.domain.repository

import com.fitmate.domain.model.FoodItem

interface FoodRepository {

    suspend fun getFoodByBarcode(
        barcode: String
    ): Result<FoodItem>
}