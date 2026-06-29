package com.fitmate.data.repository

import com.fitmate.data.mapper.toFoodItem
import com.fitmate.data.remote.RetrofitClient
import com.fitmate.domain.model.FoodItem
import com.fitmate.domain.repository.FoodRepository

class FoodRepositoryImpl : FoodRepository {

    override suspend fun getFoodByBarcode(
        barcode: String
    ): Result<FoodItem> {

        return try {

            val response =
                RetrofitClient.api.getFood(barcode)

            if (response.status == 1 && response.product != null) {

                Result.success(
                    response.product.toFoodItem()
                )

            } else {

                Result.failure(
                    Exception("Product not found.")
                )

            }

        } catch (e: Exception) {

            Result.failure(e)

        }

    }
}