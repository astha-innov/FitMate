package com.fitmate.domain.usecase

import com.fitmate.domain.repository.FoodRepository

class GetFoodByBarcodeUseCase(

    private val repository: FoodRepository

) {

    suspend operator fun invoke(

        barcode: String

    ) = repository.getFoodByBarcode(barcode)

}