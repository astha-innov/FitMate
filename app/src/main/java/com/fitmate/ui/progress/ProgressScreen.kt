package com.fitmate.ui.progress

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitmate.domain.model.WeeklyWorkoutSchedule
import com.fitmate.domain.model.WorkoutDayLog
import com.fitmate.domain.model.WorkoutDayStatus
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.model.WorkoutWeekday
import com.fitmate.ui.viewmodel.CampusFitUiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

// --- Premium Design Theme Colors ---
private val ProgressBg = Color(0xFF0B0D12)
private val ProgressCard = Color(0xFF151922)
private val ProgressGlassBg = Color(0xFF1A1F2B).copy(alpha = 0.6f)
private val ProgressBorder = Color(0xFFFFFFFF).copy(alpha = 0.08f)
private val ProgressTextPrimary = Color(0xFFF5F7FA)
private val ProgressTextSecondary = Color(0xFF8B95A5)

private val ProgressGreen = Color(0xFF1DE9B6)
private val ProgressBlue = Color(0xFF4FC3F7)
private val ProgressOrange = Color(0xFFFFB74D)
private val ProgressRed = Color(0xFFFF6B6B)
private val ProgressPurple = Color(0xFFB388FF)
private val ProgressNeutral = Color(0xFF2B3240)

@Composable
fun ProgressScreen(
    state: CampusFitUiState,
    onOpenCoach: () -> Unit,
    modifier: Modifier = Modifier
){
    val today = remember { LocalDate.now() }
    var visibleMonth by rememberSaveable { mutableStateOf(YearMonth.now()) }
    val scrollState = rememberScrollState()

    val workoutSchedule = state.workoutSchedule
    val workoutLogs = state.workoutLogs

    val analytics = state.analytics

    val hasHistory = workoutLogs.isNotEmpty()

    // Core Calculations derived from state
    val streakDays = remember(workoutSchedule, workoutLogs, today) {
        calculateWorkoutStreak(workoutSchedule, workoutLogs, today)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ProgressBg)
    ) {
        if (!hasHistory) {
            EmptyStateView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ScreenHeader()
                AnalyticsHeroCard(
                    streak = streakDays,
                    consistencyScore = analytics.completionRate,
                    totalWorkouts = analytics.totalWorkouts
                )
                AiCoachCard(
                    completionRate = analytics.completionRate,
                    streak = analytics.longestStreak,
                    totalWorkouts = analytics.totalWorkouts,
                    onClick = onOpenCoach
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FitnessScoreCard(
                        score = analytics.fitnessScore,
                        modifier = Modifier.weight(1f)
                    )

                    CompletionRingCard(
                        completed = analytics.completedSets,
                        missed = (analytics.totalSets - analytics.completedSets)
                            .coerceAtLeast(0),
                        modifier = Modifier.weight(1f)
                    )
                }

                WeeklyActivityChart(
                    activity = analytics.weeklyActivity
                )

                MonthlySummaryCard(
                    totalWorkouts = analytics.totalWorkouts,
                    longestStreak = analytics.longestStreak,
                    completionRate = analytics.completionRate,
                    completedSets = analytics.completedSets
                )

                PersonalRecordsCard(
                    totalWorkouts = analytics.totalWorkouts,
                    longestStreak = analytics.longestStreak,
                    bestWeek = analytics.weeklyActivity.maxOrNull() ?: 0
                )

                WorkoutHeatmap(
                    workoutLogs = workoutLogs
                )

                EnhancedCalendarCard(
                    visibleMonth = visibleMonth,
                    today = today,
                    workoutSchedule = workoutSchedule,
                    workoutLogs = workoutLogs,
                    onPreviousMonth = { visibleMonth = visibleMonth.minusMonths(1) },
                    onNextMonth = { visibleMonth = visibleMonth.plusMonths(1) }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// --- Subcomponents ---

@Composable
private fun ScreenHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Analytics",
            color = ProgressTextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )
        IconButton(
            onClick = { /* Handle share context */ },
            modifier = Modifier
                .background(ProgressCard, CircleShape)
                .border(1.dp, ProgressBorder, CircleShape)
        ) {
            Icon(Icons.Rounded.IosShare, contentDescription = "Share Analytics", tint = ProgressTextPrimary)
        }
    }
}

@Composable
private fun AnalyticsHeroCard(
    streak: Int,
    consistencyScore: Int,
    totalWorkouts: Int
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF232A3B), ProgressCard),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
                .border(1.dp, ProgressBorder, RoundedCornerShape(32.dp))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = ProgressOrange,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (streak == 1) "1 Day Streak" else "$streak Day Streak",
                                color = ProgressTextPrimary,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text =
                                "Good Evening\n\n" +
                                        "$totalWorkouts workouts completed\n" +
                                        "$streak day streak\n\n" +
                                        "AI Insight:\n" +
                                        if (consistencyScore >= 80)
                                            "You are ahead of last week's pace."
                                        else
                                            "Consistency can be improved this week.",
                            color = ProgressGreen,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp, start = 36.dp)
                        )
                    }
                }

                HorizontalDivider(color = ProgressBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Consistency Score",
                            color = ProgressTextSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$consistencyScore%",
                            color = ProgressTextPrimary,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Canvas(modifier = Modifier.size(80.dp, 30.dp)) {
                        val path = Path()
                        path.moveTo(0f, size.height)
                        path.cubicTo(size.width * 0.2f, size.height * 0.8f, size.width * 0.4f, size.height * 0.9f, size.width * 0.6f, size.height * 0.4f)
                        path.cubicTo(size.width * 0.8f, 0f, size.width * 0.9f, size.height * 0.2f, size.width, 0f)
                        drawPath(
                            path = path,
                            color = ProgressGreen,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiCoachCard(
    completionRate: Int,
    streak: Int,
    totalWorkouts: Int,
    onClick: () -> Unit
) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .clip(RoundedCornerShape(24.dp))
                .background(ProgressGlassBg)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            ProgressPurple.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ProgressPurple.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, ProgressPurple.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = "AI Assistant", tint = ProgressPurple)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "AI Coach",
                    color = ProgressPurple,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                val insight = when {
                    completionRate >= 80 ->
                        "🔥 Excellent consistency. Keep pushing."

                    completionRate >= 60 ->
                        "💪 Good progress. One extra workout this week could boost results."

                    else ->
                        "⚠️ Workout consistency is dropping. Try completing your scheduled sessions."
                }

                Text(
                    text = insight,
                    color = ProgressTextPrimary.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                Text(
                    text = "Current streak: $streak days • Total workouts: $totalWorkouts",
                    color = ProgressTextSecondary
                )
            }
        }
    }
}

@Composable
private fun FitnessScoreCard(score: Int, modifier: Modifier = Modifier) {
    val animatedScore by animateIntAsState(targetValue = score, animationSpec = tween(1500), label = "scoreAnimation")

    PremiumBaseCard(modifier = modifier, height = 180.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Fitness Score", color = ProgressTextSecondary, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(ProgressBlue.copy(alpha = 0.2f), CircleShape)
                        .blur(20.dp)
                )
                CircularProgressIndicator(
                    progress = { animatedScore / 100f },
                    modifier = Modifier.size(90.dp),
                    color = ProgressBlue,
                    trackColor = ProgressBorder,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "$animatedScore",
                    color = ProgressTextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun CompletionRingCard(completed: Int, missed: Int, modifier: Modifier = Modifier) {
    val total = completed + missed
    val completionRate = if (total > 0) completed.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(targetValue = completionRate, animationSpec = tween(1500), label = "progressAnimation")

    PremiumBaseCard(modifier = modifier, height = 180.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Completion", color = ProgressTextSecondary, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(90.dp),
                    color = ProgressGreen,
                    trackColor = ProgressRed.copy(alpha = 0.2f),
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedProgress * 100).roundToInt()}%",
                        color = ProgressTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyActivityChart(
    activity: List<Int>
) {
    var animationTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationTrigger = true }
    val progress by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "chartReveal"
    )

    PremiumBaseCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Activity This Week", color = ProgressTextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val points =
                        if (activity.isEmpty())
                            listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
                        else
                            activity.map { it.toFloat() }
                    val maxVal =
                        (points.maxOrNull() ?: 1f)
                            .coerceAtLeast(1f)

                    val path = Path()
                    val widthPerPoint = size.width / (points.size - 1)

                    points.forEachIndexed { index, value ->
                        val x = index * widthPerPoint
                        val y = size.height - ((value / maxVal) * size.height)

                        if (index == 0) path.moveTo(x, y)
                        else {
                            val prevX = (index - 1) * widthPerPoint
                            val prevY = size.height - ((points[index - 1] / maxVal) * size.height)
                            path.cubicTo(
                                prevX + widthPerPoint / 2, prevY,
                                x - widthPerPoint / 2, y,
                                x, y
                            )
                        }
                    }

                    clipRect(right = size.width * progress) {
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(listOf(ProgressBlue.copy(alpha = 0.3f), Color.Transparent)),
                        )

                        drawPath(
                            path = path,
                            color = ProgressBlue,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(day, color = ProgressTextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
@Composable
private fun MonthlySummaryCard(
    totalWorkouts: Int,
    longestStreak: Int,
    completionRate: Int,
    completedSets: Int
) {
    PremiumBaseCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                "Monthly Summary",
                color = ProgressTextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStat(
                    Icons.Rounded.CheckCircle,
                    "Workouts",
                    totalWorkouts.toString(),
                    ProgressGreen
                )

                SummaryStat(
                    Icons.Filled.LocalFireDepartment,
                    "Streak",
                    longestStreak.toString(),
                    ProgressOrange
                )

                SummaryStat(
                    Icons.Rounded.Insights,
                    "Rate",
                    "$completionRate%",
                    ProgressBlue
                )
            }

            Text(
                "$completedSets sets completed this month",
                color = ProgressTextSecondary
            )
        }
    }
}

@Composable
private fun SummaryStat(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Text(value, color = ProgressTextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, color = ProgressTextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable

private fun PersonalRecordsCard(
    totalWorkouts: Int,
    longestStreak: Int,
    bestWeek: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Personal Records", color = ProgressTextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                "Longest Streak",
                longestStreak.toString(),
                "Days",
                Modifier.weight(1f)
            )

            MetricCard(
                "Best Week",
                bestWeek.toString(),
                "Logs",
                Modifier.weight(1f)
            )

            MetricCard(
                "Total",
                totalWorkouts.toString(),
                "Workouts",
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, suffix: String, modifier: Modifier = Modifier) {
    PremiumBaseCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = ProgressTextSecondary, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = ProgressTextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(2.dp))
                Text(suffix, color = ProgressTextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
    }
}

@Composable
private fun WorkoutHeatmap(
    workoutLogs: List<WorkoutDayLog>
){
    PremiumBaseCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Activity Heatmap", color = ProgressTextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            val weeks = 6
            val days = 7
            val last42Days =
                (0 until 42)
                    .map { LocalDate.now().minusDays(it.toLong()) }
                    .reversed()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (w in 0 until weeks) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (d in 0 until days) {

                            val workoutCount = workoutLogs.count {
                                it.date == last42Days[w * 7 + d]
                            }

                            val alpha = when {
                                workoutCount == 0 -> 0.08f
                                workoutCount == 1 -> 0.35f
                                workoutCount == 2 -> 0.65f
                                else -> 1f
                            }

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (workoutCount == 0)
                                            ProgressNeutral
                                        else
                                            ProgressGreen.copy(alpha = alpha)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedCalendarCard(
    visibleMonth: YearMonth,
    today: LocalDate,
    workoutSchedule: WeeklyWorkoutSchedule?,
    workoutLogs: List<WorkoutDayLog>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val days = remember(visibleMonth) { buildCalendarDays(visibleMonth) }

    PremiumBaseCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(targetState = visibleMonth, label = "calendarMonthSwitch") { month ->
                    Text(
                        text = month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.year,
                        color = ProgressTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onPreviousMonth, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Prev Month", tint = ProgressTextSecondary)
                    }
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month", tint = ProgressTextSecondary)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center,
                            color = ProgressTextSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                days.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        week.forEach { date ->
                            if (date == null) {
                                Box(modifier = Modifier.size(36.dp))
                            } else {
                                val status = workoutStatusForDate(date, workoutSchedule, workoutLogs, today)
                                CalendarDayCell(date = date, today = today, status = status)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(date: LocalDate, today: LocalDate, status: WorkoutDayStatus) {
    val isToday = date == today
    val background = when (status) {
        WorkoutDayStatus.COMPLETED -> ProgressGreen.copy(alpha = 0.22f)
        WorkoutDayStatus.PARTIAL -> ProgressOrange.copy(alpha = 0.22f)
        WorkoutDayStatus.MISSED -> ProgressRed.copy(alpha = 0.22f)
        WorkoutDayStatus.REST -> ProgressBlue.copy(alpha = 0.22f)
        WorkoutDayStatus.NONE -> Color.Transparent
    }

    val textColor = when {
        isToday -> ProgressBg
        status == WorkoutDayStatus.NONE -> ProgressTextPrimary
        status == WorkoutDayStatus.COMPLETED -> ProgressGreen
        status == WorkoutDayStatus.PARTIAL -> ProgressOrange
        status == WorkoutDayStatus.MISSED -> ProgressRed
        status == WorkoutDayStatus.REST -> ProgressBlue
        else -> ProgressTextPrimary
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isToday) ProgressGreen else background)
            .border(
                width = if (isToday) 0.dp else 1.dp,
                color = if (status != WorkoutDayStatus.NONE && !isToday) background.copy(alpha = 0.4f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${date.dayOfMonth}",
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.Insights,
            contentDescription = null,
            tint = ProgressBorder,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Start your fitness journey.",
            color = ProgressTextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Complete your first workout to unlock analytics.",
            color = ProgressTextSecondary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PremiumBaseCard(
    modifier: Modifier = Modifier,
    height: Dp = Dp.Unspecified,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (height != Dp.Unspecified) Modifier.height(height) else Modifier)
            .clip(RoundedCornerShape(24.dp))
            .background(ProgressCard)
            .border(1.dp, ProgressBorder, RoundedCornerShape(24.dp))
    ) {
        content()
    }
}

// --- Data Logic Architecture Helpers ---

private fun buildCalendarDays(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val leadingBlanks = firstDay.dayOfWeek.value % 7
    val dayCells = MutableList<LocalDate?>(leadingBlanks) { null }
    repeat(month.lengthOfMonth()) { offset ->
        dayCells += month.atDay(offset + 1)
    }
    while (dayCells.size % 7 != 0) {
        dayCells += null
    }
    return dayCells
}

private fun calculateWorkoutStreak(
    schedule: WeeklyWorkoutSchedule?,
    workoutLogs: List<WorkoutDayLog>,
    today: LocalDate
): Int {
    var streak = 0
    var cursor = today
    while (true) {
        when (workoutStatusForDate(cursor, schedule, workoutLogs, today)) {
            WorkoutDayStatus.COMPLETED -> {
                streak += 1
                cursor = cursor.minusDays(1)
            }
            WorkoutDayStatus.REST,
            WorkoutDayStatus.NONE -> cursor = cursor.minusDays(1)
            WorkoutDayStatus.PARTIAL,
            WorkoutDayStatus.MISSED -> break
        }
        if (cursor.isBefore(today.minusYears(2))) break
    }
    return streak
}

private fun workoutStatusForDate(
    date: LocalDate,
    schedule: WeeklyWorkoutSchedule?,
    workoutLogs: List<WorkoutDayLog>,
    today: LocalDate
): WorkoutDayStatus {
    val scheduleDay = schedule?.days?.firstOrNull { it.weekday == date.toWorkoutWeekday() }
        ?: return WorkoutDayStatus.NONE

    if (scheduleDay.focus == WorkoutFocus.REST) return WorkoutDayStatus.REST

    val log = workoutLogs.firstOrNull { it.date == date && it.weekday == scheduleDay.weekday }
        ?: return if (date.isBefore(today)) WorkoutDayStatus.MISSED else WorkoutDayStatus.NONE

    val isCompleted = scheduleDay.exercises.isNotEmpty() &&
            scheduleDay.exercises.all { config ->
                val progress = log.exercises.firstOrNull { it.exerciseName == config.exerciseName }
                progress != null && progress.completedSets >= config.sets
            }

    if (isCompleted) return WorkoutDayStatus.COMPLETED

    val hasStarted = log.exercises.any { it.sessionCount > 0 || it.completedSets > 0 }
    return if (hasStarted) WorkoutDayStatus.PARTIAL else if (date.isBefore(today)) WorkoutDayStatus.MISSED else WorkoutDayStatus.NONE
}

private fun LocalDate.toWorkoutWeekday(): WorkoutWeekday = when (dayOfWeek) {
    DayOfWeek.SUNDAY -> WorkoutWeekday.SUNDAY
    DayOfWeek.MONDAY -> WorkoutWeekday.MONDAY
    DayOfWeek.TUESDAY -> WorkoutWeekday.TUESDAY
    DayOfWeek.WEDNESDAY -> WorkoutWeekday.WEDNESDAY
    DayOfWeek.THURSDAY -> WorkoutWeekday.THURSDAY
    DayOfWeek.FRIDAY -> WorkoutWeekday.FRIDAY
    DayOfWeek.SATURDAY -> WorkoutWeekday.SATURDAY
}