package com.fitmate.ui.scanfood

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handle returned by [CameraPreview], letting the caller control the
 * camera (torch) from outside the AndroidView factory.
 */
class CameraController internal constructor() {

    internal var camera: Camera? = null

    fun setTorchEnabled(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }

    fun hasFlashUnit(): Boolean {
        return camera?.cameraInfo?.hasFlashUnit() == true
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    isPaused: Boolean,
    controller: CameraController = remember { CameraController() },
    onBarcodeDetected: (String) -> Unit
) {

    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    // Tracks pause state inside the analyzer without needing to
    // recreate the camera pipeline every time it changes.
    val pausedState = rememberUpdatedState(isPaused)

    val analyzer = remember {
        BarcodeAnalyzer(
            isPaused = { pausedState.value },
            onBarcodeDetected = onBarcodeDetected
        )
    }

    DisposableEffect(Unit) {

        onDispose {

            cameraExecutor.shutdown()

        }

    }

    AndroidView(

        modifier = modifier.fillMaxSize(),

        factory = { ctx ->

            val previewView = PreviewView(ctx)

            startCamera(

                previewView = previewView,

                lifecycleOwner = lifecycleOwner,

                executor = cameraExecutor,

                analyzer = analyzer,

                controller = controller

            )

            previewView

        }

    )

}

@SuppressLint("UnsafeOptInUsageError")
private fun startCamera(

    previewView: PreviewView,

    lifecycleOwner: LifecycleOwner,

    executor: ExecutorService,

    analyzer: BarcodeAnalyzer,

    controller: CameraController

) {

    val cameraProviderFuture =
        ProcessCameraProvider.getInstance(previewView.context)

    cameraProviderFuture.addListener({

        val cameraProvider =
            cameraProviderFuture.get()

        val preview =
            Preview.Builder()
                .build()

        preview.setSurfaceProvider(
            previewView.surfaceProvider
        )

        val imageAnalyzer =
            ImageAnalysis.Builder()

                .setBackpressureStrategy(
                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                )

                .build()

        imageAnalyzer.setAnalyzer(

            executor,

            analyzer

        )

        try {

            cameraProvider.unbindAll()

            val boundCamera = cameraProvider.bindToLifecycle(

                lifecycleOwner,

                CameraSelector.DEFAULT_BACK_CAMERA,

                preview,

                imageAnalyzer

            )

            controller.camera = boundCamera

        } catch (e: Exception) {

            Log.e(
                "CameraPreview",
                "Camera binding failed",
                e
            )

        }

    }, ContextCompat.getMainExecutor(previewView.context))

}

/**
 * Analyzes live camera frames for barcodes.
 *
 * [isPaused] is polled on every frame rather than captured once, so the
 * caller can pause/resume scanning (e.g. after a detection, or while a
 * result/loading sheet is visible) without tearing down and rebuilding
 * the whole CameraX pipeline.
 *
 * [hasFired] provides an additional one-shot latch so that even if
 * several frames are in flight when a barcode is found, only the first
 * one reaches [onBarcodeDetected]. The caller is responsible for
 * resetting this via [BarcodeAnalyzer.reset] when starting a new scan.
 */
class BarcodeAnalyzer(

    private val isPaused: () -> Boolean,

    private val onBarcodeDetected: (String) -> Unit

) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    private val hasFired = AtomicBoolean(false)

    fun reset() {
        hasFired.set(false)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        // Bail out immediately while paused/locked — don't even run
        // ML Kit on this frame.
        if (isPaused() || hasFired.get()) {

            imageProxy.close()
            return

        }

        val mediaImage = imageProxy.image

        if (mediaImage == null) {

            imageProxy.close()
            return

        }

        val image = InputImage.fromMediaImage(

            mediaImage,

            imageProxy.imageInfo.rotationDegrees

        )

        scanner.process(image)

            .addOnSuccessListener { barcodes ->

                if (barcodes.isNotEmpty() && !hasFired.get()) {

                    val barcode =

                        barcodes.first().rawValue

                    if (!barcode.isNullOrEmpty()) {

                        // Latch immediately so any frame already
                        // in-flight on this executor can't also fire.
                        if (hasFired.compareAndSet(false, true)) {

                            onBarcodeDetected(barcode)

                        }

                    }

                }

            }

            .addOnFailureListener {

                Log.e(

                    "BarcodeAnalyzer",

                    "Barcode scanning failed",

                    it

                )

            }

            .addOnCompleteListener {

                imageProxy.close()

            }

    }

}

/**
 * Scans a single still [Bitmap] for a barcode — used by the
 * "Browse from Gallery" path. Shares the exact same ML Kit scanner
 * configuration as the live camera analyzer, so detection behavior is
 * identical between the two entry points.
 *
 * Returns the raw barcode value, or null if none was found.
 */
suspend fun scanBitmapForBarcode(bitmap: Bitmap): String? {

    val scanner = BarcodeScanning.getClient()

    val image = InputImage.fromBitmap(bitmap, 0)

    return try {

        val barcodes = scanner.process(image).await()

        barcodes.firstOrNull { !it.rawValue.isNullOrEmpty() }?.rawValue

    } catch (e: Exception) {

        Log.e("GalleryScan", "Bitmap barcode scan failed", e)

        null

    }

}