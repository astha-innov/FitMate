package com.fitmate.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.fitmate.ui.components.FitMateLogoMark
import kotlin.math.pow
import kotlin.random.Random

// ═════════════════════════════════════════════════════════════════════════════
//  DESIGN TOKENS — Premium White · Soft Glass · Emerald Green
// ═════════════════════════════════════════════════════════════════════════════

private val BgVoid        = Color(0xFFF8FAFC)           // SoftWhite background
private val BgPure        = Color(0xFFFFFFFF)           // Pure white
private val PrimaryGreen  = Color(0xFF10B981)           // Emerald primary accent
private val SoftGreen     = Color(0xFF34D399)           // Lighter emerald
private val FaintGreen    = Color(0xFFD1FAE5)           // Very faint green tint
private val DarkText      = Color(0xFF111827)           // Primary text
private val SecondaryText = Color(0xFF6B7280)           // Secondary text
private val LightGray     = Color(0xFFE5E7EB)           // Borders / dividers
private val GlassWhite    = Color(0xFFFFFFFF).copy(alpha = 0.72f)  // Glass fill
private val SurfaceBorder = Color(0xFFE5E7EB).copy(alpha = 0.80f)  // Glass border
private val ShadowGreen   = Color(0x2010B981)           // Green shadow / glow
private val FaintShadow   = Color(0x0A111827)           // Subtle dark shadow
private val ChromeDim     = Color(0xFF6B7280)           // Muted label text

private val Orbitron  = FontFamily.Monospace
private val Rajdhani  = FontFamily.SansSerif

// ═════════════════════════════════════════════════════════════════════════════
//  PARTICLE DATA MODEL  (unchanged logic — visual colours swapped)
// ═════════════════════════════════════════════════════════════════════════════

private data class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var radius: Float,
    var baseAlpha: Float,
    var life: Float,
    var decay: Float,
    var isWhite: Boolean
)

private fun spawnParticle(w: Float, h: Float, randomY: Boolean) = Particle(
    x         = Random.nextFloat() * w,
    y         = if (randomY) Random.nextFloat() * h else h + 10f,
    vx        = (Random.nextFloat() - 0.5f) * 0.4f,
    vy        = -(0.4f + Random.nextFloat() * 0.9f),
    radius    = Random.nextFloat() * 1.6f + 0.3f,
    baseAlpha = Random.nextFloat() * 0.32f + 0.06f,
    life      = 1f,
    decay     = 0.002f + Random.nextFloat() * 0.003f,
    isWhite   = Random.nextFloat() < 0.55f
)

// ═════════════════════════════════════════════════════════════════════════════
//  SPLASH SCREEN — root composable
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    val statuses = remember {
        listOf(
            "PREPARING YOUR PLAN...",
            "LOADING BIOMETRICS...",
            "SYNCING AI ENGINE...",
            "PERSONALIZING EXPERIENCE...",
            "READY."
        )
    }
    var statusText by remember { mutableStateOf(statuses[0]) }
    val progress   = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            progress.animateTo(
                targetValue   = 1f,
                animationSpec = tween(durationMillis = 5200, easing = LinearEasing)
            )
        }
        statuses.forEachIndexed { i, label ->
            if (i > 0) delay(1050L)
            statusText = label
        }
        delay(600)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgVoid)
    ) {
        PerspectiveGrid()
        ParticleField()
        BackgroundGlows()
        HeartbeatLine()
        ScanSweep()

        CornerDecorations()
        FloatingHoloStats()

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(54.dp))
            TopHudBar()
            Spacer(Modifier.weight(1f))
            CenterLogo()
            Spacer(Modifier.weight(1f))
            BottomHud(statusText = statusText, progress = progress.value)
            Spacer(Modifier.height(56.dp))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  1. PERSPECTIVE GRID
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun PerspectiveGrid() {
    val inf = rememberInfiniteTransition(label = "grid")
    val offset by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 80f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label         = "gridScroll"
    )

    Canvas(Modifier.fillMaxSize()) {
        val cx      = size.width / 2f
        val horizon = size.height * 0.28f
        val gridCol = PrimaryGreen.copy(alpha = 0.04f)

        for (i in -8..12) {
            drawLine(
                color       = gridCol,
                start       = Offset(cx, horizon),
                end         = Offset(i * size.width / 9f, size.height),
                strokeWidth = 1f
            )
        }

        for (i in 0..14) {
            val t    = i / 14f
            val yPos = horizon +
                    t.pow(1.8f) * (size.height - horizon) +
                    offset * t.pow(1.8f)
            if (yPos > size.height) continue
            drawLine(
                color       = PrimaryGreen.copy(alpha = t.pow(1.2f) * 0.045f),
                start       = Offset(0f, yPos),
                end         = Offset(size.width, yPos),
                strokeWidth = 1f
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  2. PARTICLE FIELD
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun ParticleField() {
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val particles  = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(canvasSize) {
        if (canvasSize == Size.Zero) return@LaunchedEffect
        if (particles.isEmpty()) {
            repeat(110) { particles.add(spawnParticle(canvasSize.width, canvasSize.height, true)) }
        }
        while (true) {
            withFrameMillis {
                for (i in particles.indices) {
                    val p = particles[i]
                    p.x    += p.vx
                    p.y    += p.vy
                    p.life -= p.decay
                    if (p.life <= 0f || p.y < -10f) {
                        particles[i] = spawnParticle(canvasSize.width, canvasSize.height, false)
                    }
                }
            }
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        if (size != canvasSize) canvasSize = size
        particles.forEach { p ->
            val a     = p.baseAlpha * p.life
            // Soft green or light-gray particles on white background
            val color = if (p.isWhite) LightGray.copy(alpha = a * 0.9f)
            else           PrimaryGreen.copy(alpha = a * 0.45f)
            drawCircle(color, p.radius, Offset(p.x, p.y))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  3. BACKGROUND GLOWS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun BackgroundGlows() {
    Canvas(Modifier.fillMaxSize()) {
        val ctr = Offset(size.width / 2f, size.height * 0.45f)

        // Central soft green bloom
        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(FaintGreen.copy(alpha = 0.55f), Color.Transparent),
                center = ctr,
                radius = size.width * 0.85f
            ),
            radius = size.width * 0.85f,
            center = ctr
        )

        // Top-left white glint
        val topL = Offset(size.width * 0.18f, size.height * 0.12f)
        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(BgPure.copy(alpha = 0.85f), Color.Transparent),
                center = topL,
                radius = 280f
            ),
            radius = 280f,
            center = topL
        )

        // Bottom-right green glint
        val botR = Offset(size.width * 0.85f, size.height * 0.82f)
        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(SoftGreen.copy(alpha = 0.12f), Color.Transparent),
                center = botR,
                radius = 220f
            ),
            radius = 220f,
            center = botR
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  4. HEARTBEAT LINE
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun HeartbeatLine() {
    var time by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) { while (true) { withFrameMillis { time = it } } }

    Canvas(Modifier.fillMaxSize()) {
        val baseY = size.height * 0.72f
        val cycle = (time % 2000L) / 2000f
        val pulse = if (cycle < 0.75f) 1f
        else (1f - (cycle - 0.75f) / 0.25f).coerceIn(0f, 1f)

        val pts = listOf(
            0f to 0f, 0.14f to 0f, 0.19f to -12f, 0.22f to 18f,
            0.26f to -42f, 0.31f to 28f, 0.35f to -10f, 0.39f to 0f,
            0.54f to 0f, 0.59f to -9f, 0.64f to 12f, 0.69f to -26f,
            0.74f to 16f, 0.79f to -6f, 0.84f to 0f, 1f to 0f
        )

        val ecgPath = Path().apply {
            pts.forEachIndexed { i, (xr, dy) ->
                val px = xr * size.width
                val py = baseY + dy * pulse
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
        }

        // Outer soft green glow
        drawPath(ecgPath, PrimaryGreen.copy(alpha = 0.06f), style = Stroke(width = 8f,   cap = StrokeCap.Round))
        // Mid green line
        drawPath(ecgPath, PrimaryGreen.copy(alpha = 0.40f), style = Stroke(width = 2f,   cap = StrokeCap.Round))
        // Bright white highlight on top
        drawPath(ecgPath, BgPure.copy(alpha = 0.80f),       style = Stroke(width = 0.8f, cap = StrokeCap.Round))
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  5. SCAN SWEEP
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun ScanSweep() {
    val inf = rememberInfiniteTransition(label = "scan")
    val scanFrac by inf.animateFloat(
        initialValue  = -0.02f,
        targetValue   = 1.02f,
        animationSpec = infiniteRepeatable(tween(3800, easing = LinearEasing)),
        label         = "scanY"
    )

    Canvas(Modifier.fillMaxSize()) {
        val y = scanFrac * size.height
        drawRect(
            brush   = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    PrimaryGreen.copy(alpha = 0.04f),
                    PrimaryGreen.copy(alpha = 0.10f),
                    PrimaryGreen.copy(alpha = 0.04f),
                    Color.Transparent
                )
            ),
            topLeft = Offset(0f, y - 1f),
            size    = Size(size.width, 2f)
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  6. CORNER DECORATIONS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun CornerDecorations() {
    Canvas(Modifier.fillMaxSize()) {
        val arm = 30f; val thick = 1.5f
        val col = PrimaryGreen.copy(alpha = 0.35f)
        val m   = 22f

        fun corner(x: Float, y: Float, dx: Float, dy: Float) {
            drawLine(col, Offset(x, y), Offset(x + arm * dx, y), thick)
            drawLine(col, Offset(x, y), Offset(x, y + arm * dy), thick)
        }

        corner(m, m, 1f, 1f)
        corner(size.width - m, m, -1f, 1f)
        corner(m, size.height - m, 1f, -1f)
        corner(size.width - m, size.height - m, -1f, -1f)
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  7. FLOATING HOLOGRAPHIC STATS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun FloatingHoloStats() {
    val inf = rememberInfiniteTransition(label = "floatStats")

    @Composable
    fun floatY(period: Int, reversed: Boolean): Dp {
        val raw by inf.animateFloat(
            -5f, 5f,
            infiniteRepeatable(tween(period, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "fy$period"
        )
        return (if (reversed) -raw else raw).dp
    }

    Box(Modifier.fillMaxSize()) {
        HoloStatCard("98.2",  "VO₂ MAX",    Modifier.padding(start = 18.dp, top = 178.dp).offset(y = floatY(3800, false)))
        HoloStatCard("72 BPM","HEART RATE", Modifier.align(Alignment.TopEnd).padding(top = 236.dp, end = 18.dp).offset(y = floatY(4600, true)))
        HoloStatCard("2,840", "KCAL",       Modifier.padding(start = 22.dp, top = 328.dp).offset(y = floatY(3200, false)))
    }
}

@Composable
private fun HoloStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(1900)
        alpha.animateTo(1f, tween(700))
    }

    Column(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha.value }
            .background(
                brush = Brush.verticalGradient(
                    listOf(BgPure.copy(alpha = 0.92f), BgPure.copy(alpha = 0.78f))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(LightGray, PrimaryGreen.copy(alpha = 0.20f))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            // Soft shadow via outer glow layer is handled by elevation; simulate with border
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Active green indicator dot
        Box(
            Modifier
                .size(5.dp)
                .background(PrimaryGreen, CircleShape)
        )
        Text(
            text  = value,
            style = TextStyle(
                fontFamily = Orbitron,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = DarkText
            )
        )
        Text(
            text  = label,
            style = TextStyle(
                fontFamily    = Rajdhani,
                fontSize      = 8.sp,
                letterSpacing = 2.sp,
                color         = SecondaryText
            )
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  8. TOP HUD BAR
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun TopHudBar() {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(800)
        alpha.animateTo(1f, tween(600))
    }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha.value },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "v2.4",
            style = TextStyle(
                fontFamily    = Orbitron,
                fontSize      = 9.sp,
                letterSpacing = 3.sp,
                color         = SecondaryText.copy(alpha = 0.60f)
            )
        )
        Box(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, PrimaryGreen.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )
        Text(
            "AI ENGINE",
            style = TextStyle(
                fontFamily    = Orbitron,
                fontSize      = 9.sp,
                letterSpacing = 3.sp,
                color         = SecondaryText.copy(alpha = 0.60f)
            )
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  9. CENTER LOGO SECTION
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun CenterLogo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        OrbitalLogoRing()
        Spacer(Modifier.height(28.dp))
        BrandText()
        Spacer(Modifier.height(28.dp))
        StatPillsRow()
        Spacer(Modifier.height(20.dp))
        HoloBars()
    }
}

// ── Orbital Rings + Core ──────────────────────────────────────────────────────

@Composable
private fun OrbitalLogoRing() {
    val inf = rememberInfiniteTransition(label = "rings")

    val ring1Rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(3200, easing = LinearEasing)), "r1")
    val ring2Rot by inf.animateFloat(360f, 0f, infiniteRepeatable(tween(2200, easing = LinearEasing)), "r2")
    val ring3Rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(5000, easing = LinearEasing)), "r3")

    val pulse by inf.animateFloat(
        0.92f, 1.08f,
        infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), "pulse"
    )
    val pulseAlpha by inf.animateFloat(
        0.8f, 0.2f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), "pa"
    )

    val logoEnter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(400)
        logoEnter.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }

    Box(
        modifier         = Modifier
            .size(180.dp)
            .graphicsLayer {
                scaleX = logoEnter.value; scaleY = logoEnter.value; alpha = logoEnter.value
            },
        contentAlignment = Alignment.Center
    ) {
        // Outermost soft green ambient bloom
        Canvas(Modifier.size(200.dp)) {
            drawCircle(
                Brush.radialGradient(
                    colors = listOf(PrimaryGreen.copy(alpha = 0.08f * pulse), Color.Transparent)
                )
            )
        }

        // Ring canvas
        Canvas(Modifier.size(180.dp)) {
            val cx = center

            // Ring 1 — light gray glass ring with green arc
            rotate(ring1Rot, cx) {
                drawCircle(
                    color  = LightGray.copy(alpha = 0.80f),
                    radius = size.minDimension * 0.46f,
                    center = cx,
                    style  = Stroke(width = 1.8f)
                )
                drawArc(
                    color      = PrimaryGreen.copy(alpha = 0.90f),
                    startAngle = -30f,
                    sweepAngle = 60f,
                    useCenter  = false,
                    topLeft    = Offset(cx.x - size.minDimension * 0.46f, cx.y - size.minDimension * 0.46f),
                    size       = Size(size.minDimension * 0.92f, size.minDimension * 0.92f),
                    style      = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
            }

            // Ring 2 — soft green glass ring
            rotate(ring2Rot, cx) {
                drawCircle(
                    color  = PrimaryGreen.copy(alpha = 0.22f),
                    radius = size.minDimension * 0.37f,
                    center = cx,
                    style  = Stroke(width = 1.4f)
                )
                drawArc(
                    color      = SoftGreen.copy(alpha = 0.85f),
                    startAngle = 120f,
                    sweepAngle = 50f,
                    useCenter  = false,
                    topLeft    = Offset(cx.x - size.minDimension * 0.37f, cx.y - size.minDimension * 0.37f),
                    size       = Size(size.minDimension * 0.74f, size.minDimension * 0.74f),
                    style      = Stroke(width = 2f, cap = StrokeCap.Round)
                )
            }

            // Ring 3 — ultra-faint green
            rotate(ring3Rot, cx) {
                drawCircle(
                    color  = PrimaryGreen.copy(alpha = 0.12f),
                    radius = size.minDimension * 0.28f,
                    center = cx,
                    style  = Stroke(width = 1f)
                )
            }

            // Pulse rings — soft green ripples
            for (i in 0..2) {
                val delay = i / 3f
                val scale = 1f + (pulseAlpha + delay).coerceIn(0f, 1f) * 0.8f
                val a     = ((1f - pulseAlpha) * (1f - delay / 1.5f)).coerceIn(0f, 1f)
                drawCircle(
                    color  = PrimaryGreen.copy(alpha = a * 0.18f),
                    radius = size.minDimension * 0.23f * scale,
                    center = cx,
                    style  = Stroke(width = 1f)
                )
            }
        }

        // Core circle — white glass with green border
        Box(
            modifier         = Modifier
                .size(82.dp)
                .background(
                    Brush.radialGradient(
                        listOf(BgPure, BgVoid)
                    ),
                    CircleShape
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(PrimaryGreen.copy(alpha = 0.50f), PrimaryGreen.copy(alpha = 0.15f))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            FitMateLogoMark(
                modifier = Modifier.size(76.dp),
            )
        }
    }
}

// ── Dumbbell Icon ─────────────────────────────────────────────────────────────

// ── Brand Text ────────────────────────────────────────────────────────────────

@Composable
private fun BrandText() {
    val alpha   = remember { Animatable(0f) }
    val offsetY = remember { Animatable(24f) }
    LaunchedEffect(Unit) {
        delay(1000)
        launch { alpha.animateTo(1f, tween(800)) }
        launch { offsetY.animateTo(0f, tween(800, easing = FastOutSlowInEasing)) }
    }

    Column(
        modifier            = Modifier.graphicsLayer { this.alpha = alpha.value; translationY = offsetY.value },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Soft green bloom layer behind text
            Text(
                text  = "FITMATE",
                style = TextStyle(
                    fontFamily    = Orbitron,
                    fontSize      = 38.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color         = PrimaryGreen.copy(alpha = 0.12f)
                )
            )
            // Shadow depth layer
            Text(
                text  = "FITMATE",
                style = TextStyle(
                    fontFamily    = Orbitron,
                    fontSize      = 38.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color         = LightGray.copy(alpha = 0.60f)
                ),
                modifier = Modifier.offset(y = 1.dp)
            )
            // Crisp dark top layer
            Text(
                text  = "FITMATE",
                style = TextStyle(
                    fontFamily    = Orbitron,
                    fontSize      = 38.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color         = DarkText
                )
            )
        }

        Text(
            text  = "AI  ·  PERFORMANCE  ·  ELITE",
            style = TextStyle(
                fontFamily    = Rajdhani,
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Light,
                letterSpacing = 5.sp,
                color         = SecondaryText.copy(alpha = 0.70f)
            )
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  10. STAT PILLS ROW
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun StatPillsRow() {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(1650)
        alpha.animateTo(1f, tween(700))
    }

    Row(
        modifier              = Modifier.graphicsLayer { this.alpha = alpha.value },
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        listOf(
            "12K+" to "Athletes",
            "99%"  to "AI Score",
            "4.9★" to "Rating"
        ).forEach { (v, l) -> StatPill(v, l) }
    }
}

@Composable
private fun StatPill(value: String, label: String) {
    Column(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    listOf(BgPure, BgVoid)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(LightGray, PrimaryGreen.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Green accent top bar
        Box(
            Modifier
                .width(24.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, PrimaryGreen.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = value,
            style = TextStyle(
                fontFamily = Orbitron,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = DarkText
            )
        )
        Text(
            text  = label,
            style = TextStyle(
                fontFamily    = Rajdhani,
                fontSize      = 8.sp,
                letterSpacing = 2.sp,
                color         = SecondaryText
            )
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  11. HOLO BARS (EQ visualizer)
// ═════════════════════════════════════════════════════════════════════════════

private val barHeights = listOf(8, 14, 10, 18, 12, 24, 16, 10, 20, 15, 9, 22, 13, 18, 8)

@Composable
private fun HoloBars() {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(2000)
        alpha.animateTo(1f, tween(800))
    }

    val inf = rememberInfiniteTransition(label = "bars")

    Row(
        modifier              = Modifier.graphicsLayer { this.alpha = alpha.value },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment     = Alignment.Bottom
    ) {
        barHeights.forEachIndexed { i, h ->
            val pulse by inf.animateFloat(
                0.4f, 1f,
                infiniteRepeatable(tween(800 + i * 60, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                "bar$i"
            )
            Box(
                Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .graphicsLayer {
                        scaleY           = pulse
                        transformOrigin  = TransformOrigin(0.5f, 1f)
                    }
                    .background(
                        Brush.verticalGradient(
                            listOf(PrimaryGreen.copy(alpha = 0.90f), PrimaryGreen.copy(alpha = 0.20f))
                        ),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  12. BOTTOM HUD
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun BottomHud(statusText: String, progress: Float) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(2200)
        alpha.animateTo(1f, tween(800))
    }

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha.value },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text  = statusText,
            style = TextStyle(
                fontFamily    = Orbitron,
                fontSize      = 9.sp,
                letterSpacing = 3.sp,
                color         = PrimaryGreen.copy(alpha = 0.75f)
            )
        )

        Spacer(Modifier.height(14.dp))

        // Main progress track — liquid glass style
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(LightGray, RoundedCornerShape(3.dp))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress)
                    .background(
                        Brush.horizontalGradient(
                            listOf(PrimaryGreen, SoftGreen, BgPure.copy(alpha = 0.90f))
                        ),
                        RoundedCornerShape(3.dp)
                    )
            )
        }

        Spacer(Modifier.height(10.dp))

        // Segmented bar (20 segments)
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val segments = 20
            for (i in 0 until segments) {
                val threshold = i.toFloat() / segments
                val filled    = progress >= threshold
                Box(
                    Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(
                            if (filled) Brush.horizontalGradient(
                                listOf(PrimaryGreen, SoftGreen.copy(alpha = 0.70f))
                            )
                            else Brush.horizontalGradient(
                                listOf(LightGray, LightGray)
                            ),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(6.dp))

         Text(
            text     = "${(progress * 100).toInt()}%",
            style    = TextStyle(
                fontFamily    = Orbitron,
                fontSize      = 9.sp,
                color         = PrimaryGreen.copy(alpha = 0.50f),
                letterSpacing = 1.sp
            ),
            modifier = Modifier.align(Alignment.End)
        )
    }
}
