package com.fitmate.ui.scanfood

import com.fitmate.domain.model.FoodItem

data class ScanUiState(

    val isLoading: Boolean = false,

    val scannedBarcode: String = "",

    val food: FoodItem? = null,

    val error: String? = null,

    // True the instant a barcode is detected (camera or gallery) until
    // the user taps "Scan Again". While true, the analyzer must not
    // emit further detections and the preview is frozen/dimmed.
    val isScanLocked: Boolean = false
)