package com.fitmate.ui.workout

import android.os.Build
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx. compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.LocalImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.fitmate.data.LocalExerciseDatabase
import com.fitmate.domain.model.ExerciseLibraryEntry
import com.fitmate.domain.model.ExerciseMetricType
import com.fitmate.domain.model.WorkoutDayLog
import com.fitmate.domain.model.WeeklyWorkoutSchedule
import com.fitmate.domain.model.WorkoutDaySchedule
import com.fitmate.domain.model.WorkoutExerciseConfig
import com.fitmate.domain.model.WorkoutExerciseProgress
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.model.WorkoutWeekday
import com.fitmate.ui.viewmodel.CampusFitUiState
import com.fitmate.ui.viewmodel.CampusFitViewModel

private val FitMateBlack = Color(0xFF0F0F11)
private val FitMateDarkGrey = Color(0xFF1C1C1E)
private val FitMateEmerald = Color(0xFF00E676)
private val FitMateWhite = Color(0xFFFFFFFF)
private val FitMateGlass = Color(0xFFFFFFFF).copy(alpha = 0.08f)
private val FitMateGlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f)
private val FitMateBlue = Color(0xFF00E5FF)
private val RestAccent = Color(0xFFFFC857)
private val EasyColor = Color(0xFF00E676)
private val MediumColor = Color(0xFFFFB020)
private val HardColor = Color(0xFFFF5A5F)
private val FitMateRed = Color(0xFFFF5252)

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

    if (showPlanChoiceDialog) {
        PlanChoiceDialog(
            onDismiss = { showPlanChoiceDialog = false },
            onChooseDefault = {
                viewModel.saveWorkoutSchedule(createDefaultWorkoutSchedule())
                showPlanChoiceDialog = false
            },
            onChooseCustom = {
                showPlanChoiceDialog = false
                showPlanBuilder = true
            }
        )
    }

    CompositionLocalProvider(LocalImageLoader provides gifImageLoader) {

    if (showPlanBuilder) {
        SequentialPlanBuilderDialog(
            initialSchedule = schedule,
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
                val currentSchedule = state.workoutSchedule ?: createDefaultWorkoutSchedule()
                viewModel.saveWorkoutSchedule(
                    currentSchedule.copy(
                        days = currentSchedule.days.map {
                            if (it.weekday == updatedDay.weekday) updatedDay else it
                        }
                    )
                )
                editingDay = null
            }
        )
    }

    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            background = FitMateBlack,
            surface = FitMateDarkGrey,
            primary = FitMateEmerald,
            onPrimary = FitMateBlack,
            onBackground = FitMateWhite,
            onSurface = FitMateWhite
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
                containerColor = FitMateBlack,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = FitMateBlack,
                            titleContentColor = FitMateWhite
                        ),
                        title = {
                            Column {
                                Text(
                                    text = "Workout Plan",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Make your split flexible, visual, and easy to update.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = FitMateWhite.copy(alpha = 0.65f)
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    state = workoutListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FitMateBlack)
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
                            onCustomize = { showPlanBuilder = true }
                        )
                    }

                    schedule?.days?.let { days ->
                        items(days, key = { it.weekday.name }) { day ->
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
    onCustomize: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FitMateDarkGrey),
        border = BorderStroke(1.dp, FitMateGlassBorder),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = if (isCustom) "Custom weekly split active" else "Build your weekly split",
                style = MaterialTheme.typography.headlineSmall,
                color = FitMateWhite,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Choose what to train on each weekday, keep at least one rest day, and tune every exercise with slider-based set and rep controls.",
                color = FitMateWhite.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onCustomize,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitMateEmerald,
                    contentColor = FitMateBlack
                )
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Make custom plan", fontWeight = FontWeight.Bold)
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
        colors = CardDefaults.cardColors(containerColor = FitMateDarkGrey),
        border = BorderStroke(1.dp, if (isRestDay) RestAccent.copy(alpha = 0.6f) else FitMateGlassBorder),
        shape = RoundedCornerShape(26.dp)
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
                                    listOf(FitMateDarkGrey, Color(0xFF101D17))
                                )
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, FitMateBlack.copy(alpha = 0.25f), FitMateBlack.copy(alpha = 0.92f))
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
                        color = if (isRestDay) RestAccent else FitMateBlue,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = day.focus.label,
                        color = FitMateWhite,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (!isRestDay && day.exercises.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${day.exercises.size} exercises",
                            color = FitMateEmerald.copy(alpha = 0.85f),
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
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Icon(
                        imageVector = if (collapsed) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess,
                        contentDescription = if (collapsed) "Expand ${day.weekday.label}" else "Collapse ${day.weekday.label}",
                        tint = FitMateWhite
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit ${day.weekday.label}",
                        tint = FitMateWhite
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
                            text = "Rest day selected by you. Recovery, mobility, and lighter movement live here.",
                            color = FitMateWhite.copy(alpha = 0.72f)
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
        color = FitMateGlass,
        border = BorderStroke(1.dp, FitMateGlassBorder)
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
                        .background(FitMateBlack),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        color = FitMateWhite,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${config.sets} sets • ${formatAmount(entry, config.amount)}",
                        color = FitMateBlue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = entry.instructions,
                        color = FitMateWhite.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onShowInstructions,
                        border = BorderStroke(1.dp, FitMateGlassBorder),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Open", color = FitMateWhite)
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
                                color = FitMateEmerald
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Exercise completed",
                                        tint = FitMateWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Completed",
                                color = FitMateEmerald,
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
    val parsed = remember(markdown) { parseInstructionMarkdown(markdown) }
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
        containerColor = FitMateBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FitMateBlack,
                    titleContentColor = FitMateWhite
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = FitMateWhite
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
                            text = "Exercise instructions",
                            style = MaterialTheme.typography.labelMedium,
                            color = FitMateWhite.copy(alpha = 0.65f)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FitMateBlack)
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FitMateDarkGrey),
                    border = BorderStroke(1.dp, FitMateGlassBorder),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column {
                        AsyncImage(
                            model = detailAssetModel(exercise.detailGifAsset),
                            contentDescription = exercise.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(FitMateBlack),
                            contentScale = ContentScale.Fit
                        )
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${detail.config.sets} sets • ${formatAmount(exercise, detail.config.amount)}",
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
                                                questionnaireError = "Select a reason before submitting."
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
                                title = parsed.title.ifBlank { "${exercise.name} instructions" },
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
        color = FitMateGlass,
        border = BorderStroke(1.dp, FitMateGlassBorder)
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
                    text = "Set progress",
                    color = FitMateWhite,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$completedSets / $totalSets sets",
                    color = if (completedSets >= totalSets) FitMateEmerald else FitMateBlue,
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
                color = if (completedSets >= totalSets) FitMateEmerald else FitMateBlue,
                trackColor = FitMateWhite.copy(alpha = 0.1f)
            )
            Text(
                text = if (completedSets >= totalSets) {
                    "You’ve completed every planned set for this exercise."
                } else {
                    "Complete a set, stop the timer, and log the outcome to move this bar forward."
                },
                color = FitMateWhite.copy(alpha = 0.68f),
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
        color = FitMateGlass,
        border = BorderStroke(1.dp, FitMateGlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Workout timer",
                color = FitMateWhite,
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
                        containerColor = FitMateEmerald,
                        contentColor = FitMateBlack,
                        disabledContainerColor = FitMateEmerald.copy(alpha = 0.3f),
                        disabledContentColor = FitMateBlack.copy(alpha = 0.55f)
                    )
                ) {
                    Text("Start Workout", fontWeight = FontWeight.Bold)
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Black.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, FitMateGlassBorder)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatElapsedTime(elapsedSeconds),
                            color = FitMateWhite,
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
                    Text("Stop Workout", fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = when {
                    workoutComplete -> "All planned sets are complete for this exercise."
                    isTimerRunning -> "Timer is live. Stop it when the current set ends."
                    elapsedSeconds > 0 -> "Timer paused. Submit the reason below to log this round."
                    else -> "Start the timer when you begin your current set."
                },
                color = FitMateWhite.copy(alpha = 0.68f),
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
        color = FitMateGlass,
        border = BorderStroke(1.dp, FitMateGlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Why did you stop the timer?",
                color = FitMateWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            StopReasonOption(
                text = "Completed my current set",
                selected = selectedReason == StopReason.COMPLETED_SET,
                enabled = !submitted,
                onClick = { onSelect(StopReason.COMPLETED_SET) }
            )
            StopReasonOption(
                text = "Can't continue anymore, I give up :(",
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
                    contentColor = FitMateBlack,
                    disabledContainerColor = FitMateBlue.copy(alpha = 0.32f),
                    disabledContentColor = FitMateBlack.copy(alpha = 0.55f)
                )
            ) {
                Text("Submit", fontWeight = FontWeight.Bold)
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
        color = Color.Black.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, if (selected) FitMateEmerald.copy(alpha = alpha) else FitMateGlassBorder)
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
                        if (selected) FitMateEmerald.copy(alpha = alpha) else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) FitMateEmerald.copy(alpha = alpha) else FitMateWhite.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = FitMateWhite.copy(alpha = alpha),
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
        color = FitMateGlass,
        border = BorderStroke(1.dp, FitMateGlassBorder)
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
                        text = "Instructions",
                        color = FitMateWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (expanded) "Tap to collapse" else "Tap to view step-by-step instructions",
                        color = FitMateWhite.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse instructions" else "Expand instructions",
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
                            color = Color.Black.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, FitMateGlassBorder)
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
                                        .background(FitMateEmerald.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = FitMateEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = step,
                                    color = FitMateWhite.copy(alpha = 0.85f),
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
        title = "Choose your workout setup",
        onDismiss = onDismiss
    ) {
        Text(
            text = "Start with the default weekly split or build your own schedule day by day.",
            color = FitMateWhite.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onChooseDefault,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitMateEmerald,
                    contentColor = FitMateBlack
                )
            ) {
                Text("Default plan")
            }
            OutlinedButton(
                onClick = onChooseCustom,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, FitMateGlassBorder)
            ) {
                Text("Custom plan", color = FitMateWhite)
            }
        }
    }
}

@Composable
private fun SequentialPlanBuilderDialog(
    initialSchedule: WeeklyWorkoutSchedule?,
    onDismiss: () -> Unit,
    onSave: (WeeklyWorkoutSchedule) -> Unit
) {
    val selections = remember(initialSchedule) {
        mutableStateListOf<WorkoutDaySchedule>().apply {
            addAll(initialSchedule?.days ?: createDefaultWorkoutSchedule().days)
        }
    }
    var stepIndex by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val day = selections[stepIndex]
    val previewImage = focusPreviewImage(day.focus)
    val previewExercises = availableExercisesForFocus(day.focus).take(3)

    NeonDialogShell(
        title = "Make custom plan",
        onDismiss = onDismiss,
        scrollable = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
        ) {
            WorkoutWeekday.entries.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == stepIndex) 10.dp else 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            when {
                                index < stepIndex -> FitMateEmerald
                                index == stepIndex -> FitMateBlue
                                else -> FitMateGlassBorder
                            }
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Step ${stepIndex + 1} of ${WorkoutWeekday.entries.size}",
            color = FitMateBlue,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = day.weekday.label,
            color = FitMateWhite,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Pick what you want to train on ${day.weekday.label}. Workouts can repeat, and at least one day must stay as rest.",
            color = FitMateWhite.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(FitMateGlass)
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
                            listOf(Color.Transparent, FitMateBlack.copy(alpha = 0.88f))
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
                    color = FitMateWhite,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                if (day.focus != WorkoutFocus.REST && previewExercises.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = previewExercises.joinToString(" • ") { it.name },
                        color = FitMateWhite.copy(alpha = 0.78f),
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
            WorkoutFocus.entries.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { focus ->
                        FocusChip(
                            label = focus.label,
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
                    if (stepIndex > 0) stepIndex--
                },
                modifier = Modifier.weight(1f),
                enabled = stepIndex > 0,
                border = BorderStroke(1.dp, FitMateGlassBorder)
            ) {
                Text("Back", color = FitMateWhite)
            }

            Button(
                onClick = {
                    val restCount = selections.count { it.focus == WorkoutFocus.REST }
                    if (stepIndex < selections.lastIndex) {
                        stepIndex++
                    } else if (restCount < 1) {
                        errorMessage = "Please keep at least 1 rest day in your week."
                    } else {
                        onSave(
                            WeeklyWorkoutSchedule(
                                days = selections.toList(),
                                isCustom = true
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitMateEmerald,
                    contentColor = FitMateBlack
                )
            ) {
                Text(if (stepIndex == selections.lastIndex) "Save plan" else "Next")
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
    var workingFocus by remember(day) { mutableStateOf(day.focus) }
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

    NeonDialogShell(
        title = "Edit ${day.weekday.label}",
        onDismiss = onDismiss,
        widthFraction = 0.95f,
        scrollable = true
    ) {
        Text(
            text = "Pick the day's focus, then tune each exercise with sliders. Difficulty updates live as you change the load.",
            color = FitMateWhite.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(18.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WorkoutFocus.entries.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { focus ->
                        FocusChip(
                            label = focus.label,
                            selected = workingFocus == focus,
                            onClick = {
                                workingFocus = focus
                                exerciseStates.clear()
                                availableExercisesForFocus(focus).forEach { entry ->
                                    val existing = day.exercises.firstOrNull { it.exerciseName == entry.name }
                                    exerciseStates[entry.name] = EditableExerciseState(
                                        selected = existing != null,
                                        sets = existing?.sets ?: 3,
                                        amount = existing?.amount ?: entry.defaultAmount
                                    )
                                }
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

        Spacer(modifier = Modifier.height(18.dp))

        if (workingFocus == WorkoutFocus.REST) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = FitMateGlass,
                border = BorderStroke(1.dp, RestAccent.copy(alpha = 0.6f))
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
                        text = "This day will be saved as a rest day.",
                        color = FitMateWhite
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
                Text("Cancel", color = FitMateWhite)
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
                            errorMessage = "Select at least one exercise, or switch this day to rest."
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
                    containerColor = FitMateEmerald,
                    contentColor = FitMateBlack
                )
            ) {
                Text("Save edits")
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
        color = if (state.selected) FitMateGlass else Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, if (state.selected) FitMateBlue.copy(alpha = 0.5f) else FitMateGlassBorder)
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
                        .background(if (state.selected) FitMateEmerald else Color.Transparent)
                        .border(1.dp, if (state.selected) FitMateEmerald else FitMateGlassBorder, RoundedCornerShape(10.dp))
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.selected) {
                        Text("✓", color = FitMateBlack, fontWeight = FontWeight.Bold)
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
                        .background(FitMateBlack),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        color = FitMateWhite,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.instructions,
                        color = FitMateWhite.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (state.selected) {
                Spacer(modifier = Modifier.height(16.dp))
                WorkoutAmountSlider(
                    title = "Sets",
                    value = state.sets,
                    range = 1..6,
                    unit = "sets",
                    onValueChange = onSetsChange
                )
                Spacer(modifier = Modifier.height(14.dp))
                WorkoutAmountSlider(
                    title = if (entry.metricType == ExerciseMetricType.REPS) "Reps" else "Duration",
                    value = state.amount,
                    range = entry.minAmount..entry.maxAmount,
                    unit = entry.metricType.unitLabel,
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
            color = FitMateWhite.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value.toString(),
                color = FitMateWhite,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = unit,
                color = FitMateWhite.copy(alpha = 0.55f),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first - 1).coerceAtLeast(0),
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = FitMateEmerald,
                activeTrackColor = FitMateEmerald,
                inactiveTrackColor = FitMateGlassBorder
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
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = band.label,
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
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) FitMateEmerald.copy(alpha = 0.15f) else FitMateGlass,
        border = BorderStroke(1.dp, if (selected) FitMateEmerald else FitMateGlassBorder),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            color = if (selected) FitMateEmerald else FitMateWhite,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NeonDialogShell(
    title: String,
    onDismiss: () -> Unit,
    widthFraction: Float = 0.92f,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .heightIn(max = 820.dp),
            colors = CardDefaults.cardColors(containerColor = FitMateDarkGrey),
            border = BorderStroke(1.dp, FitMateGlassBorder),
            shape = RoundedCornerShape(30.dp)
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
                    color = FitMateWhite,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(18.dp))
                content()
            }
        }
    }
}

private fun createDefaultWorkoutSchedule(): WeeklyWorkoutSchedule {
    val focusByDay = listOf(
        WorkoutWeekday.SUNDAY to WorkoutFocus.CHEST_BICEPS,
        WorkoutWeekday.MONDAY to WorkoutFocus.BACK_REAR_DELTS,
        WorkoutWeekday.TUESDAY to WorkoutFocus.LEGS,
        WorkoutWeekday.WEDNESDAY to WorkoutFocus.SHOULDERS_TRICEPS,
        WorkoutWeekday.THURSDAY to WorkoutFocus.CORE_CONDITIONING,
        WorkoutWeekday.FRIDAY to WorkoutFocus.REST,
        WorkoutWeekday.SATURDAY to WorkoutFocus.REST,
    )

    return WeeklyWorkoutSchedule(
        days = focusByDay.map { (weekday, focus) ->
            WorkoutDaySchedule(
                weekday = weekday,
                focus = focus,
                exercises = defaultExercisesForFocus(focus)
            )
        },
        isCustom = false
    )
}

private fun availableExercisesForFocus(
    focus: WorkoutFocus
): List<ExerciseLibraryEntry> {
    val names = when (focus) {
        WorkoutFocus.CHEST_BICEPS -> listOf(
            "Push-Ups",
            "Cable Chest Press",
            "Butterfly",
            "Incline Inner Biceps Curls",
        )
        WorkoutFocus.BACK_REAR_DELTS -> listOf(
            "Barbell Rear Delt Row",
            "Elevated Cable Rows",
            "Deadlift with Bands",
            "Dynamic Back Stretch",
        )
        WorkoutFocus.LEGS -> listOf(
            "Hack Squat",
            "Barbell Lunge",
            "Elevated Back Lunge",
            "Cable Hip Adduction",
        )
        WorkoutFocus.SHOULDERS_TRICEPS -> listOf(
            "Shoulder Raise",
            "Body Tricep Press",
            "Tricep Extension",
            "Bench Dips",
        )
        WorkoutFocus.CORE_CONDITIONING -> listOf(
            "Cable Crunch",
            "Decline Reverse Crunch",
            "Bent Knee Hip Raise",
            "Battling Ropes",
            "Bottoms Up",
            "Mountain Climber",
            "Jumping Jack",
            "Plank",
            "Sit-Up",
        )
        WorkoutFocus.REST -> emptyList()
    }
    return names.mapNotNull(LocalExerciseDatabase::exerciseByName)
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
        WorkoutFocus.CHEST_BICEPS -> "pushup.gif"
        WorkoutFocus.BACK_REAR_DELTS -> "bench_press.gif"
        WorkoutFocus.LEGS -> "lifting_weights.gif"
        WorkoutFocus.SHOULDERS_TRICEPS -> "chest_press.gif"
        WorkoutFocus.CORE_CONDITIONING -> "gym_buddy.gif"
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

private enum class DifficultyBand(
    val label: String
) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
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
        colors = CardDefaults.cardColors(containerColor = FitMateBlack),
        border = BorderStroke(1.dp, FitMateGlassBorder)
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
                    contentDescription = "Workout Carousel",
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
                                FitMateBlack.copy(alpha = 0.4f),
                                FitMateBlack.copy(alpha = 0.95f)
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
                    text = "Weekly Training",
                    style = MaterialTheme.typography.titleLarge,
                    color = FitMateBlue,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your personalized weekly workout split",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitMateWhite.copy(alpha = 0.7f)
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
        colors = CardDefaults.cardColors(containerColor = FitMateGlass),
        border = BorderStroke(1.dp, FitMateGlassBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = assetModel("gym_motivation.gif"),
                contentDescription = "Motivation",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.45f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, FitMateBlack.copy(alpha = 0.9f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(22.dp)
            ) {
                Text(
                    text = "DISCIPLINE BEATS MOTIVATION",
                    style = MaterialTheme.typography.titleLarge,
                    color = FitMateWhite,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stay Consistent. Trust The Process.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitMateWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}
