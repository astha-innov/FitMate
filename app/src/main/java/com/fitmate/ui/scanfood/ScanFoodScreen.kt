package com.fitmate.ui.scanfood

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

// ── Palette ───────────────────────────────────────────────────────────────────
private val ScanGreen       = Color(0xFF16C47F)
private val ScanGreenDark   = Color(0xFF0FA363)
private val ScanGreenGlow   = Color(0x4016C47F)
private val ScanGreenLight  = Color(0xFFE8FBF3)
private val OverlayDark     = Color(0xCC000000)
private val OverlayMid      = Color(0x88000000)
private val ErrorRed        = Color(0xFFEF4444)
private val ErrorRedLight   = Color(0x22EF4444)

// ── ScanFoodScreen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanFoodScreen(
    modifier: Modifier = Modifier,
    viewModel: ScanFoodViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()

    // ── Permission ─────────────────────────────────────────────────────────────
    var hasPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // ── Torch ──────────────────────────────────────────────────────────────────
    val cameraController = remember { CameraController() }
    var torchOn by remember { mutableStateOf(false) }

    // ── Gallery picker ─────────────────────────────────────────────────────────
    // Root cause fix: Use GetContent instead of PickVisualMedia for broader
    // compatibility across Android versions, and properly handle the content URI
    // by taking a persistable URI permission before decoding.
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null || uiState.isScanLocked) return@rememberLauncherForActivityResult

        scope.launch {
            // Signal loading state immediately so the UI reflects activity
            viewModel.onGalleryImageSelected()

            val barcode = try {
                // Take persistable permission if available (prevents
                // SecurityException on some OEMs when decoding the stream)
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // Not all URIs support persistable permissions; ignore
                }

                val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }

                if (bitmap == null) {
                    null // Will be treated as "no barcode found"
                } else {
                    scanBitmapForBarcode(bitmap)
                }
            } catch (e: Exception) {
                android.util.Log.e("GalleryPicker", "Failed to load or scan image", e)
                null
            }

            if (barcode != null) {
                // Valid barcode found — hand off to the same ViewModel path
                // as the camera scanner. onGalleryImageSelected() already
                // locked the state; pass directly to fetchFood via a dedicated
                // entry point that skips the lock check.
                viewModel.onBarcodeFromGallery(barcode)
            } else {
                // No barcode detected in the selected image
                viewModel.onGalleryBarcodeNotFound()
            }
        }
    }

    // ── Bottom sheet for result / error ────────────────────────────────────────
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val showSheet = uiState.food != null || uiState.error != null

    // ── Camera paused while locked ─────────────────────────────────────────────
    val cameraPaused = uiState.isScanLocked

    // ── Root ───────────────────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // ── Camera preview (always rendered for performance; analyzer pauses) ──
        if (hasPermission) {
            CameraPreview(
                modifier   = Modifier.fillMaxSize(),
                isPaused   = cameraPaused,
                controller = cameraController,
                onBarcodeDetected = { barcode ->
                    viewModel.onBarcodeDetected(barcode)
                }
            )
        }

        // ── Frosted dim overlay when locked ───────────────────────────────────
        AnimatedVisibility(
            visible = cameraPaused,
            enter   = fadeIn(tween(300)),
            exit    = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        }

        // ── No-permission state ───────────────────────────────────────────────
        if (!hasPermission) {
            NoCameraPermissionContent(
                onGrantClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            )
        }

        // ── Premium scanner overlay (only when actively scanning) ─────────────
        if (hasPermission && !uiState.isLoading && uiState.food == null && uiState.error == null) {
            PremiumScanOverlay(
                isLocked = cameraPaused
            )
        }

        // ── Top bar ───────────────────────────────────────────────────────────
        if (hasPermission) {
            ScanTopBar(
                torchOn       = torchOn,
                hasTorch      = cameraController.hasFlashUnit(),
                onTorchToggle = {
                    torchOn = !torchOn
                    cameraController.setTorchEnabled(torchOn)
                },
                onGalleryClick = {
                    if (!uiState.isScanLocked) {
                        // Launch with a broad image/* MIME type so all gallery
                        // apps (Google Photos, Files, OEM galleries) respond
                        galleryLauncher.launch("image/*")
                    }
                },
                onClose = {
                    // Caller pops back stack; within Activity just finish
                }
            )
        }

        // ── Loading state ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(),
            exit  = fadeOut()
        ) {
            ScanLoadingIndicator()
        }

        // ── Bottom sheet: result or error ──────────────────────────────────────
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.resetScan() },
                sheetState       = sheetState,
                containerColor   = Color(0xFF0F1117),
                tonalElevation   = 0.dp,
                shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                when {
                    uiState.food != null -> {
                        FoodResultCard(
                            food              = uiState.food!!,
                            onAddToDailyIntake = {
                                // TODO: persist to DietRepository / Firestore
                                scope.launch {
                                    sheetState.hide()
                                    viewModel.resetScan()
                                }
                            },
                            onScanAgain = {
                                scope.launch {
                                    sheetState.hide()
                                    viewModel.resetScan()
                                }
                            },
                            onClose = {
                                scope.launch {
                                    sheetState.hide()
                                    viewModel.resetScan()
                                }
                            }
                        )
                    }
                    uiState.error != null -> {
                        ScanErrorSheet(
                            error      = uiState.error!!,
                            onRetry    = { viewModel.retry() },
                            onScanAgain = {
                                scope.launch {
                                    sheetState.hide()
                                    viewModel.resetScan()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────
@Composable
private fun ScanTopBar(
    torchOn:       Boolean,
    hasTorch:      Boolean,
    onTorchToggle: () -> Unit,
    onGalleryClick:() -> Unit,
    onClose:       () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Close
        TopBarIconButton(
            icon        = Icons.Default.Close,
            description = "Close scanner",
            onClick     = onClose
        )

        // Title
        Text(
            text       = "Scan Barcode",
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 16.sp,
            letterSpacing = 0.3.sp
        )

        // Right-side actions
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TopBarIconButton(
                icon        = Icons.Default.Image,
                description = "Pick from gallery",
                onClick     = onGalleryClick
            )
            if (hasTorch) {
                TopBarIconButton(
                    icon        = if (torchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    description = "Toggle torch",
                    onClick     = onTorchToggle,
                    tint        = if (torchOn) ScanGreen else Color.White
                )
            }
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon:        androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick:     () -> Unit,
    tint:        Color = Color.White
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector       = icon,
                contentDescription = description,
                tint               = tint,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ── Premium Scanner Overlay ───────────────────────────────────────────────────
@Composable
private fun PremiumScanOverlay(isLocked: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_anim")

    // Animated scan line sweep
    val scanLineY by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    // Corner bracket pulse
    val bracketAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.7f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bracket_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // Instruction label top
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text       = "Point at any packaged food barcode",
                color      = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize   = 15.sp,
                textAlign  = TextAlign.Center
            )
        }

        // Scanner frame drawn with Canvas
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 280.dp, height = 180.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val r = 24.dp.toPx()
                val armLen = 48.dp.toPx()
                val stroke = 3.dp.toPx()

                // Dark vignette outside frame — punch-out effect
                drawRect(Color.Black.copy(alpha = 0.0f)) // transparent inside, drawn by overlay

                // Corner brackets
                val bracketColor = ScanGreen.copy(alpha = bracketAlpha)

                // TL
                drawCornerBracket(Offset(0f, 0f), armLen, r, stroke, bracketColor, Corner.TopLeft)
                // TR
                drawCornerBracket(Offset(w, 0f), armLen, r, stroke, bracketColor, Corner.TopRight)
                // BL
                drawCornerBracket(Offset(0f, h), armLen, r, stroke, bracketColor, Corner.BottomLeft)
                // BR
                drawCornerBracket(Offset(w, h), armLen, r, stroke, bracketColor, Corner.BottomRight)

                // Scan line
                if (!isLocked) {
                    val lineY = h * scanLineY
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ScanGreen.copy(alpha = 0.9f),
                                ScanGreen,
                                ScanGreen.copy(alpha = 0.9f),
                                Color.Transparent
                            )
                        ),
                        start       = Offset(armLen * 0.5f, lineY),
                        end         = Offset(w - armLen * 0.5f, lineY),
                        strokeWidth = 2.dp.toPx(),
                        cap         = StrokeCap.Round
                    )
                    // Glow under scan line
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ScanGreenGlow,
                                ScanGreenGlow,
                                Color.Transparent
                            )
                        ),
                        start       = Offset(armLen * 0.5f, lineY + 4),
                        end         = Offset(w - armLen * 0.5f, lineY + 4),
                        strokeWidth = 8.dp.toPx(),
                        cap         = StrokeCap.Round
                    )
                }
            }
        }

        // Bottom hint
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ScanGreen)
                )
                Text(
                    text      = "Scanning…",
                    color     = Color.White.copy(alpha = 0.75f),
                    fontSize  = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text      = "Or pick an image from your gallery ↑",
                color     = Color.White.copy(alpha = 0.45f),
                fontSize  = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // Dark vignette panels (top / bottom / left / right — exclude the frame hole)
        VignettePanels()
    }
}

// Draw four semi-transparent panels around the scanner window
@Composable
private fun VignettePanels() {
    val frameW = 280.dp
    val frameH = 180.dp

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val cx       = size.width / 2f
            val cy       = size.height / 2f
            val halfW    = frameW.toPx() / 2f
            val halfH    = frameH.toPx() / 2f
            val panelColor = Color.Black.copy(alpha = 0.62f)

            // Top panel
            drawRect(panelColor, topLeft = Offset(0f, 0f), size = Size(size.width, cy - halfH))
            // Bottom panel
            drawRect(panelColor, topLeft = Offset(0f, cy + halfH), size = Size(size.width, size.height - cy - halfH))
            // Left panel (middle strip)
            drawRect(panelColor, topLeft = Offset(0f, cy - halfH), size = Size(cx - halfW, frameH.toPx()))
            // Right panel (middle strip)
            drawRect(panelColor, topLeft = Offset(cx + halfW, cy - halfH), size = Size(size.width - cx - halfW, frameH.toPx()))
        }
    }
}

private enum class Corner { TopLeft, TopRight, BottomLeft, BottomRight }

private fun DrawScope.drawCornerBracket(
    corner:    Offset,
    armLen:    Float,
    radius:    Float,
    stroke:    Float,
    color:     Color,
    which:     Corner
) {
    val xDir = if (which == Corner.TopLeft || which == Corner.BottomLeft) 1f else -1f
    val yDir = if (which == Corner.TopLeft || which == Corner.TopRight)   1f else -1f

    val path = Path().apply {
        moveTo(corner.x + xDir * armLen, corner.y)
        lineTo(corner.x + xDir * radius, corner.y)
        quadraticBezierTo(
            corner.x, corner.y,
            corner.x, corner.y + yDir * radius
        )
        lineTo(corner.x, corner.y + yDir * armLen)
    }
    drawPath(path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

// ── Loading Indicator ─────────────────────────────────────────────────────────
@Composable
private fun ScanLoadingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.82f))
            .border(1.dp, ScanGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 36.dp, vertical = 28.dp)
    ) {
        CircularProgressIndicator(
            color        = ScanGreen,
            strokeWidth  = 3.dp,
            modifier     = Modifier.size(48.dp)
        )
        Text(
            text       = "Looking up product…",
            color      = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize   = 15.sp
        )
        Text(
            text      = "Fetching nutrition data",
            color     = Color.White.copy(alpha = 0.5f),
            fontSize  = 12.sp
        )
    }
}

// ── Error Sheet ───────────────────────────────────────────────────────────────
@Composable
private fun ScanErrorSheet(
    error:       String,
    onRetry:     () -> Unit,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Handle indicator
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        )

        Spacer(Modifier.height(8.dp))

        // Error icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ErrorRedLight)
                .border(1.dp, ErrorRed.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector       = Icons.Default.Warning,
                contentDescription = null,
                tint               = ErrorRed,
                modifier           = Modifier.size(32.dp)
            )
        }

        Text(
            text       = when {
                error == "NO_BARCODE_IN_IMAGE" -> "No Barcode Found"
                else -> "Product Not Found"
            },
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 20.sp
        )

        Text(
            text      = when {
                error == "NO_BARCODE_IN_IMAGE" ->
                    "No barcode was detected in the selected image. Please choose a clearer photo or scan directly with the camera."
                error == "Product not found." || error.contains("404") || error.contains("not found", ignoreCase = true) ->
                    "This barcode isn't in our database yet. Try scanning again or search manually."
                else -> error
            },
            color     = Color.White.copy(alpha = 0.6f),
            fontSize  = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(4.dp))

        // Retry (re-fetch same barcode) — only shown when we have a barcode
        if (error != "NO_BARCODE_IN_IMAGE") {
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScanGreen),
                shape  = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Retry", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        // Scan again (reset + unlock camera)
        OutlinedButton(
            onClick = onScanAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
            shape  = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Scan Again", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── No Camera Permission ──────────────────────────────────────────────────────
@Composable
private fun NoCameraPermissionContent(onGrantClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ScanGreenLight.copy(alpha = 0.1f))
                    .border(1.dp, ScanGreen.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint               = ScanGreen,
                    modifier           = Modifier.size(40.dp)
                )
            }

            Text(
                text       = "Camera Access Needed",
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 22.sp,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = "FitMate needs camera permission to scan barcodes on packaged foods.",
                color     = Color.White.copy(alpha = 0.6f),
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Button(
                onClick  = onGrantClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScanGreen),
                shape  = RoundedCornerShape(14.dp)
            ) {
                Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
            }
        }
    }
}