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
import kotlin.math.pow
import kotlin.random.Random

// ═════════════════════════════════════════════════════════════════════════════
//  DESIGN TOKENS — Deep Space · Neon Cyan · Chrome White
// ═════════════════════════════════════════════════════════════════════════════

private val BgVoid       = Color(0xFF05070A)          // DeepSpace background
private val NeonCyan     = Color(0xFF00E5FF)           // primary accent
private val SoftCyan     = Color(0xFF55F0FF)           // lighter cyan
private val Chrome       = Color(0xFFFFFFFF)           // full white
private val ChromeDim    = Color(0x66FFFFFF)           // 40% white
private val GlassWhite   = Color(0xFFFFFFFF).copy(alpha = 0.05f)  // glass fill
private val SurfaceBorder= Color(0xFFFFFFFF).copy(alpha = 0.12f)  // border
private val ChromeGhost  = Color(0x08FFFFFF)           // 5% white
private val CyanGlow     = Color(0x4400E5FF)           // cyan glow
private val CyanFaint    = Color(0x1800E5FF)           // very faint cyan

private val Orbitron  = FontFamily.Monospace
private val Rajdhani  = FontFamily.SansSerif

// ═════════════════════════════════════════════════════════════════════════════
//  PARTICLE DATA MODEL
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
    baseAlpha = Random.nextFloat() * 0.5f + 0.1f,
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
            "INITIALIZING CORE...",
            "LOADING BIOMETRICS...",
            "SYNCING AI ENGINE...",
            "CALIBRATING NEURAL LINK...",
            "SYSTEM READY."
        )
    }
    var statusText by remember { mutableStateOf(statuses[0]) }
    val progress   = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            progress.animateTo(
                targetValue    = 1f,
                animationSpec  = tween(durationMillis = 5200, easing = LinearEasing)
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
            modifier                = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment     = Alignment.CenterHorizontally,
            verticalArrangement     = Arrangement.SpaceBetween
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
        initialValue   = 0f,
        targetValue    = 80f,
        animationSpec  = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label          = "gridScroll"
    )

    Canvas(Modifier.fillMaxSize()) {
        val cx      = size.width / 2f
        val horizon = size.height * 0.28f
        val gridCol = NeonCyan.copy(alpha = 0.06f)

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
                color       = NeonCyan.copy(alpha = t.pow(1.2f) * 0.07f),
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
            val color = if (p.isWhite) Chrome.copy(alpha = a) else NeonCyan.copy(alpha = a * 0.8f)
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

        // Central cyan nebula
        drawCircle(
            brush  = Brush.radialGradient(
                colors   = listOf(NeonCyan.copy(alpha = 0.12f), Color.Transparent),
                center   = ctr,
                radius   = size.width * 0.85f
            ),
            radius = size.width * 0.85f,
            center = ctr
        )

        // Top-left chrome glint
        val topL = Offset(size.width * 0.18f, size.height * 0.12f)
        drawCircle(
            brush  = Brush.radialGradient(
                colors   = listOf(Chrome.copy(alpha = 0.05f), Color.Transparent),
                center   = topL,
                radius   = 280f
            ),
            radius = 280f,
            center = topL
        )

        // Bottom-right cyan glint
        val botR = Offset(size.width * 0.85f, size.height * 0.82f)
        drawCircle(
            brush  = Brush.radialGradient(
                colors   = listOf(SoftCyan.copy(alpha = 0.1f), Color.Transparent),
                center   = botR,
                radius   = 220f
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

        // Outer cyan glow
        drawPath(ecgPath, NeonCyan.copy(alpha = 0.08f),  style = Stroke(width = 6f,   cap = StrokeCap.Round))
        // Mid glow
        drawPath(ecgPath, NeonCyan.copy(alpha = 0.35f),  style = Stroke(width = 2f,   cap = StrokeCap.Round))
        // White highlight
        drawPath(ecgPath, Chrome.copy(alpha = 0.25f),    style = Stroke(width = 0.8f, cap = StrokeCap.Round))
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
            brush     = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    NeonCyan.copy(alpha = 0.06f),
                    NeonCyan.copy(alpha = 0.14f),
                    NeonCyan.copy(alpha = 0.06f),
                    Color.Transparent
                )
            ),
            topLeft   = Offset(0f, y - 1f),
            size      = Size(size.width, 2f)
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
        val col = NeonCyan.copy(alpha = 0.50f)
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
        HoloStatCard("98.2",  "VO₂ MAX",     Modifier.padding(start = 18.dp, top = 178.dp).offset(y = floatY(3800, false)))
        HoloStatCard("72 BPM","HEART RATE",  Modifier.align(Alignment.TopEnd).padding(top = 236.dp, end = 18.dp).offset(y = floatY(4600, true)))
        HoloStatCard("2,840", "KCAL",        Modifier.padding(start = 22.dp, top = 328.dp).offset(y = floatY(3200, false)))
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
            .background(GlassWhite, RoundedCornerShape(8.dp))
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    listOf(NeonCyan.copy(alpha = 0.45f), NeonCyan.copy(alpha = 0.10f))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Active indicator dot
        Box(
            Modifier
                .size(4.dp)
                .background(NeonCyan, CircleShape)
        )
        Text(
            text  = value,
            style = TextStyle(
                fontFamily = Orbitron,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = Chrome
            )
        )
        Text(
            text  = label,
            style = TextStyle(
                fontFamily    = Rajdhani,
                fontSize      = 8.sp,
                letterSpacing = 2.sp,
                color         = ChromeDim
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
        modifier            = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha.value },
        verticalAlignment   = Alignment.CenterVertically
    ) {
        Text(
            "v2.4",
            style = TextStyle(fontFamily = Orbitron, fontSize = 9.sp, letterSpacing = 3.sp, color = ChromeDim)
        )
        Box(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, NeonCyan.copy(alpha = 0.30f), Color.Transparent)
                    )
                )
        )
        Text(
            "AI ENGINE",
            style = TextStyle(fontFamily = Orbitron, fontSize = 9.sp, letterSpacing = 3.sp, color = ChromeDim)
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
        modifier        = Modifier
            .size(180.dp)
            .graphicsLayer { scaleX = logoEnter.value; scaleY = logoEnter.value; alpha = logoEnter.value },
        contentAlignment = Alignment.Center
    ) {
        // Outermost ambient glow
        Canvas(Modifier.size(200.dp)) {
            drawCircle(
                Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.15f * pulse), Color.Transparent)
                )
            )
        }

        // Ring canvas
        Canvas(Modifier.size(180.dp)) {
            val cx = center

            // Ring 1 — chrome white
            rotate(ring1Rot, cx) {
                drawCircle(
                    color  = Chrome.copy(alpha = 0.70f),
                    radius = size.minDimension * 0.46f,
                    center = cx,
                    style  = Stroke(width = 1.8f)
                )
                drawArc(
                    color       = Chrome,
                    startAngle  = -30f,
                    sweepAngle  = 60f,
                    useCenter   = false,
                    topLeft     = Offset(cx.x - size.minDimension * 0.46f, cx.y - size.minDimension * 0.46f),
                    size        = Size(size.minDimension * 0.92f, size.minDimension * 0.92f),
                    style       = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
            }

            // Ring 2 — neon cyan
            rotate(ring2Rot, cx) {
                drawCircle(
                    color  = NeonCyan.copy(alpha = 0.65f),
                    radius = size.minDimension * 0.37f,
                    center = cx,
                    style  = Stroke(width = 1.4f)
                )
                drawArc(
                    color       = SoftCyan,
                    startAngle  = 120f,
                    sweepAngle  = 50f,
                    useCenter   = false,
                    topLeft     = Offset(cx.x - size.minDimension * 0.37f, cx.y - size.minDimension * 0.37f),
                    size        = Size(size.minDimension * 0.74f, size.minDimension * 0.74f),
                    style       = Stroke(width = 2f, cap = StrokeCap.Round)
                )
            }

            // Ring 3 — faint cyan
            rotate(ring3Rot, cx) {
                drawCircle(
                    color  = NeonCyan.copy(alpha = 0.20f),
                    radius = size.minDimension * 0.28f,
                    center = cx,
                    style  = Stroke(width = 1f)
                )
            }

            // Pulse rings
            for (i in 0..2) {
                val delay   = i / 3f
                val scale   = 1f + (pulseAlpha + delay).coerceIn(0f, 1f) * 0.8f
                val a       = ((1f - pulseAlpha) * (1f - delay / 1.5f)).coerceIn(0f, 1f)
                drawCircle(
                    color  = NeonCyan.copy(alpha = a * 0.35f),
                    radius = size.minDimension * 0.23f * scale,
                    center = cx,
                    style  = Stroke(width = 1f)
                )
            }
        }

        // Core circle
        Box(
            modifier        = Modifier
                .size(82.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF040810), Color(0xFF05070A))
                    ),
                    CircleShape
                )
                .border(
                    width = 0.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(NeonCyan.copy(alpha = 0.40f), NeonCyan.copy(alpha = 0.10f))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            DumbbellIcon(Modifier.size(44.dp))
        }
    }
}

// ── Dumbbell Icon ─────────────────────────────────────────────────────────────

@Composable
private fun DumbbellIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w  = size.width
        val h  = size.height
        val cx = w / 2f
        val cy = h / 2f

        val barColor  = Chrome
        val plateCol  = Chrome.copy(alpha = 0.9f)
        val claspCol  = NeonCyan.copy(alpha = 0.85f)

        drawLine(barColor, Offset(w * 0.15f, cy), Offset(w * 0.85f, cy), strokeWidth = h * 0.08f, cap = StrokeCap.Butt)

        drawRoundRect(
            color  = plateCol,
            topLeft = Offset(0f, cy - h * 0.28f),
            size   = Size(w * 0.14f, h * 0.56f),
            cornerRadius = CornerRadius(3f)
        )
        drawRoundRect(
            color   = claspCol,
            topLeft = Offset(w * 0.12f, cy - h * 0.2f),
            size    = Size(w * 0.07f, h * 0.4f),
            cornerRadius = CornerRadius(2f)
        )

        drawRoundRect(
            color   = claspCol,
            topLeft = Offset(w * 0.81f, cy - h * 0.2f),
            size    = Size(w * 0.07f, h * 0.4f),
            cornerRadius = CornerRadius(2f)
        )
        drawRoundRect(
            color   = plateCol,
            topLeft = Offset(w * 0.86f, cy - h * 0.28f),
            size    = Size(w * 0.14f, h * 0.56f),
            cornerRadius = CornerRadius(3f)
        )

        // Cyan arc above bar (AI motif)
        drawArc(
            color      = NeonCyan.copy(alpha = 0.60f),
            startAngle = 200f,
            sweepAngle = -40f,
            useCenter  = false,
            topLeft    = Offset(w * 0.3f, cy - h * 0.32f),
            size       = Size(w * 0.4f, h * 0.4f),
            style      = Stroke(width = h * 0.06f, cap = StrokeCap.Round)
        )
    }
}

// ── Brand Text ────────────────────────────────────────────────────────────────

@Composable
private fun BrandText() {
    val alpha = remember { Animatable(0f) }
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
            // Outer cyan bloom
            Text(
                text  = "FITMATE",
                style = TextStyle(
                    fontFamily    = Orbitron,
                    fontSize      = 38.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color         = NeonCyan.copy(alpha = 0.20f)
                )
            )
            // Mid chrome glow
            Text(
                text  = "FITMATE",
                style = TextStyle(
                    fontFamily    = Orbitron,
                    fontSize      = 38.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color         = Chrome.copy(alpha = 0.30f)
                ),
                modifier = Modifier.offset(y = 1.dp)
            )
            // Crisp top layer
            Text(
                text  = "FITMATE",
                style = TextStyle(
                    fontFamily    = Orbitron,
                    fontSize      = 38.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color         = Chrome
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
                color         = ChromeDim
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
        modifier            = Modifier.graphicsLayer { this.alpha = alpha.value },
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
            .background(GlassWhite, RoundedCornerShape(8.dp))
            .border(
                0.5.dp,
                Brush.verticalGradient(
                    listOf(SurfaceBorder, NeonCyan.copy(alpha = 0.10f))
                ),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment  = Alignment.CenterHorizontally,
        verticalArrangement  = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            Modifier
                .width(24.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, NeonCyan.copy(alpha = 0.50f), Color.Transparent)
                    )
                )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = value,
            style = TextStyle(fontFamily = Orbitron, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Chrome)
        )
        Text(
            text  = label,
            style = TextStyle(fontFamily = Rajdhani, fontSize = 8.sp, letterSpacing = 2.sp, color = ChromeDim)
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
                    .graphicsLayer { scaleY = pulse; transformOrigin = TransformOrigin(0.5f, 1f) }
                    .background(
                        Brush.verticalGradient(
                            listOf(NeonCyan.copy(alpha = 0.95f), NeonCyan.copy(alpha = 0.25f))
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
                color         = NeonCyan.copy(alpha = 0.70f)
            )
        )

        Spacer(Modifier.height(14.dp))

        // Main progress track
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(GlassWhite, RoundedCornerShape(2.dp))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress)
                    .background(
                        Brush.horizontalGradient(listOf(NeonCyan, SoftCyan, Chrome)),
                        RoundedCornerShape(2.dp)
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
                            if (filled) Brush.horizontalGradient(listOf(NeonCyan, Chrome.copy(alpha = 0.7f)))
                            else        Brush.horizontalGradient(listOf(GlassWhite, GlassWhite)),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text  = "${(progress * 100).toInt()}%",
            style = TextStyle(
                fontFamily    = Orbitron,
                fontSize      = 9.sp,
                color         = NeonCyan.copy(alpha = 0.40f),
                letterSpacing = 1.sp
            ),
            modifier = Modifier.align(Alignment.End)
        )
    }
}