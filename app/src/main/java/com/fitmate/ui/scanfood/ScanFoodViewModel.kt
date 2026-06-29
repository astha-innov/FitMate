package com.fitmate.ui.scanfood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitmate.data.repository.FoodRepositoryImpl
import com.fitmate.domain.usecase.GetFoodByBarcodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanFoodViewModel : ViewModel() {

    // Repository
    private val repository = FoodRepositoryImpl()

    // Use Case
    private val getFoodByBarcodeUseCase =
        GetFoodByBarcodeUseCase(repository)

    // UI State
    private val _uiState = MutableStateFlow(ScanUiState())

    val uiState: StateFlow<ScanUiState> =
        _uiState.asStateFlow()

    /**
     * Called when a barcode is detected from the live camera analyzer.
     * Respects the isScanLocked guard — if already locked, the detection
     * is silently ignored (camera frames can still arrive during teardown).
     */
    fun onBarcodeDetected(barcode: String) {
        if (_uiState.value.isScanLocked) {
            return
        }
        _uiState.value = _uiState.value.copy(isScanLocked = true)
        fetchFood(barcode)
    }

    /**
     * Called by the gallery path BEFORE a barcode is extracted, to
     * immediately lock the UI and show the loading indicator while the
     * bitmap is being decoded and ML Kit runs. This mirrors what the
     * camera path does when it calls onBarcodeDetected.
     */
    fun onGalleryImageSelected() {
        if (_uiState.value.isScanLocked) return
        _uiState.value = _uiState.value.copy(
            isScanLocked = true,
            isLoading    = true,
            error        = null,
            food         = null
        )
    }

    /**
     * Called by the gallery path AFTER a barcode is successfully extracted
     * from the selected image bitmap. Bypasses the isScanLocked guard
     * because onGalleryImageSelected() already set it; calling
     * onBarcodeDetected() here would be silently ignored.
     */
    fun onBarcodeFromGallery(barcode: String) {
        fetchFood(barcode)
    }

    /**
     * Called when the gallery image contained no detectable barcode.
     * Unlocks the scan state and surfaces a user-friendly error via the
     * same error sheet used for camera scanning failures.
     */
    fun onGalleryBarcodeNotFound() {
        _uiState.value = _uiState.value.copy(
            isLoading      = false,
            isScanLocked   = true, // Keep locked so error sheet shows
            food           = null,
            error          = "NO_BARCODE_IN_IMAGE"
        )
    }

    /**
     * Fetch food information from OpenFoodFacts.
     * Single code path used by both camera and gallery.
     */
    private fun fetchFood(barcode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading      = true,
                scannedBarcode = barcode,
                error          = null,
                food           = null
            )

            val result = getFoodByBarcodeUseCase(barcode)

            result.onSuccess { food ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    food      = food,
                    error     = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    food      = null,
                    error     = it.message ?: "Product not found."
                )
            }
        }
    }

    /**
     * Clears the previous scan entirely and unlocks the analyzer so a
     * brand-new scan (camera or gallery) can begin. Used by "Scan Again"
     * and "Done"/"Close".
     */
    fun resetScan() {
        _uiState.value = ScanUiState()
    }

    /**
     * Backward-compatible alias — some call sites may still reference
     * the original clearResult() name.
     */
    fun clearResult() {
        resetScan()
    }

    /**
     * Retry last scanned barcode after a failure, without unlocking
     * the analyzer (we're not re-scanning, just re-fetching).
     */
    fun retry() {
        if (_uiState.value.scannedBarcode.isNotEmpty()) {
            fetchFood(_uiState.value.scannedBarcode)
        }
    }
}