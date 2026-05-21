package com.fitmate.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitmate.R
import com.fitmate.domain.model.AiConfig
import com.fitmate.domain.model.AiProviderMode
import com.fitmate.domain.model.AppThemeMode
import com.fitmate.domain.model.FoodPreference
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.MealSlot
import com.fitmate.domain.model.UserProfile
import com.fitmate.ui.theme.CampusFitTheme

private enum class HomeTab(val label: String) {
    DASHBOARD("Dashboard"),
    MEALS("Meals"),
    DIET("Diet"),
    WORKOUT("Workout"),
    PROGRESS("Progress"),
    SETTINGS("Settings"),
}

private enum class IntroStage {
    WELCOME,
    QUESTIONS,
    PERSONALIZING,
    HOME,
}

private enum class OnboardingStep(val number: String, val prompt: String) {
    HEIGHT("01", "May I know your height?"),
    WEIGHT("02", "How about whispering me your weight?"),
    AGE("03", "And your age?"),
    WORKOUT("04", "How much time can you give to workouts?"),
    GENDER("05", "Which gender option fits you best?"),
    GOAL("06", "What kind of result are we building toward?"),
    FOOD("07", "What kind of food do you usually prefer?"),
    AI_PROVIDER("08", "How should FitMate talk to your AI coach?"),
    AI_BASE_URL("09", "Where should FitMate send the AI requests?"),
    AI_API_KEY("10", "What API key should FitMate use?"),
    AI_MODEL("11", "Which model should power your plan?"),
    LOCAL_ENDPOINT("12", "What local endpoint should FitMate use?"),
    LOCAL_MODEL("13", "Which local model should FitMate call?"),
}

@Composable
fun CampusFitApp(viewModel: CampusFitViewModel = viewModel(factory = CampusFitViewModel.Factory)) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val personalizationState by viewModel.personalizationState.collectAsStateWithLifecycle()
    var stage by rememberSaveable { mutableStateOf(if (uiState.setupCompleted) IntroStage.HOME else IntroStage.WELCOME) }
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.DASHBOARD) }

    if (uiState.setupCompleted && stage != IntroStage.HOME && !personalizationState.isRunning) {
        stage = IntroStage.HOME
    }
    if (personalizationState.isRunning) {
        stage = IntroStage.PERSONALIZING
    }

    CampusFitTheme(darkTheme = uiState.themeMode == AppThemeMode.DARK) {
        when (stage) {
            IntroStage.WELCOME -> WelcomeScreen { stage = IntroStage.QUESTIONS }
            IntroStage.QUESTIONS -> FitMateOnboarding(
                initialProfile = uiState.profile,
                initialConfig = uiState.aiConfig,
                onStartPersonalization = { profile, config -> viewModel.bootstrapPersonalization(profile, config) },
            )
            IntroStage.PERSONALIZING -> PersonalizingScreen(personalizationState) { stage = IntroStage.QUESTIONS }
            IntroStage.HOME -> HomeScreen(
                state = uiState,
                viewModel = viewModel,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        }
    }
}

@Composable
private fun WelcomeScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background)))
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Surface(modifier = Modifier.size(160.dp), shape = RoundedCornerShape(42.dp), tonalElevation = 8.dp, shadowElevation = 10.dp) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "FitMate logo",
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Text("FitMate", style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
            Text(
                "Fitness that fits your routine.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onGetStarted, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth(0.74f)) {
                Text("Get Started", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun FitMateOnboarding(
    initialProfile: UserProfile,
    initialConfig: AiConfig,
    onStartPersonalization: (UserProfile, AiConfig) -> Unit,
) {
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var height by rememberSaveable { mutableStateOf(initialProfile.heightCm.toString()) }
    var weight by rememberSaveable { mutableStateOf(initialProfile.weightKg.toString()) }
    var age by rememberSaveable { mutableStateOf(initialProfile.age.toString()) }
    var workoutMinutes by rememberSaveable { mutableStateOf(initialProfile.workoutMinutes.toString()) }
    var gender by rememberSaveable { mutableStateOf(initialProfile.gender) }
    var goal by rememberSaveable { mutableStateOf(initialProfile.goal) }
    var foodPreference by rememberSaveable { mutableStateOf(initialProfile.foodPreference) }
    var providerMode by rememberSaveable { mutableStateOf(initialConfig.providerMode) }
    var baseUrl by rememberSaveable { mutableStateOf(initialConfig.baseUrl) }
    var apiKey by rememberSaveable { mutableStateOf(initialConfig.apiKey) }
    var modelName by rememberSaveable { mutableStateOf(initialConfig.modelName) }
    var localEndpoint by rememberSaveable { mutableStateOf(initialConfig.localEndpoint) }
    var localModelName by rememberSaveable { mutableStateOf(initialConfig.localModelName) }
    var showValidation by rememberSaveable { mutableStateOf(false) }

    val steps = buildList {
        addAll(
            listOf(
                OnboardingStep.HEIGHT,
                OnboardingStep.WEIGHT,
                OnboardingStep.AGE,
                OnboardingStep.WORKOUT,
                OnboardingStep.GENDER,
                OnboardingStep.GOAL,
                OnboardingStep.FOOD,
                OnboardingStep.AI_PROVIDER,
            ),
        )
        if (providerMode == AiProviderMode.REMOTE_API) {
            add(OnboardingStep.AI_BASE_URL)
            add(OnboardingStep.AI_API_KEY)
            add(OnboardingStep.AI_MODEL)
        } else {
            add(OnboardingStep.LOCAL_ENDPOINT)
            add(OnboardingStep.LOCAL_MODEL)
        }
    }

    val current = steps[stepIndex]
    val error = when (current) {
        OnboardingStep.HEIGHT -> requireField(height)
        OnboardingStep.WEIGHT -> requireField(weight)
        OnboardingStep.AGE -> requireField(age)
        OnboardingStep.WORKOUT -> requireField(workoutMinutes)
        OnboardingStep.AI_BASE_URL -> requireField(baseUrl)
        OnboardingStep.AI_API_KEY -> requireField(apiKey)
        OnboardingStep.AI_MODEL -> requireField(modelName)
        OnboardingStep.LOCAL_ENDPOINT -> requireField(localEndpoint)
        OnboardingStep.LOCAL_MODEL -> requireField(localModelName)
        else -> null
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    if (error != null) {
                        showValidation = true
                        return@Button
                    }
                    showValidation = false
                    if (stepIndex < steps.lastIndex) {
                        stepIndex += 1
                    } else {
                        onStartPersonalization(
                            initialProfile.copy(
                                age = age.toInt(),
                                heightCm = height.toInt(),
                                weightKg = weight.toInt(),
                                gender = gender,
                                goal = goal,
                                foodPreference = foodPreference,
                                workoutMinutes = workoutMinutes.toInt(),
                            ),
                            AiConfig(
                                providerMode = providerMode,
                                baseUrl = baseUrl,
                                apiKey = apiKey,
                                modelName = modelName,
                                localEndpoint = localEndpoint,
                                localModelName = localModelName,
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(24.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text(if (stepIndex == steps.lastIndex) "Personalize My FitMate" else "Next")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("${current.number} / ${steps.size.toString().padStart(2, '0')}", color = MaterialTheme.colorScheme.primary)
            LinearProgressIndicator(
                progress = { (stepIndex + 1) / steps.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(20.dp)),
            )
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text("FitMate", color = MaterialTheme.colorScheme.primary)
                    Text(current.prompt, style = MaterialTheme.typography.headlineSmall)
                    when (current) {
                        OnboardingStep.HEIGHT -> NumericField("Height (cm)", height, showValidation.thenError(error)) { height = it.filter(Char::isDigit) }
                        OnboardingStep.WEIGHT -> NumericField("Weight (kg)", weight, showValidation.thenError(error)) { weight = it.filter(Char::isDigit) }
                        OnboardingStep.AGE -> NumericField("Age", age, showValidation.thenError(error)) { age = it.filter(Char::isDigit) }
                        OnboardingStep.WORKOUT -> NumericField("Workout time (minutes)", workoutMinutes, showValidation.thenError(error)) { workoutMinutes = it.filter(Char::isDigit) }
                        OnboardingStep.GENDER -> TextSelection("Choose one", listOf("Male", "Female", "Other"), gender) { gender = it }
                        OnboardingStep.GOAL -> EnumSelection("Pick your goal", GoalType.entries, goal, { it.label }) { goal = it }
                        OnboardingStep.FOOD -> EnumSelection("Pick your style", FoodPreference.entries, foodPreference, { it.label }) { foodPreference = it }
                        OnboardingStep.AI_PROVIDER -> EnumSelection("Provider mode", AiProviderMode.entries, providerMode, { it.label }) {
                            providerMode = it
                            stepIndex = 7
                        }
                        OnboardingStep.AI_BASE_URL -> NumericOrTextField("Base URL", baseUrl, showValidation.thenError(error)) { baseUrl = it }
                        OnboardingStep.AI_API_KEY -> NumericOrTextField("API key", apiKey, showValidation.thenError(error)) { apiKey = it }
                        OnboardingStep.AI_MODEL -> NumericOrTextField("Model name", modelName, showValidation.thenError(error)) { modelName = it }
                        OnboardingStep.LOCAL_ENDPOINT -> NumericOrTextField("Local endpoint", localEndpoint, showValidation.thenError(error)) { localEndpoint = it }
                        OnboardingStep.LOCAL_MODEL -> NumericOrTextField("Local model name", localModelName, showValidation.thenError(error)) { localModelName = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalizingScreen(state: PersonalizationState, onBackToSetup: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background))),
        contentAlignment = Alignment.Center,
    ) {
        Card(shape = RoundedCornerShape(32.dp), modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "FitMate logo",
                    modifier = Modifier.size(110.dp),
                )
                Text("Personalising...", style = MaterialTheme.typography.headlineSmall)
                Text(
                    state.status.ifBlank { "Building your AI-first goals, meals, diet, and workout memory." },
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { state.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(22.dp)),
                )
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    TextButton(onClick = onBackToSetup) { Text("Back to setup") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(state: CampusFitUiState, viewModel: CampusFitViewModel, selectedTab: HomeTab, onTabSelected: (HomeTab) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("FitMate", fontWeight = FontWeight.Bold)
                        Text(
                            state.personalizedPlan?.aiSummary ?: "AI-powered goals, meals, and momentum",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = { Icon(tab.icon(), contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (selectedTab) {
                HomeTab.DASHBOARD -> DashboardTab(state)
                HomeTab.MEALS -> MealsTab(state, viewModel)
                HomeTab.DIET -> DietTab(state)
                HomeTab.WORKOUT -> WorkoutTab(state)
                HomeTab.PROGRESS -> ProgressTab(state)
                HomeTab.SETTINGS -> SettingsTab(state, viewModel)
            }
        }
    }
}

@Composable
private fun DashboardTab(state: CampusFitUiState) {
    val dashboard = state.dashboard ?: return
    var showStreakDialog by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { showStreakDialog = true }, modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
            Icon(Icons.Outlined.LocalFireDepartment, contentDescription = "Streak", tint = MaterialTheme.colorScheme.primary)
        }
        Text("${dashboard.disciplineState.rewardPoints} reward points", style = MaterialTheme.typography.titleMedium)
    }

    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Smart Body Goal System", style = MaterialTheme.typography.headlineSmall)
            Text(dashboard.reasoning.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            GoalProgressLine("Calories", dashboard.progress.caloriesConsumed, dashboard.metrics.caloriesTarget, dashboard.progress.caloriesRatio(dashboard.metrics.caloriesTarget))
            GoalProgressLine("Protein", dashboard.progress.proteinConsumed, dashboard.metrics.proteinTarget, dashboard.progress.proteinRatio(dashboard.metrics.proteinTarget))
            GoalProgressLine("Water", dashboard.progress.waterLitersConsumed, dashboard.metrics.waterLitersTarget, dashboard.progress.waterRatio(dashboard.metrics.waterLitersTarget), "L")
            Text("Body mode: ${dashboard.metrics.calorieMode}")
            Text("BMI: ${"%.1f".format(dashboard.metrics.bmi)}")
        }
    }

    SectionCard("AI reasoning") {
        Text(dashboard.reasoning.calorieReasoning)
        Text(dashboard.reasoning.proteinReasoning)
        Text(dashboard.reasoning.waterReasoning)
        HorizontalDivider()
        dashboard.reasoning.coachingNotes.forEach { Text("- $it") }
    }

    if (showStreakDialog) {
        AlertDialog(
            onDismissRequest = { showStreakDialog = false },
            confirmButton = { TextButton(onClick = { showStreakDialog = false }) { Text("Close") } },
            title = { Text("Streak roadmap") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${dashboard.disciplineState.streakDays} day streak")
                    Text(dashboard.disciplineState.encouragement, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    dashboard.milestoneMap.forEach { milestone ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${milestone.days} days - ${milestone.title}", fontWeight = FontWeight.SemiBold)
                                Text(milestone.reward, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun MealsTab(state: CampusFitUiState, viewModel: CampusFitViewModel) {
    val meals = state.meals ?: return
    var slot by rememberSaveable { mutableStateOf(MealSlot.BREAKFAST) }
    var description by rememberSaveable { mutableStateOf("") }

    Text("Today's meal", style = MaterialTheme.typography.headlineSmall)

    SectionCard("Mess plate analyser") {
        EnumSelection("Meal slot", MealSlot.entries, slot, { it.label }) { slot = it }
        NumericOrTextField("Describe your meal", description, null, minLines = 4) { description = it }
        Button(onClick = { viewModel.analyzeMeal(slot, description) }, shape = RoundedCornerShape(18.dp)) {
            Text("Analyse meal with AI")
        }
    }

    state.dashboard?.let { dashboard ->
        SectionCard("Today's goal progress") {
            GoalProgressLine("Calories", meals.progress.caloriesConsumed, dashboard.metrics.caloriesTarget, meals.progress.caloriesRatio(dashboard.metrics.caloriesTarget))
            GoalProgressLine("Protein", meals.progress.proteinConsumed, dashboard.metrics.proteinTarget, meals.progress.proteinRatio(dashboard.metrics.proteinTarget))
        }
    }

    meals.latestAnalysis?.let { analysis ->
        SectionCard("Latest meal analysis") {
            Text("${analysis.slot.label}: ${analysis.estimatedCalories} kcal, ${analysis.estimatedProtein} g protein", style = MaterialTheme.typography.titleMedium)
            Text(analysis.reasoning, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Suggestions")
            analysis.suggestions.forEach { Text("- $it") }
            Text("What to avoid")
            analysis.avoid.forEach { Text("- $it") }
        }
    }
}

@Composable
private fun DietTab(state: CampusFitUiState) {
    val diet = state.personalizedPlan?.dietRecommendation ?: return
    SectionCard(diet.title) {
        diet.meals.forEach { Text("- $it") }
        HorizontalDivider()
        Text("Affordable proteins")
        diet.cheapProteins.forEach { Text("- $it") }
        HorizontalDivider()
        Text("Avoid")
        diet.avoid.forEach { Text("- $it") }
    }
}

@Composable
private fun WorkoutTab(state: CampusFitUiState) {
    val workout = state.personalizedPlan?.workoutPlan ?: return
    SectionCard(workout.title) {
        Text("Split: ${workout.split}")
        Text("Duration: ${workout.durationLabel}")
        workout.exercises.forEach { Text("- $it") }
    }
}

@Composable
private fun ProgressTab(state: CampusFitUiState) {
    val meals = state.meals ?: return
    SectionCard("Goal achievement history") {
        meals.weeklySummary.forEach { day ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(day.dateLabel, fontWeight = FontWeight.SemiBold)
                    Text("Calories: ${day.calories}/${day.calorieTarget}")
                    Text("Protein: ${day.protein}/${day.proteinTarget}")
                    Text(if (day.completed) "Goals hit" else "Still building consistency")
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(state: CampusFitUiState, viewModel: CampusFitViewModel) {
    var providerMode by rememberSaveable { mutableStateOf(state.aiConfig.providerMode) }
    var baseUrl by rememberSaveable { mutableStateOf(state.aiConfig.baseUrl) }
    var apiKey by rememberSaveable { mutableStateOf(state.aiConfig.apiKey) }
    var modelName by rememberSaveable { mutableStateOf(state.aiConfig.modelName) }
    var localEndpoint by rememberSaveable { mutableStateOf(state.aiConfig.localEndpoint) }
    var localModelName by rememberSaveable { mutableStateOf(state.aiConfig.localModelName) }
    var age by rememberSaveable { mutableStateOf(state.profile.age.toString()) }
    var height by rememberSaveable { mutableStateOf(state.profile.heightCm.toString()) }
    var weight by rememberSaveable { mutableStateOf(state.profile.weightKg.toString()) }
    var workout by rememberSaveable { mutableStateOf(state.profile.workoutMinutes.toString()) }

    SectionCard("Appearance") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(selected = state.themeMode == AppThemeMode.LIGHT, onClick = { viewModel.updateThemeMode(AppThemeMode.LIGHT) }, label = { Text("Light") }, leadingIcon = { Icon(Icons.Outlined.LightMode, contentDescription = null) })
            FilterChip(selected = state.themeMode == AppThemeMode.DARK, onClick = { viewModel.updateThemeMode(AppThemeMode.DARK) }, label = { Text("Dark") }, leadingIcon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) })
        }
    }

    SectionCard("AI memory setup") {
        Text(state.personalizedPlan?.aiSummary ?: "Your persistent AI baseline will be stored after setup.")
        EnumSelection("Provider mode", AiProviderMode.entries, providerMode, { it.label }) { providerMode = it }
        NumericOrTextField("Base URL", baseUrl, null) { baseUrl = it }
        NumericOrTextField("API key", apiKey, null) { apiKey = it }
        NumericOrTextField("Model name", modelName, null) { modelName = it }
        NumericOrTextField("Local endpoint", localEndpoint, null) { localEndpoint = it }
        NumericOrTextField("Local model name", localModelName, null) { localModelName = it }
        Button(
            onClick = {
                viewModel.updateAiConfig(
                    AiConfig(
                        providerMode = providerMode,
                        baseUrl = baseUrl,
                        apiKey = apiKey,
                        modelName = modelName,
                        localEndpoint = localEndpoint,
                        localModelName = localModelName,
                    ),
                )
            },
            shape = RoundedCornerShape(18.dp),
        ) {
            Text("Save AI settings")
        }
    }

    SectionCard("Personal info memory") {
        NumericField("Age", age, null) { age = it.filter(Char::isDigit) }
        NumericField("Height (cm)", height, null) { height = it.filter(Char::isDigit) }
        NumericField("Weight (kg)", weight, null) { weight = it.filter(Char::isDigit) }
        NumericField("Workout time (minutes)", workout, null) { workout = it.filter(Char::isDigit) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Daily reminders")
            Switch(checked = state.dashboard?.disciplineState?.remindersEnabled == true, onCheckedChange = { viewModel.toggleReminders() })
        }
        Button(
            onClick = {
                viewModel.updateProfile(
                    state.profile.copy(
                        age = age.toIntOrNull() ?: state.profile.age,
                        heightCm = height.toIntOrNull() ?: state.profile.heightCm,
                        weightKg = weight.toIntOrNull() ?: state.profile.weightKg,
                        workoutMinutes = workout.toIntOrNull() ?: state.profile.workoutMinutes,
                    ),
                )
            },
            shape = RoundedCornerShape(18.dp),
        ) {
            Text("Update personal info")
        }
    }
}

private fun HomeTab.icon(): ImageVector = when (this) {
    HomeTab.DASHBOARD -> Icons.Outlined.Bolt
    HomeTab.MEALS -> Icons.Outlined.LocalDining
    HomeTab.DIET -> Icons.Outlined.AutoAwesome
    HomeTab.WORKOUT -> Icons.AutoMirrored.Outlined.DirectionsRun
    HomeTab.PROGRESS -> Icons.Outlined.QueryStats
    HomeTab.SETTINGS -> Icons.Outlined.Settings
}

@Composable
private fun GoalProgressLine(label: String, current: Int, target: Int, progress: Float, suffix: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$label - $current/$target${if (suffix.isNotBlank()) " $suffix" else ""}", style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(24.dp)))
    }
}

@Composable
private fun GoalProgressLine(label: String, current: Double, target: Double, progress: Float, suffix: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$label - ${"%.1f".format(current)}/${"%.1f".format(target)}${if (suffix.isNotBlank()) " $suffix" else ""}", style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(24.dp)))
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun NumericField(label: String, value: String, errorText: String?, onValueChange: (String) -> Unit) {
    FieldBase(label, value, errorText, 1, onValueChange)
}

@Composable
private fun NumericOrTextField(label: String, value: String, errorText: String?, minLines: Int = 1, onValueChange: (String) -> Unit) {
    FieldBase(label, value, errorText, minLines, onValueChange)
}

@Composable
private fun FieldBase(label: String, value: String, errorText: String?, minLines: Int, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            isError = errorText != null,
            singleLine = minLines == 1,
            minLines = minLines,
        )
        if (errorText != null) Text(errorText, color = MaterialTheme.colorScheme.error)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> EnumSelection(label: String, options: Iterable<T>, selected: T, display: (T) -> String, onSelected: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(selected = option == selected, onClick = { onSelected(option) }, label = { Text(display(option)) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TextSelection(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(selected = option == selected, onClick = { onSelected(option) }, label = { Text(option) })
            }
        }
    }
}

private fun requireField(value: String): String? = if (value.isBlank()) "This field is required" else null
private fun Boolean.thenError(error: String?): String? = if (this) error else null
