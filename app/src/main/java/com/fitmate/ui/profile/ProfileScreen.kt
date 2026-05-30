package com.fitmate.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitmate.ui.components.SectionCard
import com.fitmate.ui.viewmodel.CampusFitUiState
import kotlinx.coroutines.delay

// ── Private palette — no conflicts with any theme Color.kt ────────────────────
private val P_Cyan        = Color(0xFF00E5FF)
private val P_DeepSpace   = Color(0xFF05070A)
private val P_Glass       = Color(0x0DFFFFFF)
private val P_Border      = Color(0x1FFFFFFF)
private val P_Gold        = Color(0xFFFFB300)
private val P_Green       = Color(0xFF00E676)
private val P_Surface     = Color(0xFF0D1117)
private val P_TextPrimary = Color(0xFFEEEEEE)
private val P_TextDim     = Color(0x80FFFFFF)

private fun Color.fa(alpha: Float): Color =
    Color(red = this.red, green = this.green, blue = this.blue, alpha = alpha)

// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    state: CampusFitUiState
) {
    var showDiet     by remember { mutableStateOf(false) }
    var showHabits   by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // staggered entrance
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); entered = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(P_DeepSpace)
    ) {
        ProfileGridBg()
        ProfileParticles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Avatar + headline ──────────────────────────────────────────
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(500)) + expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            ) {
                ProfileHeader(state = state)
            }

            // ── Stats row ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(500, 100))
            ) {
                StatsRow(state = state)
            }

            // ── Diet card ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(500, 180))
            ) {
                ExpandableNeonCard(
                    title = "Diet",
                    icon = "🥗",
                    accentColor = P_Green,
                    expanded = showDiet,
                    onToggle = { showDiet = !showDiet }
                ) {
                    NeonRow("Goal", state.profile.goal.label, P_Green)
                    NeonRow("Food Type", state.profile.foodPreference.label, P_Green)
                    state.personalizedPlan?.dietRecommendation?.let { diet ->
                        Spacer(Modifier.height(12.dp))
                        NeonSectionLabel(diet.title, P_Green)
                        Spacer(Modifier.height(6.dp))
                        diet.meals.forEach { meal ->
                            BulletItem(meal, P_Green)
                        }
                    }
                }
            }

            // ── Habits card ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(500, 260))
            ) {
                ExpandableNeonCard(
                    title = "Habits",
                    icon = "⚡",
                    accentColor = P_Gold,
                    expanded = showHabits,
                    onToggle = { showHabits = !showHabits }
                ) {
                    HabitCheckRow("Drink enough water", P_Gold)
                    HabitCheckRow("Complete workout", P_Gold)
                    HabitCheckRow("Hit protein goal", P_Gold)
                    state.dashboard?.disciplineState?.let { ds ->
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MiniStatChip(
                                label = "STREAK",
                                value = "${ds.streakDays}d",
                                color = P_Gold,
                                modifier = Modifier.weight(1f)
                            )
                            MiniStatChip(
                                label = "POINTS",
                                value = "${ds.rewardPoints}",
                                color = P_Cyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── Settings card ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(500, 340))
            ) {
                ExpandableNeonCard(
                    title = "Settings",
                    icon = "⚙",
                    accentColor = P_Cyan,
                    expanded = showSettings,
                    onToggle = { showSettings = !showSettings }
                ) {
                    NeonRow("AI Provider", state.aiConfig.providerMode.label, P_Cyan)
                    NeonRow("Theme",       state.themeMode.label,             P_Cyan)
                    NeonRow("Model",       state.aiConfig.modelName,          P_Cyan)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Profile header with pulsing avatar ring ────────────────────────────────────
@Composable
private fun ProfileHeader(state: CampusFitUiState) {
    val pulse = rememberInfiniteTransition(label = "avatar_pulse")
    val ringAlpha by pulse.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring"
    )
    val glowRadius by pulse.animateFloat(
        initialValue = 18f, targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(P_Glass)
            .border(1.dp, P_Border, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(72.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = P_Cyan.fa(0.12f),
                    radius = glowRadius.dp.toPx()
                )
                drawCircle(
                    color = P_Cyan.fa(ringAlpha),
                    radius = size.minDimension / 2f,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(P_Cyan.fa(0.2f), P_Cyan.fa(0.05f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 28.sp)
            }
        }

        // Name + stats
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "ATHLETE",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = P_Cyan.fa(0.7f)
            )
            Text(
                text = state.profile.goal.label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = P_TextPrimary
            )
            Text(
                text = "${state.profile.activityLevel.label} • ${state.profile.foodPreference.label}",
                fontSize = 12.sp,
                color = P_TextDim
            )
        }
    }
}

// ── Compact stats row ──────────────────────────────────────────────────────────
@Composable
private fun StatsRow(state: CampusFitUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatPill(label = "AGE",    value = "${state.profile.age}y",               color = P_Cyan,  modifier = Modifier.weight(1f))
        StatPill(label = "HEIGHT", value = "${state.profile.heightCm}cm",         color = P_Green, modifier = Modifier.weight(1f))
        StatPill(label = "WEIGHT", value = "${state.profile.weightKg}kg",         color = P_Gold,  modifier = Modifier.weight(1f))
        StatPill(label = "WORKOUT",value = "${state.profile.workoutMinutes}m",    color = P_Cyan,  modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val glow = rememberInfiniteTransition(label = "pill_$label")
    val borderAlpha by glow.animateFloat(
        initialValue = 0.15f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pill_border"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.fa(0.06f))
            .border(1.dp, color.fa(borderAlpha), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = color.fa(0.6f)
        )
    }
}

// ── Expandable neon card ───────────────────────────────────────────────────────
@Composable
private fun ExpandableNeonCard(
    title: String,
    icon: String,
    accentColor: Color,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (expanded) accentColor.fa(0.4f) else P_Border,
        animationSpec = tween(300),
        label = "card_border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (expanded) accentColor.fa(0.06f) else P_Glass,
        animationSpec = tween(300),
        label = "card_bg"
    )
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "chevron"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
    ) {
        // Header row — tap to toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon bubble
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.fa(0.12f))
                    .border(1.dp, accentColor.fa(0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp)
            }

            Text(
                text = title.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = accentColor,
                modifier = Modifier.weight(1f)
            )

            // Chevron
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.fa(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                // Draw a simple chevron via Canvas so no Icon import needed
                Canvas(modifier = Modifier.size(14.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val arm = size.width * 0.35f
                    val drop = size.height * 0.2f
                    // rotate by chevronRotation around center
                    val rad = Math.toRadians(chevronRotation.toDouble()).toFloat()
                    val cos = kotlin.math.cos(rad)
                    val sin = kotlin.math.sin(rad)
                    fun rot(x: Float, y: Float): Offset {
                        val rx = x - cx; val ry = y - cy
                        return Offset(cx + rx * cos - ry * sin, cy + rx * sin + ry * cos)
                    }
                    val p1 = rot(cx - arm, cy - drop)
                    val p2 = rot(cx,       cy + drop)
                    val p3 = rot(cx + arm, cy - drop)
                    drawLine(accentColor, p1, p2, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(accentColor, p2, p3, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                }
            }
        }

        // Neon separator line
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 20.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, accentColor.fa(0.4f), Color.Transparent)
                        )
                    )
            )
        }

        // Expandable content
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(250)) + expandVertically(tween(300, easing = FastOutSlowInEasing)),
            exit  = fadeOut(tween(200)) + shrinkVertically(tween(250))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        }
    }
}

// ── Key-value row ──────────────────────────────────────────────────────────────
@Composable
private fun NeonRow(label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(accent.fa(0.04f))
            .border(1.dp, accent.fa(0.1f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = accent.fa(0.65f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = P_TextPrimary,
            textAlign = TextAlign.End
        )
    }
}

// ── Section label inside card ──────────────────────────────────────────────────
@Composable
private fun NeonSectionLabel(text: String, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = P_TextPrimary
        )
    }
}

// ── Bullet item ────────────────────────────────────────────────────────────────
@Composable
private fun BulletItem(text: String, accent: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(accent.fa(0.7f))
        )
        Text(text = text, fontSize = 13.sp, color = P_TextDim, lineHeight = 18.sp)
    }
}

// ── Habit check row ────────────────────────────────────────────────────────────
@Composable
private fun HabitCheckRow(text: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(accent.fa(0.04f))
            .border(1.dp, accent.fa(0.1f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Check circle
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(accent.fa(0.15f))
                .border(1.dp, accent.fa(0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", fontSize = 11.sp, color = accent)
        }
        Text(
            text = text,
            fontSize = 13.sp,
            color = P_TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Mini stat chip (streak / points) ──────────────────────────────────────────
@Composable
private fun MiniStatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.fa(0.08f))
            .border(1.dp, color.fa(0.25f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = color.fa(0.6f)
        )
    }
}

// ── Ambient grid background ────────────────────────────────────────────────────
@Composable
private fun ProfileGridBg() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 52.dp.toPx()
        val c = P_Cyan.fa(0.025f)
        var x = 0f
        while (x < size.width)  { drawLine(c, Offset(x,0f), Offset(x,size.height), 1f); x += step }
        var y = 0f
        while (y < size.height) { drawLine(c, Offset(0f,y), Offset(size.width,y),  1f); y += step }
        // Subtle radial glow top-center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(P_Cyan.fa(0.04f), Color.Transparent),
                center = Offset(size.width / 2f, 0f),
                radius = size.width * 0.7f
            ),
            radius = size.width * 0.7f,
            center = Offset(size.width / 2f, 0f)
        )
    }
}

// ── Floating particles ─────────────────────────────────────────────────────────
@Composable
private fun ProfileParticles() {
    data class Dot(val x: Float, val y: Float, val r: Float, val a: Float)
    val dots = remember {
        List(30) {
            Dot(
                x = (0..1000).random() / 1000f,
                y = (0..1000).random() / 1000f,
                r = (2..10).random() / 10f,
                a = (1..3).random() / 10f
            )
        }
    }
    val inf = rememberInfiniteTransition(label = "pdrift")
    val t by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "pdrift_t"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        dots.forEach { d ->
            val px = (d.x * size.width  + t * 60f) % size.width
            val py = (d.y * size.height + t * 30f) % size.height
            drawCircle(P_Cyan.fa(d.a), radius = d.r.dp.toPx(), center = Offset(px, py))
        }
    }
}