package com.fitmate.ui.workout

import android.os.Build
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.LocalImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.fitmate.R
import com.fitmate.data.LocalExerciseDatabase
import com.fitmate.data.LocalExerciseCatalog
import com.fitmate.domain.model.ExerciseLibraryEntry
import com.fitmate.domain.model.ExerciseMetricType
import com.fitmate.domain.model.WorkoutDayLog
import com.fitmate.domain.model.WeeklyWorkoutSchedule
import com.fitmate.domain.model.WorkoutDaySchedule
import com.fitmate.domain.model.WorkoutExerciseConfig
import com.fitmate.domain.model.WorkoutExerciseProgress
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.model.WorkoutPlanType
import com.fitmate.domain.model.WorkoutWeekday
import com.fitmate.ui.viewmodel.CampusFitUiState
import com.fitmate.ui.viewmodel.CampusFitViewModel

// ── Premium Light Palette ──────────────────────────────────────────────────
private val FitMateWhiteBackground = Color(0xFFFFFFFF)
private val FitMateCard            = Color(0xFFF8FAFC)
private val FitMateSurface         = Color(0xFFFFFFFF)

private val FitMateTextPrimary     = Color(0xFF111827)
private val FitMateTextSecondary   = Color(0xFF6B7280)

private val FitMateGreen           = Color(0xFF10B981)
private val FitMateGreenLight      = Color(0xFF34D399)

private val FitMateBlue            = Color(0xFF3B82F6)
private val FitMateRed             = Color(0xFFEF4444)

private val FitMateBorder          = Color(0xFFE5E7EB)

private val RestAccent             = Color(0xFF3B82F6)
private val EasyColor              = Color(0xFF10B981)
private val MediumColor            = Color(0xFFF59E0B)
private val HardColor              = Color(0xFFEF4444)
// ──────────────────────────────────────────────────────────────────────────

private val workoutWeekdaysMondayFirst = listOf(
    WorkoutWeekday.MONDAY,
    WorkoutWeekday.TUESDAY,
    WorkoutWeekday.WEDNESDAY,
    WorkoutWeekday.THURSDAY,
    WorkoutWeekday.FRIDAY,
    WorkoutWeekday.SATURDAY,
    WorkoutWeekday.SUNDAY,
)

private val workoutWeekdayDisplayIndex = workoutWeekdaysMondayFirst
    .withIndex()
    .associate { (index, weekday) -> weekday to index }

private val customWorkoutFocusOptions = listOf(
    WorkoutFocus.PUSH,
    WorkoutFocus.PULL,
    WorkoutFocus.FULL_BODY,
    WorkoutFocus.MOBILITY,
    WorkoutFocus.ARMS_ABS,
    WorkoutFocus.LEGS_SHOULDERS,
    WorkoutFocus.UPPER_BODY_POWER,
    WorkoutFocus.ARM_SPECIALIZATION_WEAK_POINTS,
    WorkoutFocus.REST,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    state: CampusFitUiState,
    viewModel: CampusFitViewModel
) {
    val context = LocalContext.current
    val gifImageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val schedule = state.workoutSchedule
    val workoutListState = rememberLazyListState()
    var showPlanChoiceDialog by rememberSaveable { mutableStateOf(false) }
    var showPlanBuilder by rememberSaveable { mutableStateOf(false) }
    var showGoalChangeDialog by rememberSaveable { mutableStateOf(false) }
    var showReplaceCustomDialog by rememberSaveable { mutableStateOf(false) }
    var dismissedGoalPromptFor by rememberSaveable { mutableStateOf<String?>(null) }
    var editingDay by remember { mutableStateOf<WorkoutDaySchedule?>(null) }
    var selectedInstructionExercise by remember { mutableStateOf<SelectedExerciseDetail?>(null) }
    val collapsedDays = remember {
        mutableStateMapOf<String, Boolean>()
    }

    LaunchedEffect(schedule) {
        if (schedule == null) {
            showPlanChoiceDialog = true
        }
    }

    LaunchedEffect(schedule?.planType, schedule?.generatedForGoal, state.profile.goal) {
        val recommendedGoalChanged = schedule?.planType == WorkoutPlanType.DEFAULT &&
                schedule.generatedForGoal != state.profile.goal
        if (recommendedGoalChanged && dismissedGoalPromptFor != state.profile.goal.name) {
            showGoalChangeDialog = true
        }
    }

    if (showPlanChoiceDialog) {
        PlanChoiceDialog(
            onDismiss = { showPlanChoiceDialog = false },
            onChooseDefault = {
                viewModel.saveWorkoutSchedule(
                    viewModel.generateRecommendedWorkoutSchedule(state.profile)
                )
                showPlanChoiceDialog = false
            },
            onChooseCustom = {
                showPlanChoiceDialog = false
                showPlanBuilder = true
            }
        )
    }

    if (showGoalChangeDialog) {
        RecommendedPlanConfirmationDialog(
            title = stringResource(R.string.workout_goal_changed_title),
            message = stringResource(R.string.workout_goal_changed_msg, state.profile.goal.label),
            confirmLabel = stringResource(R.string.workout_btn_confirm),
            onDismiss = {
                dismissedGoalPromptFor = state.profile.goal.name
                showGoalChangeDialog = false
            },
            onConfirm = {
                viewModel.saveWorkoutSchedule(
                    viewModel.generateRecommendedWorkoutSchedule(state.profile)
                )
                dismissedGoalPromptFor = null
                showGoalChangeDialog = false
            }
        )
    }

    if (showReplaceCustomDialog) {
        RecommendedPlanConfirmationDialog(
            title = stringResource(R.string.workout_replace_custom_title),
            message = stringResource(R.string.workout_replace_custom_msg, state.profile.goal.label),
            confirmLabel = stringResource(R.string.workout_btn_confirm),
            onDismiss = { showReplaceCustomDialog = false },
            onConfirm = {
                viewModel.saveWorkoutSchedule(
                    viewModel.generateRecommendedWorkoutSchedule(state.profile)
                )
                showReplaceCustomDialog = false
            }
        )
    }

    CompositionLocalProvider(LocalImageLoader provides gifImageLoader) {

        if (showPlanBuilder) {
            SequentialPlanBuilderDialog(
                initialSchedule = schedule,
                fallbackSchedule = viewModel.generateRecommendedWorkoutSchedule(state.profile),
                onDismiss = { showPlanBuilder = false },
                onSave = {
                    viewModel.saveWorkoutSchedule(it)
                    showPlanBuilder = false
                }
            )
        }

        editingDay?.let { day ->
            WorkoutDayEditorDialog(
                day = day,
                onDismiss = { editingDay = null },
                onSave = { updatedDay ->
                    val currentSchedule = state.workoutSchedule
                        ?: viewModel.generateRecommendedWorkoutSchedule(state.profile)
                    viewModel.saveWorkoutSchedule(
                        currentSchedule.copy(
                            days = currentSchedule.days.map {
                                if (it.weekday == updatedDay.weekday) updatedDay else it
                            },
                            isCustom = true,
                            planType = WorkoutPlanType.CUSTOM,
                            generatedForGoal = null,
                        )
                    )
                    editingDay = null
                }
            )
        }

        MaterialTheme(
            colorScheme = androidx.compose.material3.lightColorScheme(
                background = FitMateWhiteBackground,
                surface = FitMateSurface,
                primary = FitMateGreen,
                onPrimary = FitMateWhiteBackground,
                onBackground = FitMateTextPrimary,
                onSurface = FitMateTextPrimary
            )
        ) {
            if (selectedInstructionExercise != null) {
                WorkoutInstructionScreen(
                    detail = selectedInstructionExercise!!,
                    workoutLogs = state.workoutLogs,
                    viewModel = viewModel,
                    onBack = { selectedInstructionExercise = null }
                )
            } else {
                Scaffold(
                    containerColor = FitMateWhiteBackground
                ) { paddingValues ->
                    LazyColumn(
                        state = workoutListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(FitMateWhiteBackground)
                            .padding(paddingValues),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        item {
                            HeroGifCarousel()
                        }

                        item {
                            WorkoutHeroCard(
                                isCustom = schedule?.isCustom == true,
                                onCustomize = { showPlanBuilder = true },
                                onUseRecommended = {
                                    if (schedule?.planType == WorkoutPlanType.CUSTOM) {
                                        showReplaceCustomDialog = true
                                    } else {
                                        viewModel.saveWorkoutSchedule(
                                            viewModel.generateRecommendedWorkoutSchedule(state.profile)
                                        )
                                    }
                                }
                            )
                        }

                        schedule?.days?.let { days ->
                            val displayDays = days.sortedBy {
                                workoutWeekdayDisplayIndex[it.weekday] ?: Int.MAX_VALUE
                            }
                            items(displayDays, key = { it.weekday.name }) { day ->
                                WorkoutDayCard(
                                    day = day,
                                    workoutLogs = state.workoutLogs,
                                    onEdit = { editingDay = day },
                                    onShowInstructions = { selectedInstructionExercise = it },
                                    collapsed = collapsedDays[day.weekday.name] == true,
                                    onToggleCollapse = {
                                        val key = day.weekday.name
                                        collapsedDays[key] = !(collapsedDays[key] == true)
                                    }
                                )
                            }
                        }

                        item {
                            MotivationBanner()
                        }
                    }
                }
            }
        }
    } // CompositionLocalProvider
}

@Composable
private fun WorkoutHeroCard(
    isCustom: Boolean,
    onCustomize: () -> Unit,
    onUseRecommended: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FitMateCard),
        border = BorderStroke(1.dp, FitMateBorder),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = if (isCustom) stringResource(R.string.workout_hero_custom_title) else stringResource(R.string.workout_hero_recommended_title),
                style = MaterialTheme.typography.headlineSmall,
                color = FitMateTextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isCustom) {
                    stringResource(R.string.workout_hero_custom_desc)
                } else {
                    stringResource(R.string.workout_hero_recommended_desc)
                },
                color = FitMateTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onCustomize,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitMateGreen,
                    contentColor = FitMateWhiteBackground
                )
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.workout_btn_make_custom), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onUseRecommended,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, FitMateBorder)
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, tint = FitMateTextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCustom) {
                        stringResource(R.string.workout_btn_use_recommended)
                    } else {
                        stringResource(R.string.workout_btn_regenerate_recommended)
                    },
                    color = FitMateTextPrimary
                )
            }
        }
    }
}

@Composable
private fun WorkoutDayCard(
    day: WorkoutDaySchedule,
    workoutLogs: List<WorkoutDayLog>,
    onEdit: () -> Unit,
    onShowInstructions: (SelectedExerciseDetail) -> Unit,
    collapsed: Boolean,
    onToggleCollapse: () -> Unit
) {
    val isRestDay = day.focus == WorkoutFocus.REST
    val focusImage = focusPreviewImage(day.focus)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FitMateCard),
        border = BorderStroke(1.dp, if (isRestDay) RestAccent.copy(alpha = 0.4f) else FitMateBorder),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (focusImage.isNotBlank()) {
                    AsyncImage(
                        model = "file:///android_asset/exercises/$focusImage",
                        contentDescription = day.focus.label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(FitMateCard, Color(0xFFE0F2FE))
                                )
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Text(
                        text = day.weekday.label,
                        color = if (isRestDay) RestAccent else FitMateGreenLight,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = day.focus.label,
                        color = FitMateWhiteBackground,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (!isRestDay && day.exercises.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.workout_exercises_count, day.exercises.size),
                            color = FitMateGreenLight,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onToggleCollapse,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = if (collapsed) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess,
                        contentDescription = if (collapsed) stringResource(R.string.workout_calendar_next) else stringResource(R.string.workout_calendar_prev),
                        tint = FitMateTextPrimary
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.workout_edit_day_title, day.weekday.label),
                        tint = FitMateTextPrimary
                    )
                }
            }

            if (!collapsed) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (isRestDay) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.EventBusy, contentDescription = null, tint = RestAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.workout_rest_day_note),
                                color = FitMateTextSecondary
                            )
                        }
                    } else {
                        day.exercises.forEach { exercise ->
                            val entry = LocalExerciseDatabase.exerciseByName(exercise.exerciseName) ?: return@forEach
                            ExerciseCardRow(
                                config = exercise,
                                entry = entry,
                                progress = todayProgressFor(day.weekday, exercise.exerciseName, workoutLogs),
                                onShowInstructions = {
                                    onShowInstructions(
                                        SelectedExerciseDetail(
                                            exercise = entry,
                                            config = exercise,
                                            weekday = day.weekday,
                                            focus = day.focus,
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCardRow(
    config: WorkoutExerciseConfig,
    entry: ExerciseLibraryEntry,
    progress: WorkoutExerciseProgress?,
    onShowInstructions: () -> Unit
) {
    val workload = config.sets * config.amount
    val band = difficultyBand(entry, workload)
    val isCompleted = (progress?.completedSets ?: 0) >= config.sets && config.sets > 0

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = FitMateSurface,
        border = BorderStroke(1.dp, FitMateBorder),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            DifficultyPill(band = band)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = assetModel(entry.postureImage),
                    contentDescription = entry.name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(FitMateCard),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(FitMateCard),
                    error = ColorPainter(FitMateCard),
                    fallback = ColorPainter(FitMateCard),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        color = FitMateTextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.workout_sets_reps_format, config.sets, formatAmount(entry, config.amount)),
                        color = FitMateBlue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = entry.instructions,
                        color = FitMateTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onShowInstructions,
                        border = BorderStroke(1.dp, FitMateBorder),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.workout_btn_open), color = FitMateTextPrimary)
                    }
                    AnimatedVisibility(
                        visible = isCompleted,
                        enter = fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.84f)
                    ) {
                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = RoundedCornerShape(999.dp),
                                color = FitMateGreen
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = stringResource(R.string.workout_completed),
                                        tint = FitMateWhiteBackground,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.workout_completed),
                                color = FitMateGreen,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutInstructionScreen(
    detail: SelectedExerciseDetail,
    workoutLogs: List<WorkoutDayLog>,
    viewModel: CampusFitViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val exercise = detail.exercise
    val totalSets = detail.config.sets.coerceAtLeast(1)
    val markdown = remember(exercise.instructionMarkdownAsset) {
        loadAssetText(context, "workout_details/instructions/${exercise.instructionMarkdownAsset}")
    }
    val parsed = remember(markdown, exercise.name, exercise.instructions) {
        if (markdown.isBlank()) {
            ParsedInstructions(
                title = exercise.name + " " + context.getString(R.string.workout_instructions_label).lowercase(),
                steps = listOf(
                    exercise.instructions.ifBlank {
                        LocalExerciseCatalog.DEFAULT_INSTRUCTION_FALLBACK
                    }
                )
            )
        } else {
            parseInstructionMarkdown(markdown)
        }
    }
    val persistedProgress = remember(workoutLogs, detail.weekday, exercise.name) {
        todayProgressFor(detail.weekday, exercise.name, workoutLogs)
    }
    var instructionsExpanded by rememberSaveable(exercise.name) { mutableStateOf(false) }
    var elapsedSeconds by rememberSaveable(exercise.name, detail.weekday.name) {
        mutableIntStateOf(persistedProgress?.lastElapsedSeconds ?: 0)
    }
    var isTimerRunning by rememberSaveable(exercise.name) { mutableStateOf(false) }
    var completedSets by rememberSaveable(exercise.name, detail.weekday.name) {
        mutableIntStateOf((persistedProgress?.completedSets ?: 0).coerceAtMost(totalSets))
    }
    var questionnaireVisible by rememberSaveable(exercise.name) { mutableStateOf(false) }
    var selectedStopReason by rememberSaveable(exercise.name) { mutableStateOf<StopReason?>(null) }
    var questionnaireSubmitted by rememberSaveable(exercise.name) { mutableStateOf(false) }
    var questionnaireError by rememberSaveable(exercise.name) { mutableStateOf<String?>(null) }
    val progressRatio by animateFloatAsState(
        targetValue = (completedSets.toFloat() / totalSets.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "workout-progress"
    )

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            elapsedSeconds += 1
        }
    }

    Scaffold(
        containerColor = FitMateWhiteBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FitMateWhiteBackground,
                    titleContentColor = FitMateTextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = FitMateTextPrimary
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = exercise.name,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.workout_exercise_instructions),
                            style = MaterialTheme.typography.labelMedium,
                            color = FitMateTextSecondary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FitMateWhiteBackground)
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FitMateCard),
                    border = BorderStroke(1.dp, FitMateBorder),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        AsyncImage(
                            model = detailAssetModel(exercise.detailGifAsset),
                            contentDescription = exercise.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(FitMateCard),
                            contentScale = ContentScale.Fit,
                            placeholder = ColorPainter(FitMateCard),
                            error = ColorPainter(FitMateCard),
                            fallback = ColorPainter(FitMateCard),
                        )
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.workout_sets_reps_format, detail.config.sets, formatAmount(exercise, detail.config.amount)),
                                color = FitMateBlue,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            WorkoutProgressCard(
                                completedSets = completedSets,
                                totalSets = totalSets,
                                progressRatio = progressRatio
                            )
                            WorkoutTimerCard(
                                elapsedSeconds = elapsedSeconds,
                                isTimerRunning = isTimerRunning,
                                workoutComplete = completedSets >= totalSets,
                                onStart = {
                                    if (completedSets >= totalSets) return@WorkoutTimerCard
                                    if (!isTimerRunning) {
                                        elapsedSeconds = 0
                                        isTimerRunning = true
                                        questionnaireVisible = false
                                        selectedStopReason = null
                                        questionnaireSubmitted = false
                                        questionnaireError = null
                                    }
                                },
                                onStop = {
                                    if (!isTimerRunning) return@WorkoutTimerCard
                                    isTimerRunning = false
                                    questionnaireVisible = true
                                    questionnaireError = null
                                }
                            )
                            if (questionnaireVisible) {
                                WorkoutStopQuestionnaire(
                                    selectedReason = selectedStopReason,
                                    submitted = questionnaireSubmitted,
                                    errorMessage = questionnaireError,
                                    onSelect = { reason ->
                                        if (!questionnaireSubmitted) {
                                            selectedStopReason = reason
                                            questionnaireError = null
                                        }
                                    },
                                    onSubmit = {
                                        when {
                                            questionnaireSubmitted -> Unit
                                            selectedStopReason == null -> {
                                                questionnaireError = context.getString(R.string.workout_error_select_reason)
                                            }
                                            else -> {
                                                if (selectedStopReason == StopReason.COMPLETED_SET) {
                                                    completedSets = (completedSets + 1).coerceAtMost(totalSets)
                                                }
                                                viewModel.recordWorkoutSet(
                                                    weekday = detail.weekday,
                                                    focus = detail.focus,
                                                    exerciseName = exercise.name,
                                                    totalSets = totalSets,
                                                    elapsedSeconds = elapsedSeconds,
                                                    incrementCompletedSet = selectedStopReason == StopReason.COMPLETED_SET,
                                                )
                                                questionnaireSubmitted = true
                                                questionnaireVisible = false
                                                questionnaireError = null
                                            }
                                        }
                                    }
                                )
                            }
                            InstructionsAccordion(
                                title = parsed.title.ifBlank { exercise.name + " " + context.getString(R.string.workout_instructions_label).lowercase() },
                                steps = parsed.steps,
                                expanded = instructionsExpanded,
                                onToggle = { instructionsExpanded = !instructionsExpanded }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutProgressCard(
    completedSets: Int,
    totalSets: Int,
    progressRatio: Float
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = FitMateCard,
        border = BorderStroke(1.dp, FitMateBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.workout_set_progress),
                    color = FitMateTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.workout_sets_progress, completedSets, totalSets),
                    color = if (completedSets >= totalSets) FitMateGreen else FitMateBlue,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = if (completedSets >= totalSets) FitMateGreen else FitMateBlue,
                trackColor = FitMateBorder
            )
            Text(
                text = if (completedSets >= totalSets) {
                    stringResource(R.string.workout_progress_desc_complete)
                } else {
                    stringResource(R.string.workout_progress_desc_incomplete)
                },
                color = FitMateTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun WorkoutTimerCard(
    elapsedSeconds: Int,
    isTimerRunning: Boolean,
    workoutComplete: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = FitMateCard,
        border = BorderStroke(1.dp, FitMateBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.workout_timer_label),
                color = FitMateTextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStart,
                    enabled = !isTimerRunning && !workoutComplete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FitMateGreen,
                        contentColor = FitMateWhiteBackground,
                        disabledContainerColor = FitMateGreen.copy(alpha = 0.3f),
                        disabledContentColor = FitMateWhiteBackground.copy(alpha = 0.55f)
                    )
                ) {
                    Text(stringResource(R.string.workout_btn_start), fontWeight = FontWeight.Bold)
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = FitMateCard,
                    border = BorderStroke(1.dp, FitMateBorder)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatElapsedTime(elapsedSeconds),
                            color = FitMateTextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                OutlinedButton(
                    onClick = onStop,
                    enabled = isTimerRunning,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, FitMateRed.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = FitMateRed,
                        disabledContentColor = FitMateRed.copy(alpha = 0.35f)
                    )
                ) {
                    Text(stringResource(R.string.workout_btn_stop), fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = when {
                    workoutComplete -> stringResource(R.string.workout_timer_status_complete)
                    isTimerRunning -> stringResource(R.string.workout_timer_status_live)
                    elapsedSeconds > 0 -> stringResource(R.string.workout_timer_status_paused)
                    else -> stringResource(R.string.workout_timer_status_idle)
                },
                color = FitMateTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun WorkoutStopQuestionnaire(
    selectedReason: StopReason?,
    submitted: Boolean,
    errorMessage: String?,
    onSelect: (StopReason) -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = FitMateCard,
        border = BorderStroke(1.dp, FitMateBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.workout_stop_reason),
                color = FitMateTextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            StopReasonOption(
                text = stringResource(R.string.workout_stop_completed_set),
                selected = selectedReason == StopReason.COMPLETED_SET,
                enabled = !submitted,
                onClick = { onSelect(StopReason.COMPLETED_SET) }
            )
            StopReasonOption(
                text = stringResource(R.string.workout_stop_give_up),
                selected = selectedReason == StopReason.GIVE_UP,
                enabled = !submitted,
                onClick = { onSelect(StopReason.GIVE_UP) }
            )
            errorMessage?.let {
                Text(
                    text = it,
                    color = HardColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = onSubmit,
                enabled = !submitted,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitMateBlue,
                    contentColor = FitMateWhiteBackground,
                    disabledContainerColor = FitMateBlue.copy(alpha = 0.32f),
                    disabledContentColor = FitMateWhiteBackground.copy(alpha = 0.55f)
                )
            ) {
                Text(stringResource(R.string.submit), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StopReasonOption(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.68f
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = FitMateCard,
        border = BorderStroke(1.dp, if (selected) FitMateGreen.copy(alpha = alpha) else FitMateBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (selected) FitMateGreen.copy(alpha = alpha) else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) FitMateGreen.copy(alpha = alpha) else FitMateBorder,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = FitMateTextPrimary.copy(alpha = alpha),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun InstructionsAccordion(
    title: String,
    steps: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = FitMateCard,
        border = BorderStroke(1.dp, FitMateBorder),
        shadowElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.workout_instructions_label),
                        color = FitMateTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (expanded) stringResource(R.string.workout_instructions_tap_collapse) else stringResource(R.string.workout_instructions_tap_expand),
                        color = FitMateTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) stringResource(R.string.workout_instructions_tap_collapse) else stringResource(R.string.workout_instructions_tap_expand),
                    tint = FitMateBlue
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    steps.forEachIndexed { index, step ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = FitMateWhiteBackground,
                            border = BorderStroke(1.dp, FitMateBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(FitMateGreen.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = FitMateGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = step,
                                    color = FitMateTextPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanChoiceDialog(
    onDismiss: () -> Unit,
    onChooseDefault: () -> Unit,
    onChooseCustom: () -> Unit
) {
    NeonDialogShell(
        title = stringResource(R.string.workout_setup_title),
        onDismiss = onDismiss
    ) {
        Text(
            text = stringResource(R.string.workout_no_plan_body),
            color = FitMateTextSecondary
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onChooseDefault,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitMateGreen,
                    contentColor = FitMateWhiteBackground
                )
            ) {
                Text(stringResource(R.string.workout_btn_recommended_plan))
            }
            OutlinedButton(
                onClick = onChooseCustom,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, FitMateBorder)
            ) {
                Text(stringResource(R.string.workout_btn_custom_plan), color = FitMateTextPrimary)
            }
        }
    }
}

@Composable
private fun RecommendedPlanConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    NeonDialogShell(
        title = title,
        onDismiss = onDismiss
    ) {
        Text(
            text = message,
            color = FitMateTextSecondary
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, FitMateBorder)
            ) {
                Text(stringResource(R.string.cancel), color = FitMateTextPrimary)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitMateGreen,
                    contentColor = FitMateWhiteBackground
                )
            ) {
                Text(confirmLabel)
            }
        }
    }
}

@Composable
private fun SequentialPlanBuilderDialog(
    initialSchedule: WeeklyWorkoutSchedule?,
    fallbackSchedule: WeeklyWorkoutSchedule,
    onDismiss: () -> Unit,
    onSave: (WeeklyWorkoutSchedule) -> Unit
) {

    val context = LocalContext.current
    val selections = remember(initialSchedule, fallbackSchedule) {
        mutableStateListOf<WorkoutDaySchedule>().apply {
            addAll(
                (initialSchedule?.days ?: fallbackSchedule.days).sortedBy {
                    workoutWeekdayDisplayIndex[it.weekday] ?: Int.MAX_VALUE
                }
            )
        }
    }
    var stepIndex by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val day = selections[stepIndex]
    val previewImage = focusPreviewImage(day.focus)
    val previewExercises = availableExercisesForFocus(day.focus).take(3)

    NeonDialogShell(
        title = stringResource(R.string.workout_btn_make_custom),
        onDismiss = onDismiss,
        widthFraction = 0.96f,
        heightFraction = 0.92f,
        usePlatformDefaultWidth = false,
        scrollable = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
        ) {
            workoutWeekdaysMondayFirst.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == stepIndex) 10.dp else 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            when {
                                index < stepIndex -> FitMateGreen
                                index == stepIndex -> FitMateBlue
                                else -> FitMateBorder
                            }
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.workout_custom_step, stepIndex + 1, workoutWeekdaysMondayFirst.size),
            color = FitMateBlue,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = day.weekday.label,
            color = FitMateTextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.workout_custom_pick, day.weekday.label),
            color = FitMateTextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(FitMateCard)
        ) {
            if (previewImage.isNotBlank()) {
                AsyncImage(
                    model = assetModel(previewImage),
                    contentDescription = day.focus.label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.60f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = day.focus.label,
                    color = FitMateWhiteBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                if (day.focus != WorkoutFocus.REST && previewExercises.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = previewExercises.joinToString(" • ") { it.name },
                        color = FitMateWhiteBackground.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            customWorkoutFocusOptions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { focus ->
                        FocusChip(
                            label = customFocusTitle(focus),
                            subtitle = customFocusSubtitle(focus),
                            selected = day.focus == focus,
                            onClick = {
                                selections[stepIndex] = WorkoutDaySchedule(
                                    weekday = day.weekday,
                                    focus = focus,
                                    exercises = defaultExercisesForFocus(focus)
                                )
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = it,
                color = HardColor,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    if (stepIndex == 0) {
                        onDismiss()
                    } else {
                        stepIndex--
                    }
                },
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, FitMateBorder)
            ) {
                Text(stringResource(R.string.back), color = FitMateTextPrimary)
            }

            Button(
                onClick = {
                    val restCount = selections.count { it.focus == WorkoutFocus.REST }
                    if (stepIndex < selections.lastIndex) {
                        stepIndex++
                    } else if (restCount < 1) {
                        errorMessage = context.getString(R.string.workout_error_rest_requirement)
                    } else {
                        onSave(
                            WeeklyWorkoutSchedule(
                                days = selections.toList(),
                                isCustom = true,
                                planType = WorkoutPlanType.CUSTOM,
                                generatedForGoal = null,
                                version = fallbackSchedule.version,
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitMateGreen,
                    contentColor = FitMateWhiteBackground
                )
            ) {
                Text(if (stepIndex == selections.lastIndex) stringResource(R.string.workout_btn_save_plan) else stringResource(R.string.workout_btn_next))
            }
        }
    }
}

@Composable
private fun WorkoutDayEditorDialog(
    day: WorkoutDaySchedule,
    onDismiss: () -> Unit,
    onSave: (WorkoutDaySchedule) -> Unit
) {
    val workingFocus = day.focus
    val exerciseStates = remember(day) {
        mutableStateMapOf<String, EditableExerciseState>().apply {
            availableExercisesForFocus(day.focus).forEach { entry ->
                val existing = day.exercises.firstOrNull { it.exerciseName == entry.name }
                this[entry.name] = EditableExerciseState(
                    selected = existing != null,
                    sets = existing?.sets ?: 3,
                    amount = existing?.amount ?: entry.defaultAmount
                )
            }
        }
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    NeonDialogShell(
        title = stringResource(R.string.workout_edit_day_title, day.weekday.label),
        onDismiss = onDismiss,
        widthFraction = 0.96f,
        heightFraction = 0.92f,
        usePlatformDefaultWidth = false,
        scrollable = true
    ) {
        if (workingFocus == WorkoutFocus.REST) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = FitMateCard,
                border = BorderStroke(1.dp, RestAccent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.EventBusy, contentDescription = null, tint = RestAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.workout_rest_day_saved),
                        color = FitMateTextPrimary
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                availableExercisesForFocus(workingFocus).forEach { entry ->
                    val state = exerciseStates[entry.name] ?: return@forEach
                    EditableExerciseCard(
                        entry = entry,
                        state = state,
                        onToggle = {
                            exerciseStates[entry.name] = state.copy(selected = !state.selected)
                            errorMessage = null
                        },
                        onSetsChange = { sets ->
                            exerciseStates[entry.name] = state.copy(sets = sets)
                        },
                        onAmountChange = { amount ->
                            exerciseStates[entry.name] = state.copy(amount = amount)
                        }
                    )
                }
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = it,
                color = HardColor,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cancel), color = FitMateTextSecondary)
            }
            Button(
                onClick = {
                    if (workingFocus == WorkoutFocus.REST) {
                        onSave(
                            WorkoutDaySchedule(
                                weekday = day.weekday,
                                focus = WorkoutFocus.REST,
                                exercises = emptyList()
                            )
                        )
                    } else {
                        val chosen = availableExercisesForFocus(workingFocus)
                            .mapNotNull { entry ->
                                val editable = exerciseStates[entry.name] ?: return@mapNotNull null
                                if (!editable.selected) return@mapNotNull null
                                WorkoutExerciseConfig(
                                    exerciseName = entry.name,
                                    sets = editable.sets,
                                    amount = editable.amount
                                )
                            }
                        if (chosen.isEmpty()) {
                            errorMessage = context.getString(R.string.workout_error_no_exercises)
                        } else {
                            onSave(
                                WorkoutDaySchedule(
                                    weekday = day.weekday,
                                    focus = workingFocus,
                                    exercises = chosen
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitMateGreen,
                    contentColor = FitMateWhiteBackground
                )
            ) {
                Text(stringResource(R.string.workout_btn_save_edits))
            }
        }
    }
}

@Composable
private fun EditableExerciseCard(
    entry: ExerciseLibraryEntry,
    state: EditableExerciseState,
    onToggle: () -> Unit,
    onSetsChange: (Int) -> Unit,
    onAmountChange: (Int) -> Unit
) {
    val workload = state.sets * state.amount
    val band = difficultyBand(entry, workload)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (state.selected) FitMateCard else FitMateWhiteBackground,
        border = BorderStroke(1.dp, if (state.selected) FitMateBlue.copy(alpha = 0.5f) else FitMateBorder),
        shadowElevation = if (state.selected) 2.dp else 0.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (state.selected) FitMateGreen else Color.Transparent)
                        .border(1.dp, if (state.selected) FitMateGreen else FitMateBorder, RoundedCornerShape(10.dp))
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.selected) {
                        Text(stringResource(R.string.step_done), color = FitMateWhiteBackground, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                DifficultyPill(band = band)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = assetModel(entry.postureImage),
                    contentDescription = entry.name,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(FitMateCard),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(FitMateCard),
                    error = ColorPainter(FitMateCard),
                    fallback = ColorPainter(FitMateCard),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        color = FitMateTextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.instructions,
                        color = FitMateTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (state.selected) {
                Spacer(modifier = Modifier.height(16.dp))
                WorkoutAmountSlider(
                    title = stringResource(R.string.workout_label_sets),
                    value = state.sets,
                    range = 1..6,
                    unit = stringResource(R.string.workout_unit_sets),
                    onValueChange = onSetsChange
                )
                Spacer(modifier = Modifier.height(14.dp))
                WorkoutAmountSlider(
                    title = if (entry.metricType == ExerciseMetricType.REPS) stringResource(R.string.workout_label_reps) else stringResource(R.string.workout_label_duration),
                    value = state.amount,
                    range = entry.minAmount..entry.maxAmount,
                    unit = if (entry.metricType == ExerciseMetricType.REPS) stringResource(R.string.workout_unit_reps) else stringResource(R.string.workout_unit_sec),
                    onValueChange = onAmountChange
                )
            }
        }
    }
}

@Composable
private fun WorkoutAmountSlider(
    title: String,
    value: Int,
    range: IntRange,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Column {
        Text(
            text = title,
            color = FitMateTextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value.toString(),
                color = FitMateTextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = unit,
                color = FitMateTextSecondary,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first - 1).coerceAtLeast(0),
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = FitMateGreen,
                activeTrackColor = FitMateGreen,
                inactiveTrackColor = FitMateBorder
            )
        )
    }
}

@Composable
private fun DifficultyPill(
    band: DifficultyBand
) {
    val color = when (band) {
        DifficultyBand.EASY -> EasyColor
        DifficultyBand.MEDIUM -> MediumColor
        DifficultyBand.HARD -> HardColor
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = when(band) {
                DifficultyBand.EASY -> stringResource(R.string.difficulty_easy)
                DifficultyBand.MEDIUM -> stringResource(R.string.difficulty_medium)
                DifficultyBand.HARD -> stringResource(R.string.difficulty_hard)
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FocusChip(
    label: String,
    subtitle: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) FitMateGreen.copy(alpha = 0.10f) else FitMateWhiteBackground,
        border = BorderStroke(1.dp, if (selected) FitMateGreen else FitMateBorder),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = if (selected) FitMateGreen else FitMateTextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    color = if (selected) {
                        FitMateGreen.copy(alpha = 0.78f)
                    } else {
                        FitMateTextSecondary
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NeonDialogShell(
    title: String,
    onDismiss: () -> Unit,
    widthFraction: Float = 0.92f,
    heightFraction: Float? = null,
    usePlatformDefaultWidth: Boolean = true,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = usePlatformDefaultWidth)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .then(
                    heightFraction?.let { Modifier.fillMaxHeight(it) }
                        ?: Modifier.heightIn(max = 820.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = FitMateSurface),
            border = BorderStroke(1.dp, FitMateBorder),
            shape = RoundedCornerShape(30.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            val bodyModifier = if (scrollable) {
                Modifier.verticalScroll(rememberScrollState())
            } else {
                Modifier
            }
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .then(bodyModifier),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = title,
                    color = FitMateTextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(18.dp))
                content()
            }
        }
    }
}

private fun availableExercisesForFocus(
    focus: WorkoutFocus
): List<ExerciseLibraryEntry> {
    return LocalExerciseCatalog.forFocus(focus)
        .mapNotNull { LocalExerciseDatabase.exerciseByName(it.name) }
}

private fun customFocusTitle(focus: WorkoutFocus): String = when (focus) {
    WorkoutFocus.PUSH -> "PUSH"
    WorkoutFocus.PULL -> "PULL"
    else -> focus.label
}

@Composable
private fun customFocusSubtitle(focus: WorkoutFocus): String? = when (focus) {
    WorkoutFocus.PUSH -> stringResource(R.string.workout_focus_push_sub)
    WorkoutFocus.PULL -> stringResource(R.string.workout_focus_pull_sub)
    else -> null
}

private fun defaultExercisesForFocus(
    focus: WorkoutFocus
): List<WorkoutExerciseConfig> {
    return availableExercisesForFocus(focus).take(2).map {
        WorkoutExerciseConfig(
            exerciseName = it.name,
            sets = 3,
            amount = it.defaultAmount
        )
    }
}

private fun focusPreviewImage(
    focus: WorkoutFocus
): String {
    return when (focus) {
        WorkoutFocus.PUSH -> "chest_press.gif"
        WorkoutFocus.PULL -> "bench_press.gif"
        WorkoutFocus.CHEST_BICEPS -> "pushup.gif"
        WorkoutFocus.BACK_REAR_DELTS -> "bench_press.gif"
        WorkoutFocus.LEGS -> "lifting_weights.gif"
        WorkoutFocus.SHOULDERS_TRICEPS -> "chest_press.gif"
        WorkoutFocus.CORE_CONDITIONING -> "gym_buddy.gif"
        WorkoutFocus.CONDITIONING -> "gym_buddy.gif"
        WorkoutFocus.FULL_BODY -> "lifting_weights.gif"
        WorkoutFocus.MOBILITY -> "gym_buddy.gif"
        WorkoutFocus.ARMS_ABS -> "gym_buddy.gif"
        WorkoutFocus.LEGS_SHOULDERS -> "lifting_weights.gif"
        WorkoutFocus.UPPER_BODY_POWER -> "bench_press.gif"
        WorkoutFocus.ARM_SPECIALIZATION_WEAK_POINTS -> "chest_press.gif"
        WorkoutFocus.REST -> ""
    }
}

private fun formatAmount(
    entry: ExerciseLibraryEntry,
    amount: Int
): String {
    return if (entry.metricType == ExerciseMetricType.SECONDS) {
        "$amount sec"
    } else {
        "$amount reps"
    }
}

private fun assetModel(
    assetName: String
): String? {
    return assetName.takeIf { it.isNotBlank() }?.let { "file:///android_asset/exercises/$it" }
}

private fun detailAssetModel(
    assetName: String
): String? {
    return assetName.takeIf { it.isNotBlank() }?.let { "file:///android_asset/workout_details/gifs/$it" }
}

private fun loadAssetText(
    context: Context,
    assetPath: String
): String {
    return runCatching {
        context.assets.open(assetPath).bufferedReader().use { it.readText() }
    }.getOrDefault("")
}

private fun parseInstructionMarkdown(
    raw: String
): ParsedInstructions {
    val lines = raw
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val title = lines.firstOrNull()
        ?.removePrefix("#")
        ?.removeSuffix(":")
        ?.trim()
        .orEmpty()

    val steps = lines
        .drop(1)
        .map { line ->
            line.replace(Regex("^\\d+\\.\\s*"), "").trim()
        }
        .filter { it.isNotBlank() }

    return ParsedInstructions(
        title = title,
        steps = steps
    )
}

private fun difficultyBand(
    entry: ExerciseLibraryEntry,
    workload: Int
): DifficultyBand {
    return when {
        workload <= entry.easyMaxWorkload -> DifficultyBand.EASY
        workload <= entry.mediumMaxWorkload -> DifficultyBand.MEDIUM
        else -> DifficultyBand.HARD
    }
}

private fun formatElapsedTime(
    totalSeconds: Int
): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private fun todayProgressFor(
    weekday: WorkoutWeekday,
    exerciseName: String,
    workoutLogs: List<WorkoutDayLog>
): WorkoutExerciseProgress? {
    return workoutLogs.firstOrNull {
        it.date == java.time.LocalDate.now() && it.weekday == weekday
    }?.exercises?.firstOrNull { it.exerciseName == exerciseName }
}

private data class SelectedExerciseDetail(
    val exercise: ExerciseLibraryEntry,
    val config: WorkoutExerciseConfig,
    val weekday: WorkoutWeekday,
    val focus: WorkoutFocus,
)

private data class EditableExerciseState(
    val selected: Boolean,
    val sets: Int,
    val amount: Int,
)

private data class ParsedInstructions(
    val title: String,
    val steps: List<String>,
)

private enum class DifficultyBand {
    EASY,
    MEDIUM,
    HARD,
}

private enum class StopReason {
    COMPLETED_SET,
    GIVE_UP,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeroGifCarousel() {
    val heroGifs = remember {
        listOf(
            "gym_buddy.gif",
            "doggo.gif",
            "chest_press.gif",
            "bench_press.gif",
            "lifting_weights.gif"
        )
    }

    val pageCount = 10000
    val pagerState = rememberPagerState(
        initialPage = 5000,
        pageCount = { pageCount }
    )

    LaunchedEffect(pagerState) {
        while (true) {
            delay(3500)
            pagerState.animateScrollToPage(
                page = pagerState.currentPage + 1,
                animationSpec = tween(durationMillis = 800)
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FitMateCard),
        border = BorderStroke(1.dp, FitMateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) { page ->
                val actualIndex = page % heroGifs.size
                AsyncImage(
                    model = assetModel(heroGifs[actualIndex]),
                    contentDescription = stringResource(R.string.workout_weekly_title),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.70f)
                            ),
                            startY = 400f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(22.dp)
            ) {
                Text(
                    text = stringResource(R.string.workout_weekly_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = FitMateGreenLight,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.workout_weekly_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitMateWhiteBackground.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun MotivationBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FitMateCard),
        border = BorderStroke(1.dp, FitMateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = assetModel("gym_motivation.gif"),
                contentDescription = stringResource(R.string.workout_discipline_quote),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.45f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(22.dp)
            ) {
                Text(
                    text = stringResource(R.string.workout_discipline_quote),
                    style = MaterialTheme.typography.titleLarge,
                    color = FitMateWhiteBackground,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.workout_discipline_sub),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitMateWhiteBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}