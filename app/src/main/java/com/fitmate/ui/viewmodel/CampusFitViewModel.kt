package com.fitmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitmate.data.CampusFitRepositoryImpl
import com.fitmate.data.LocalExerciseDatabase
import com.fitmate.domain.analytics.AnalyticsEngine
import com.fitmate.domain.analytics.AnalyticsSnapshot
import com.fitmate.domain.model.AiConfig
import com.fitmate.domain.model.AppThemeMode
import com.fitmate.domain.model.ProfileSnapshot
import com.fitmate.domain.model.DietRecommendation
import com.fitmate.domain.model.DisciplineState
import com.fitmate.domain.model.GoalProgress
import com.fitmate.domain.model.GoalReasoning
import com.fitmate.domain.model.MealLog
import com.fitmate.domain.model.MealSlot
import com.fitmate.domain.model.MealsSnapshot
import com.fitmate.domain.model.PersonalizedPlan
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WorkoutDayLog
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.model.WorkoutWeekday
import com.fitmate.domain.model.WeeklyWorkoutSchedule
import com.fitmate.domain.model.WorkoutPlan
import com.fitmate.domain.repository.CampusFitRepository

import com.fitmate.domain.usecase.BuildProfileSnapshotUseCase
import com.fitmate.domain.usecase.BuildMealsSnapshotUseCase
import com.fitmate.domain.usecase.CalculateGoalMetricsUseCase
import com.fitmate.domain.usecase.CreateDietRecommendationUseCase

import com.fitmate.domain.usecase.CreateWorkoutPlanUseCase
import com.fitmate.domain.usecase.GenerateWorkoutScheduleUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CampusFitUiState(
    val profile: UserProfile = UserProfile(),
    val aiConfig: AiConfig = AiConfig(),
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val setupCompleted: Boolean = false,
    val personalizedPlan: PersonalizedPlan? = null,
    val profileSnapshot: ProfileSnapshot? = null,
    val meals: MealsSnapshot? = null,
    val diet: DietRecommendation? = null,
    val workout: WorkoutPlan? = null,
    val workoutSchedule: WeeklyWorkoutSchedule? = null,
    val workoutLogs: List<WorkoutDayLog> = emptyList(),
    val analytics: AnalyticsSnapshot = AnalyticsSnapshot(),
)

data class PersonalizationState(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val status: String = "",
    val error: String? = null,
)

class CampusFitViewModel(
    private val repository: CampusFitRepository,
    private val buildProfileSnapshot: BuildProfileSnapshotUseCase = BuildProfileSnapshotUseCase(),
    private val buildMealsSnapshot: BuildMealsSnapshotUseCase = BuildMealsSnapshotUseCase(),
    private val calculateGoalMetrics: CalculateGoalMetricsUseCase = CalculateGoalMetricsUseCase(),

    private val createDietRecommendation: CreateDietRecommendationUseCase =
        CreateDietRecommendationUseCase(),
    private val createWorkoutPlan: CreateWorkoutPlanUseCase =
        CreateWorkoutPlanUseCase(),
    private val generateWorkoutSchedule: GenerateWorkoutScheduleUseCase =
        GenerateWorkoutScheduleUseCase(LocalExerciseDatabase.exercises),
    private val analyticsEngine: AnalyticsEngine = AnalyticsEngine(),
) : ViewModel() {

    private val _personalizationState =
        MutableStateFlow(PersonalizationState())

    val personalizationState: StateFlow<PersonalizationState> =
        _personalizationState.asStateFlow()

    val uiState: StateFlow<CampusFitUiState> =
        repository.profile
            .combine(repository.aiConfig) { profile, config ->
                profile to config
            }
            .combine(repository.themeMode) { (profile, config), themeMode ->
                Triple(profile, config, themeMode)
            }
            .combine(repository.setupCompleted) {
                    (profile, config, themeMode),
                    setupCompleted ->

                Quad(
                    profile,
                    config,
                    themeMode,
                    setupCompleted
                )
            }
            .combine(repository.personalizedPlan) { seed, plan ->
                seed to plan
            }
            .combine(repository.discipline) {
                    (seed, plan),
                    discipline ->

                ProfileSeed(
                    seed.profile,
                    seed.config,
                    seed.themeMode,
                    seed.setupCompleted,
                    plan,
                    discipline
                )
            }
            .combine(repository.todayProgress) { seed, progress ->
                seed to progress
            }
            .combine(repository.mealLogs) {
                    (seed, progress),
                    mealLogs ->

                ProfileMealsSeed(
                    seed,
                    progress,
                    mealLogs
                )
            }
            .combine(repository.latestMealAnalysis) {
                    seeded,
                    latestAnalysis ->

                seeded to latestAnalysis
            }
            .combine(repository.workoutSchedule) {
                    (seeded, latestAnalysis),
                    workoutSchedule ->
                Triple(seeded, latestAnalysis, workoutSchedule)
            }
            .combine(repository.workoutLogs) {
                    (seeded, latestAnalysis, workoutSchedule),
                    workoutLogs ->

                val profile = seeded.seed.profile
                val config = seeded.seed.config
                val plan = seeded.seed.plan

                CampusFitUiState(
                    profile = profile,
                    aiConfig = config,
                    themeMode = seeded.seed.themeMode,
                    setupCompleted = seeded.seed.setupCompleted,
                    personalizedPlan = plan,
                    profileSnapshot = plan?.let {
                        buildProfileSnapshot(
                            it,
                            seeded.progress,
                            seeded.seed.discipline
                        )
                    },
                    meals = plan?.let {
                        buildMealsSnapshot(
                            it,
                            seeded.progress,
                            seeded.mealLogs,
                            latestAnalysis
                        )
                    },
                    diet = plan?.dietRecommendation
                        ?: createDietRecommendation(profile),

                    workout = plan?.workoutPlan
                        ?: createWorkoutPlan(profile),
                    workoutSchedule = workoutSchedule,
                    workoutLogs = workoutLogs,
                    analytics = analyticsEngine.generate(workoutLogs),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CampusFitUiState(),
            )

    fun updateProfile(profile: UserProfile) =
        repository.updateProfile(profile)

    fun updateThemeMode(mode: AppThemeMode) =
        repository.updateThemeMode(mode)

    fun toggleReminders() =
        repository.toggleReminders()

    fun addWater(amountLiters: Double) =
        repository.addWater(amountLiters)

    fun saveWorkoutSchedule(schedule: WeeklyWorkoutSchedule) =
        repository.saveWorkoutSchedule(schedule)

    fun generateRecommendedWorkoutSchedule(profile: UserProfile): WeeklyWorkoutSchedule =
        generateWorkoutSchedule(profile)

    fun recordWorkoutSet(
        weekday: WorkoutWeekday,
        focus: WorkoutFocus,
        exerciseName: String,
        totalSets: Int,
        elapsedSeconds: Int,
        incrementCompletedSet: Boolean,
    ) = repository.recordWorkoutSet(
        weekday = weekday,
        focus = focus,
        exerciseName = exerciseName,
        totalSets = totalSets,
        elapsedSeconds = elapsedSeconds,
        incrementCompletedSet = incrementCompletedSet,
    )

    fun bootstrapPersonalization(profile: UserProfile) {

        viewModelScope.launch {

            val config = AiConfig(
                baseUrl = "https://openrouter.ai/api/v1",
                apiKey = "",
                modelName = "openai/gpt-4o-mini"
            )

            try {

                _personalizationState.value =
                    PersonalizationState(
                        isRunning = true,
                        progress = 0.12f,
                        status = "Saving your FitMate profile"
                    )

                repository.updateProfile(profile)
                repository.updateAiConfig(config)

                delay(300)

                _personalizationState.value =
                    PersonalizationState(
                        isRunning = true,
                        progress = 0.38f,
                        status = "Generating your smart fitness system"
                    )

                delay(250)

                val plan = PersonalizedPlan(
                    metrics = calculateGoalMetrics(profile),

                    reasoning = GoalReasoning(
                        summary = "Starter fitness plan generated locally.",
                        calorieReasoning = "Calories adjusted for your goal.",
                        proteinReasoning = "Protein optimized for recovery.",
                        waterReasoning = "Hydration adjusted for your body.",
                        coachingNotes = listOf(
                            "Stay consistent daily.",
                            "Track meals regularly."
                        )
                    ),

                    dietRecommendation = createDietRecommendation(profile),

                    workoutPlan = createWorkoutPlan(profile),

                    aiSummary = "Local FitMate starter profile"
                )


                _personalizationState.value =
                    PersonalizationState(
                        isRunning = true,
                        progress = 0.67f,
                        status = "Building your workouts and nutrition"
                    )

                repository.savePersonalizedPlan(plan)

                delay(250)

                repository.markSetupCompleted(true)

                _personalizationState.value =
                    PersonalizationState(
                        isRunning = true,
                        progress = 1f,
                        status = "Your FitMate is ready"
                    )

                delay(300)

                _personalizationState.value =
                    PersonalizationState()

            } catch (t: Throwable) {

                _personalizationState.value =
                    PersonalizationState(
                        isRunning = false,
                        progress = 0f,
                        status = "Personalization failed",
                        error = t.message ?: "Unknown setup error"
                    )
            }
        }
    }

    fun analyzeMeal(
        slot: MealSlot,
        description: String,
    ) {

        if (description.isBlank()) return

        viewModelScope.launch {

            try {

                val profile = uiState.value.profile
                val config = uiState.value.aiConfig

                val metrics =
                    uiState.value.personalizedPlan?.metrics
                        ?: calculateGoalMetrics(profile)



            } catch (_: Throwable) {
            }
        }
    }

    private data class Quad(
        val profile: UserProfile,
        val config: AiConfig,
        val themeMode: AppThemeMode,
        val setupCompleted: Boolean,
    )

    private data class ProfileSeed(
        val profile: UserProfile,
        val config: AiConfig,
        val themeMode: AppThemeMode,
        val setupCompleted: Boolean,
        val plan: PersonalizedPlan?,
        val discipline: DisciplineState,
    )

    private data class ProfileMealsSeed(
        val seed: ProfileSeed,
        val progress: GoalProgress,
        val mealLogs: List<MealLog>,
    )

    companion object {

        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {

                    @Suppress("UNCHECKED_CAST")

                    return CampusFitViewModel(
                        CampusFitRepositoryImpl()
                    ) as T
                }
            }
    }
}
