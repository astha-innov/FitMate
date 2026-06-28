package com.fitmate.ui.progress

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitmate.R
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
import java.util.Locale
import kotlin.math.roundToInt

// --- Premium White Design Theme Colors ---
private val CanvasWhite = Color(0xFFF7F9FC)
private val CardWhite = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val FitGreen = Color(0xFF16C47F)
private val FitGreenLight = Color(0xFFE8FBF3)
private val DividerColor = Color(0xFFF0F2F5)

// Accent Colors for Metrics
private val FitBlue = Color(0xFF3B82F6)
private val FitOrange = Color(0xFFF59E0B)

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
    val streakDays = analytics.currentStreak

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasWhite)
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
private fun PremiumBaseCard(modifier: Modifier = Modifier, height: Dp? = null, content: @Composable () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (height != null) Modifier.height(height) else Modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
    ) {
        content()
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(FitGreenLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = FitOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (streak == 1) stringResource(R.string.progress_streak_day_single) else stringResource(R.string.progress_streak_days_multi, streak),
                                color = TextDark,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text =
                                stringResource(R.string.progress_hero_summary,
                                    totalWorkouts,
                                    streak,
                                    if (consistencyScore >= 80)
                                        stringResource(R.string.coach_ai_insight_body)
                                    else
                                        stringResource(R.string.progress_insight_consistency_low)
                                ),
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, start = 52.dp)
                        )
                    }
                }

                HorizontalDivider(color = DividerColor)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.progress_consistency_score),
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.progress_percentage, consistencyScore),
                            color = TextDark,
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
                            color = FitGreen,
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
            .clickable { onClick() }
            .clip(RoundedCornerShape(24.dp))
            .background(CardWhite)
            .border(1.dp, DividerColor, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(FitGreenLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = stringResource(R.string.coach_ai_metrics), tint = FitGreen, modifier = Modifier.size(28.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.progress_ai_insight),
                    color = TextDark,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val insight = when {
                    completionRate >= 80 ->
                        stringResource(R.string.progress_insight_excellent)
                    completionRate >= 60 ->
                        stringResource(R.string.progress_insight_good)
                    else ->
                        stringResource(R.string.progress_insight_poor)
                }

                Text(
                    text = insight,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                Text(
                    text = stringResource(R.string.progress_streak_workouts, streak, totalWorkouts),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
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
            Text(stringResource(R.string.progress_fitness_score_label), color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedScore / 100f },
                    modifier = Modifier.size(90.dp),
                    color = FitBlue,
                    trackColor = DividerColor,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "$animatedScore",
                    color = TextDark,
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
            Text(stringResource(R.string.progress_completion_label), color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(90.dp),
                    color = FitGreen,
                    trackColor = DividerColor,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.progress_percentage, (animatedProgress * 100).roundToInt()),
                        color = TextDark,
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
            Text(stringResource(R.string.progress_activity_week_title), color = TextDark, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                            brush = Brush.verticalGradient(listOf(FitBlue.copy(alpha = 0.15f), Color.Transparent)),
                        )

                        drawPath(
                            path = path,
                            color = FitBlue,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(
                    stringResource(R.string.day_mon_short),
                    stringResource(R.string.day_tue_short),
                    stringResource(R.string.day_wed_short),
                    stringResource(R.string.day_thu_short),
                    stringResource(R.string.day_fri_short),
                    stringResource(R.string.day_sat_short),
                    stringResource(R.string.day_sun_short)
                ).forEach { day ->
                    Text(day, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
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
                stringResource(R.string.progress_monthly_summary_title),
                color = TextDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStat(
                    Icons.Rounded.CheckCircle,
                    stringResource(R.string.progress_stat_workouts),
                    totalWorkouts.toString(),
                    FitGreen
                )

                SummaryStat(
                    Icons.Filled.LocalFireDepartment,
                    stringResource(R.string.progress_stat_streak),
                    longestStreak.toString(),
                    FitOrange
                )

                SummaryStat(
                    Icons.Rounded.Insights,
                    stringResource(R.string.progress_stat_rate),
                    stringResource(R.string.progress_percentage, completionRate),
                    FitBlue
                )
            }

            Text(
                stringResource(R.string.progress_sets_completed_suffix, completedSets),
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SummaryStat(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Text(value, color = TextDark, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun PersonalRecordsCard(
    totalWorkouts: Int,
    longestStreak: Int,
    bestWeek: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.progress_personal_records_title), color = TextDark, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                stringResource(R.string.progress_longest_streak_label),
                longestStreak.toString(),
                stringResource(R.string.progress_days_suffix),
                Modifier.weight(1f)
            )

            MetricCard(
                stringResource(R.string.progress_best_week_label),
                bestWeek.toString(),
                stringResource(R.string.progress_logs_suffix),
                Modifier.weight(1f)
            )

            MetricCard(
                stringResource(R.string.progress_total_label),
                totalWorkouts.toString(),
                stringResource(R.string.progress_workouts_suffix),
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
            Text(title, color = TextSecondary, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = TextDark, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(suffix, color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
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
            Text(stringResource(R.string.progress_heatmap_title), color = TextDark, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                                workoutCount == 0 -> 1f
                                workoutCount == 1 -> 0.35f
                                workoutCount == 2 -> 0.65f
                                else -> 1f
                            }

                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (workoutCount == 0)
                                            CanvasWhite
                                        else
                                            FitGreen.copy(alpha = alpha)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (workoutCount == 0) DividerColor else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
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
                        color = TextDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onPreviousMonth, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.progress_calendar_prev), tint = TextDark)
                    }
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.progress_calendar_next), tint = TextDark)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(
                        stringResource(R.string.day_sun_short),
                        stringResource(R.string.day_mon_short),
                        stringResource(R.string.day_tue_short),
                        stringResource(R.string.day_wed_short),
                        stringResource(R.string.day_thu_short),
                        stringResource(R.string.day_fri_short),
                        stringResource(R.string.day_sat_short)
                    ).forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center,
                            color = TextSecondary,
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
        WorkoutDayStatus.COMPLETED -> FitGreen.copy(alpha = 0.15f)
        WorkoutDayStatus.PARTIAL -> FitOrange.copy(alpha = 0.15f)
        WorkoutDayStatus.MISSED -> Color.Transparent
        WorkoutDayStatus.REST -> CanvasWhite
        else -> Color.Transparent
    }

    val textColor = when {
        isToday -> CardWhite
        status == WorkoutDayStatus.COMPLETED -> FitGreen
        status == WorkoutDayStatus.MISSED -> TextSecondary
        else -> TextDark
    }

    val boxModifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(if (isToday) FitGreen else background)
        .border(
            width = 1.dp,
            color = if (isToday) FitGreen else if (status == WorkoutDayStatus.REST) DividerColor else Color.Transparent,
            shape = CircleShape
        )

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || status == WorkoutDayStatus.COMPLETED) FontWeight.Bold else FontWeight.Normal
        )

        if (status == WorkoutDayStatus.MISSED) {
            Icon(
                imageVector = Icons.Rounded.Cancel,
                contentDescription = stringResource(R.string.progress_status_missed),
                tint = TextSecondary.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 2.dp)
            )
        }
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
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(FitGreenLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Insights,
                contentDescription = null,
                tint = FitGreen,
                modifier = Modifier.size(50.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.progress_no_data_title),
            color = TextDark,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.progress_no_data_subtitle),
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

// --- Logic Helpers ---

private fun buildCalendarDays(yearMonth: YearMonth): List<LocalDate?> {
    val days = mutableListOf<LocalDate?>()
    val firstOfMonth = yearMonth.atDay(1)
    val lastOfMonth = yearMonth.atEndOfMonth()

    // Sunday is index 7 in DayOfWeek, we want it to be 0 for layout logic.
    val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7

    for (i in 0 until firstDayOfWeek) {
        days.add(null)
    }

    var current = firstOfMonth
    while (!current.isAfter(lastOfMonth)) {
        days.add(current)
        current = current.plusDays(1)
    }

    while (days.size % 7 != 0) {
        days.add(null)
    }
    return days
}

private fun workoutStatusForDate(
    date: LocalDate,
    schedule: WeeklyWorkoutSchedule?,
    logs: List<WorkoutDayLog>,
    today: LocalDate
): WorkoutDayStatus {
    // Standardized fallback logic to prevent crash
    val logsForDay = logs.filter { it.date == date }

    if (logsForDay.isNotEmpty()) return WorkoutDayStatus.COMPLETED
    if (date.isAfter(today)) return WorkoutDayStatus.NONE // Future
    if (date.isBefore(today)) return WorkoutDayStatus.MISSED // Past missed

    return WorkoutDayStatus.NONE
}