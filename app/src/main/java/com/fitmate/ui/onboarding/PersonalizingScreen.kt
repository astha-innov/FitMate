package com.fitmate.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitmate.R
import com.fitmate.ui.components.FitMateLogoMark
import com.fitmate.ui.viewmodel.PersonalizationState
import kotlinx.coroutines.delay

// ── All colors are private to this file to avoid conflicts with any
//    NeonCyan / DeepSpace / etc. already declared in your theme/Colors.kt ──────
private val _NeonCyan      = Color(0xFF10B981)
private val _DeepSpace     = Color(0xFFFFFFFF)
private val _GlassWhite    = Color(0xFFF8FAFC)
private val _SurfaceBorder = Color(0xFFE5E7EB)
private val _ErrorRed      = Color(0xFFEF4444)

// ── Safe alpha helper — avoids any AnimationVector .copy() internal issue ─────
private fun Color.a(alpha: Float): Color =
    Color(red = this.red, green = this.green, blue = this.blue, alpha = alpha)

private enum class StepStatus { DONE, ACTIVE, PENDING }
private data class OnboardingStep(val label: String, val status: StepStatus)

@Composable
fun PersonalizingScreen(
    state: PersonalizationState,
    onBackToSetup: () -> Unit
) {
    val progress = state.progress.coerceIn(0f, 1f)

    val goalsLabel = stringResource(R.string.step_goals_targets)
    val dietLabel = stringResource(R.string.step_diet_nutrition)
    val workoutLabel = stringResource(R.string.step_workout_memory)
    val aiLabel = stringResource(R.string.step_ai_calibration)

    val steps = remember(progress, goalsLabel, dietLabel, workoutLabel, aiLabel) {
        listOf(
            OnboardingStep(goalsLabel, when {
                progress > 0.25f -> StepStatus.DONE
                progress > 0f    -> StepStatus.ACTIVE
                else             -> StepStatus.PENDING
            }),
            OnboardingStep(dietLabel, when {
                progress > 0.50f -> StepStatus.DONE
                progress > 0.25f -> StepStatus.ACTIVE
                else             -> StepStatus.PENDING
            }),
            OnboardingStep(workoutLabel, when {
                progress > 0.75f -> StepStatus.DONE
                progress > 0.50f -> StepStatus.ACTIVE
                else             -> StepStatus.PENDING
            }),
            OnboardingStep(aiLabel, when {
                progress >= 1f   -> StepStatus.DONE
                progress > 0.75f -> StepStatus.ACTIVE
                else             -> StepStatus.PENDING
            }),
        )
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(_DeepSpace),
        contentAlignment = Alignment.Center
    ) {
        GridBackground()
        ParticleField()

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(_GlassWhite)
                    .border(1.dp, _SurfaceBorder, RoundedCornerShape(32.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PulsingLogo()
                    Spacer(Modifier.height(20.dp))
                    ShimmerTitle()
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = state.status.ifBlank {
                            stringResource(R.string.personalizing_default_status)
                        },
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.widthIn(max = 260.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    NeonProgressBar(progress = progress)
                    Spacer(Modifier.height(22.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        steps.forEachIndexed { idx, step ->
                            StepRow(step = step, animDelay = idx * 80)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    NeonDivider()
                    if (state.error != null) {
                        Spacer(Modifier.height(16.dp))
                        ErrorBox(message = state.error, onBackToSetup = onBackToSetup)
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingLogo() {
    val pulse = rememberInfiniteTransition(label = "logo_pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "logo_scale"
    )
    val ringAlpha by pulse.animateFloat(
        initialValue = 0.25f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring_alpha"
    )
    val ring2Alpha by pulse.animateFloat(
        initialValue = 0.08f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(1400, 400, FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring2_alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
        Box(
            modifier = Modifier
                .size(126.dp)
                .clip(CircleShape)
                .border(1.dp, _NeonCyan.a(ring2Alpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(114.dp)
                .clip(CircleShape)
                .border(1.5.dp, _NeonCyan.a(ringAlpha), CircleShape)
        )
        Canvas(modifier = Modifier.size(130.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(_NeonCyan.a(0.18f), Color.Transparent),
                    center = center,
                    radius = size.minDimension / 2f
                )
            )
        }
        FitMateLogoMark(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .padding(22.dp),
        )
    }
}

@Composable
private fun ShimmerTitle() {
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val alphaAnim by shimmer.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shimmer_alpha"
    )
    Text(
        text = stringResource(R.string.personalising),
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 3.sp,
        color = Color(0xFF111827).a(alphaAnim)
    )
}

@Composable
private fun NeonProgressBar(progress: Float) {
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "prog"
    )
    val dotPulse = rememberInfiniteTransition(label = "dot_pulse")
    val dotGlow by dotPulse.animateFloat(
        initialValue = 4f, targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot_glow"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.progress_label),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = _NeonCyan.a(0.7f)
            )
            Text(
                text = stringResource(R.string.progress_percentage, (animProgress * 100).toInt()),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = _NeonCyan
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFFE5E7EB))
                    .border(1.dp, _NeonCyan.a(0.1f), RoundedCornerShape(99.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animProgress)
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF10B981), _NeonCyan, Color(0xFF34D399))
                        )
                    )
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotX = size.width * animProgress
                if (dotX > 6.dp.toPx()) {
                    drawCircle(
                        color = _NeonCyan.a(0.25f),
                        radius = dotGlow.dp.toPx(),
                        center = Offset(dotX, size.height / 2f)
                    )
                    drawCircle(
                        color = _NeonCyan,
                        radius = 5.dp.toPx(),
                        center = Offset(dotX, size.height / 2f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(dotX, size.height / 2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepRow(step: OnboardingStep, animDelay: Int) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(animDelay.toLong()); appeared = true }

    val bgColor by animateColorAsState(
        targetValue = when (step.status) {
            StepStatus.DONE    -> _NeonCyan.a(0.05f)
            StepStatus.ACTIVE  -> _NeonCyan.a(0.08f)
            StepStatus.PENDING -> Color.Transparent
        },
        label = "step_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = when (step.status) {
            StepStatus.DONE    -> _NeonCyan.a(0.18f)
            StepStatus.ACTIVE  -> _NeonCyan.a(0.35f)
            StepStatus.PENDING -> Color(0xFFE5E7EB)
        },
        label = "step_border"
    )
    val textColor by animateColorAsState(
        targetValue = when (step.status) {
            StepStatus.DONE    -> _NeonCyan.a(0.75f)
            StepStatus.ACTIVE  -> _NeonCyan
            StepStatus.PENDING -> Color(0xFF6B7280)
        },
        label = "step_text"
    )

    AnimatedVisibility(
        visible = appeared,
        enter = fadeIn(tween(350))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepIcon(status = step.status)
            Text(
                text = step.label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.9.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            if (step.status == StepStatus.ACTIVE) {
                SpinnerIcon()
            }
        }
    }
}

@Composable
private fun StepIcon(status: StepStatus) {
    val pulse = rememberInfiniteTransition(label = "icon_pulse")
    val glowRadius by pulse.animateFloat(
        initialValue = 6f, targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "icon_glow"
    )

    val bgColor = when (status) {
        StepStatus.DONE, StepStatus.ACTIVE -> _NeonCyan.a(0.12f)
        StepStatus.PENDING                 -> Color(0xFFE5E7EB)
    }
    val borderColor = when (status) {
        StepStatus.DONE    -> _NeonCyan.a(0.4f)
        StepStatus.ACTIVE  -> _NeonCyan
        StepStatus.PENDING -> Color(0xFFD1D5DB)
    }
    val iconText = when (status) {
        StepStatus.DONE    -> stringResource(R.string.step_done)
        StepStatus.ACTIVE  -> stringResource(R.string.step_active)
        StepStatus.PENDING -> stringResource(R.string.step_pending)
    }
    val iconColor = when (status) {
        StepStatus.DONE, StepStatus.ACTIVE -> _NeonCyan
        StepStatus.PENDING                 -> Color(0xFF9CA3AF)
    }

    val glowModifier = if (status == StepStatus.ACTIVE) {
        Modifier.drawBehind {
            drawCircle(_NeonCyan.a(0.2f), radius = glowRadius.dp.toPx())
        }
    } else Modifier

    Box(
        modifier = Modifier
            .size(28.dp)
            .then(glowModifier)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = iconText, fontSize = 12.sp, color = iconColor)
    }
}

@Composable
private fun SpinnerIcon() {
    val rotation = rememberInfiniteTransition(label = "spin")
    val angle by rotation.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
        label = "angle"
    )
    Canvas(modifier = Modifier.size(16.dp)) {
        rotate(angle) {
            drawArc(
                color = _NeonCyan,
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
                size = Size(size.width - 2.dp.toPx(), size.height - 2.dp.toPx())
            )
        }
    }
}

@Composable
private fun NeonDivider() {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, _NeonCyan.a(0.5f), Color.Transparent)
                )
            )
    )
}

@Composable
private fun ErrorBox(message: String, onBackToSetup: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(_ErrorRed.a(0.08f))
            .border(1.dp, _ErrorRed.a(0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = message,
            color = _ErrorRed,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        TextButton(
            onClick = onBackToSetup,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(_ErrorRed.a(0.12f))
                .border(1.dp, _ErrorRed.a(0.3f), RoundedCornerShape(12.dp))
        ) {
            Text(
                text = stringResource(R.string.back_to_setup),
                color = _ErrorRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun GridBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 48.dp.toPx()
        val lineColor = _NeonCyan.a(0.03f)
        var x = 0f
        while (x < size.width) {
            drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
    }
}

@Composable
private fun ParticleField() {
    data class Particle(val x: Float, val y: Float, val r: Float, val alpha: Float)
    val particles = remember {
        List(40) {
            Particle(
                x = (0..1000).random() / 1000f,
                y = (0..1000).random() / 1000f,
                r = (3..12).random() / 10f,
                alpha = (1..4).random() / 10f
            )
        }
    }
    val drift = rememberInfiniteTransition(label = "drift")
    val offset by drift.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "drift_val"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val px = (p.x * size.width + offset * 80) % size.width
            val py = (p.y * size.height + offset * 40) % size.height
            drawCircle(_NeonCyan.a(p.alpha), radius = p.r.dp.toPx(), center = Offset(px, py))
        }
    }
}