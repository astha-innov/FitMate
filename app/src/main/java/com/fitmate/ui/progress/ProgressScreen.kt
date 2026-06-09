package com.fitmate.ui.progress

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitmate.domain.model.WorkoutDayLog
import com.fitmate.domain.model.WorkoutDayStatus
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.model.WorkoutWeekday
import com.fitmate.domain.model.WeeklyWorkoutSchedule
import com.fitmate.ui.viewmodel.CampusFitUiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val ProgressBg = Color(0xFF0B0D12)
private val ProgressCard = Color(0xFF151922)
private val ProgressBorder = Color(0xFFFFFFFF).copy(alpha = 0.10f)
private val ProgressTextPrimary = Color(0xFFF5F7FA)
private val ProgressTextSecondary = Color(0xFFB2BAC8)
private val ProgressGreen = Color(0xFF1DE9B6)
private val ProgressOrange = Color(0xFFFFB74D)
private val ProgressRed = Color(0xFFFF6B6B)
private val ProgressBlue = Color(0xFF4FC3F7)
private val ProgressNeutral = Color(0xFF2B3240)
private val ProgressTodayBorder = Color(0xFF8BE9FD)

@Composable
fun ProgressScreen(
    state: CampusFitUiState,
    onOpenCoach: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var visibleMonth by rememberSaveable { mutableStateOf(YearMonth.now()) }
    val scrollState = rememberScrollState()
    val workoutSchedule = state.workoutSchedule
    val workoutLogs = state.workoutLogs
    val streakDays = remember(workoutSchedule, workoutLogs, today) {
        calculateWorkoutStreak(workoutSchedule, workoutLogs, today)
    }
    val weekDays = remember(today) { currentWeekDates(today) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ProgressBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Progress",
            color = ProgressTextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        StreakCard(
            streakDays = streakDays,
            weekDates = weekDays,
            workoutSchedule = workoutSchedule,
            workoutLogs = workoutLogs,
            today = today
        )

        if (onOpenCoach != null) {
            AiCoachShortcutCard(onClick = onOpenCoach)
        }

        WorkoutCalendarCard(
            visibleMonth = visibleMonth,
            today = today,
            workoutSchedule = workoutSchedule,
            workoutLogs = workoutLogs,
            onPreviousMonth = { visibleMonth = visibleMonth.minusMonths(1) },
            onNextMonth = { visibleMonth = visibleMonth.plusMonths(1) }
        )
    }
}

@Composable
private fun AiCoachShortcutCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ProgressCard),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            ProgressBlue.copy(alpha = 0.28f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ProgressBlue.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = ProgressBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "AI Coach",
                    color = ProgressTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Ask FitMate for workout, recovery, and consistency guidance.",
                    color = ProgressTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StreakCard(
    streakDays: Int,
    weekDates: List<LocalDate>,
    workoutSchedule: WeeklyWorkoutSchedule?,
    workoutLogs: List<WorkoutDayLog>,
    today: LocalDate
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ProgressCard),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFFA726), Color(0xFFFF5252))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = if (streakDays == 1) "1 Day Streak" else "$streakDays Day Streak",
                        color = ProgressTextPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Stay consistent on workout days. Rest days keep the fire alive.",
                        color = ProgressTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDates.forEach { date ->
                    val status = workoutStatusForDate(date, workoutSchedule, workoutLogs, today)
                    val isToday = date == today
                    WeeklyDayPill(
                        label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(3),
                        status = status,
                        isToday = isToday
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyDayPill(
    label: String,
    status: WorkoutDayStatus,
    isToday: Boolean
) {
    val background = when (status) {
        WorkoutDayStatus.COMPLETED -> ProgressGreen
        WorkoutDayStatus.REST -> ProgressBlue.copy(alpha = 0.3f)
        else -> ProgressNeutral
    }
    val border = when {
        isToday -> ProgressTodayBorder
        status == WorkoutDayStatus.REST -> ProgressBlue.copy(alpha = 0.7f)
        else -> Color.Transparent
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(18.dp),
            color = background,
            border = androidx.compose.foundation.BorderStroke(
                width = if (border == Color.Transparent) 0.dp else 1.5.dp,
                color = border
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                when (status) {
                    WorkoutDayStatus.COMPLETED -> Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    WorkoutDayStatus.REST -> Text(
                        text = "R",
                        color = ProgressBlue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    else -> Box(
                        modifier = Modifier
                            .size(if (isToday) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isToday) ProgressTodayBorder else ProgressTextSecondary.copy(alpha = 0.45f))
                    )
                }
            }
        }
        Text(
            text = label,
            color = if (isToday) ProgressTextPrimary else ProgressTextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun WorkoutCalendarCard(
    visibleMonth: YearMonth,
    today: LocalDate,
    workoutSchedule: WeeklyWorkoutSchedule?,
    workoutLogs: List<WorkoutDayLog>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val days = remember(visibleMonth) { buildCalendarDays(visibleMonth) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ProgressCard),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Workout calendar",
                        color = ProgressTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    AnimatedContent(targetState = visibleMonth, label = "month-switch") { month ->
                        Text(
                            text = month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.year,
                            color = ProgressTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Row {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = "Previous month",
                            tint = ProgressTextPrimary
                        )
                    }
                    IconButton(onClick = onNextMonth) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Next month",
                            tint = ProgressTextPrimary
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                SpacerCell()
                            } else {
                                CalendarDayCell(
                                    date = date,
                                    today = today,
                                    status = workoutStatusForDate(date, workoutSchedule, workoutLogs, today)
                                )
                            }
                        }
                    }
                }
            }

            CalendarLegend()
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    today: LocalDate,
    status: WorkoutDayStatus
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (date == today) 1f else 0.92f,
        animationSpec = tween(300),
        label = "calendar-day-alpha"
    )
    val background = when (status) {
        WorkoutDayStatus.COMPLETED -> ProgressGreen.copy(alpha = 0.24f * animatedAlpha)
        WorkoutDayStatus.PARTIAL -> ProgressOrange.copy(alpha = 0.24f * animatedAlpha)
        WorkoutDayStatus.MISSED -> ProgressRed.copy(alpha = 0.24f * animatedAlpha)
        WorkoutDayStatus.REST -> ProgressBlue.copy(alpha = 0.22f * animatedAlpha)
        WorkoutDayStatus.NONE -> ProgressNeutral.copy(alpha = 0.75f * animatedAlpha)
    }
    val textColor = when (status) {
        WorkoutDayStatus.COMPLETED -> ProgressGreen
        WorkoutDayStatus.PARTIAL -> ProgressOrange
        WorkoutDayStatus.MISSED -> ProgressRed
        WorkoutDayStatus.REST -> ProgressBlue
        WorkoutDayStatus.NONE -> ProgressTextPrimary
    }

    Surface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(12.dp),
        color = background,
        border = androidx.compose.foundation.BorderStroke(
            width = if (date == today) 1.5.dp else 0.dp,
            color = if (date == today) ProgressTodayBorder else Color.Transparent
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "${date.dayOfMonth}",
                color = textColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (date == today) FontWeight.ExtraBold else FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SpacerCell() {
    Box(modifier = Modifier.size(36.dp))
}

@Composable
private fun CalendarLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Legend",
            color = ProgressTextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LegendItem("Completed Workout", ProgressGreen, Modifier.weight(1f))
            LegendItem("Partially Completed", ProgressOrange, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LegendItem("Missed Workout", ProgressRed, Modifier.weight(1f))
            LegendItem("Rest Day", ProgressBlue, Modifier.weight(1f))
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProgressBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                color = ProgressTextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

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

private fun currentWeekDates(today: LocalDate): List<LocalDate> {
    val sunday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    return List(7) { sunday.plusDays(it.toLong()) }
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
