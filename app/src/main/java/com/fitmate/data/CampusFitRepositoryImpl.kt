package com.fitmate.data

import com.fitmate.domain.model.AiConfig
import com.fitmate.domain.model.AppThemeMode
import com.fitmate.domain.model.DisciplineState
import com.fitmate.domain.model.GoalProgress
import com.fitmate.domain.model.MealAnalysis
import com.fitmate.domain.model.MealLog
import com.fitmate.domain.model.PersonalizedPlan
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WeeklyWorkoutSchedule
import com.fitmate.domain.repository.CampusFitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class CampusFitRepositoryImpl(
    private val backendService: FirebaseBackendService = FirebaseBackendService(),
) : CampusFitRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _profile = MutableStateFlow(if (AppStorage.isReady()) AppStorage.loadProfile() ?: UserProfile() else UserProfile())
    private val _aiConfig = MutableStateFlow(if (AppStorage.isReady()) AppStorage.loadAiConfig() ?: AiConfig() else AiConfig())
    private val _themeMode = MutableStateFlow(if (AppStorage.isReady()) AppStorage.loadThemeMode() else AppThemeMode.LIGHT)
    private val _setupCompleted = MutableStateFlow(if (AppStorage.isReady()) AppStorage.loadSetupCompleted() else false)
    private val _personalizedPlan = MutableStateFlow(if (AppStorage.isReady()) AppStorage.loadPlan() else null)
    private val _discipline = MutableStateFlow(if (AppStorage.isReady()) AppStorage.loadDiscipline() ?: defaultDisciplineState() else defaultDisciplineState())
    private val _todayProgress = MutableStateFlow(if (AppStorage.isReady()) AppStorage.loadGoalProgress() ?: GoalProgress(LocalDate.now()) else GoalProgress(LocalDate.now()))
    private val _mealLogs = MutableStateFlow(if (AppStorage.isReady()) AppStorage.loadMealLogs() else emptyList())
    private val _latestMealAnalysis = MutableStateFlow(_mealLogs.value.firstOrNull()?.analysis)
    private val _workoutSchedule = MutableStateFlow(if (AppStorage.isReady()) AppStorage.loadWorkoutSchedule() else null)

    override val profile: StateFlow<UserProfile> = _profile.asStateFlow()
    override val aiConfig: StateFlow<AiConfig> = _aiConfig.asStateFlow()
    override val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()
    override val setupCompleted: StateFlow<Boolean> = _setupCompleted.asStateFlow()
    override val personalizedPlan: StateFlow<PersonalizedPlan?> = _personalizedPlan.asStateFlow()
    override val discipline: StateFlow<DisciplineState> = _discipline.asStateFlow()
    override val todayProgress: StateFlow<GoalProgress> = _todayProgress.asStateFlow()
    override val mealLogs: StateFlow<List<MealLog>> = _mealLogs.asStateFlow()
    override val latestMealAnalysis: StateFlow<MealAnalysis?> = _latestMealAnalysis.asStateFlow()
    override val workoutSchedule: StateFlow<WeeklyWorkoutSchedule?> = _workoutSchedule.asStateFlow()

    init {
        normalizeDailyProgress()
        scope.launch { bootstrapBackend() }
    }

    override fun updateProfile(profile: UserProfile) {
        _profile.value = profile
        persistLocalState()
        syncToBackend()
    }

    override fun updateAiConfig(config: AiConfig) {
        _aiConfig.value = config
        persistLocalState()
        syncToBackend()
    }

    override fun updateThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        persistLocalState()
        syncToBackend()
    }

    override fun toggleReminders() {
        _discipline.value = _discipline.value.copy(remindersEnabled = !_discipline.value.remindersEnabled)
        persistLocalState()
        syncToBackend()
    }

    override fun saveMealAnalysis(analysis: MealAnalysis) {
        normalizeDailyProgress()
        _latestMealAnalysis.value = analysis
        _mealLogs.value = listOf(MealLog(UUID.randomUUID().toString(), LocalDate.now(), analysis)) + _mealLogs.value
        _todayProgress.value = _todayProgress.value.copy(
            date = LocalDate.now(),
            caloriesConsumed = _todayProgress.value.caloriesConsumed + analysis.estimatedCalories,
            proteinConsumed = _todayProgress.value.proteinConsumed + analysis.estimatedProtein,
        )
        updateRewardState()
        persistLocalState()
        syncToBackend()
    }

    override fun addWater(amountLiters: Double) {
        normalizeDailyProgress()
        _todayProgress.value = _todayProgress.value.copy(waterLitersConsumed = _todayProgress.value.waterLitersConsumed + amountLiters)
        updateRewardState()
        persistLocalState()
        syncToBackend()
    }

    override fun savePersonalizedPlan(plan: PersonalizedPlan) {
        _personalizedPlan.value = plan
        persistLocalState()
        syncToBackend()
    }

    override fun markSetupCompleted(completed: Boolean) {
        _setupCompleted.value = completed
        persistLocalState()
        syncToBackend()
    }

    override fun saveWorkoutSchedule(schedule: WeeklyWorkoutSchedule) {
        _workoutSchedule.value = schedule
        persistLocalState()
        syncToBackend()
    }

    private suspend fun bootstrapBackend() {
        if (!backendService.isConfigured()) return
        val remoteState = runCatching { backendService.loadState() }.getOrNull()
        if (remoteState != null) applyRemoteState(remoteState) else syncToBackend()
    }

    private fun applyRemoteState(state: BackendState) {
        state.profile?.let { _profile.value = it }
        state.aiConfig?.let { _aiConfig.value = it }
        state.themeMode?.let { _themeMode.value = it }
        state.setupCompleted?.let { _setupCompleted.value = it }
        state.personalizedPlan?.let { _personalizedPlan.value = it }
        state.discipline?.let { _discipline.value = it }
        state.todayProgress?.let { _todayProgress.value = it }
        state.mealLogs?.let {
            _mealLogs.value = it.sortedByDescending(MealLog::date)
            _latestMealAnalysis.value = it.firstOrNull()?.analysis
        }
        state.workoutSchedule?.let { _workoutSchedule.value = it }
        normalizeDailyProgress()
        persistLocalState()
    }

    private fun syncToBackend() {
        scope.launch { runCatching { backendService.saveState(snapshotState()) } }
    }

    private fun snapshotState(): BackendState = BackendState(
        profile = _profile.value,
        aiConfig = _aiConfig.value,
        themeMode = _themeMode.value,
        setupCompleted = _setupCompleted.value,
        personalizedPlan = _personalizedPlan.value,
        discipline = _discipline.value,
        todayProgress = _todayProgress.value,
        mealLogs = _mealLogs.value.take(60),
        workoutSchedule = _workoutSchedule.value,
    )

    private fun persistLocalState() {
        if (!AppStorage.isReady()) return
        AppStorage.saveProfile(_profile.value)
        AppStorage.saveAiConfig(_aiConfig.value)
        AppStorage.saveThemeMode(_themeMode.value)
        AppStorage.saveSetupCompleted(_setupCompleted.value)
        _personalizedPlan.value?.let(AppStorage::savePlan)
        AppStorage.saveDiscipline(_discipline.value)
        AppStorage.saveGoalProgress(_todayProgress.value)
        AppStorage.saveMealLogs(_mealLogs.value.take(60))
        _workoutSchedule.value?.let(AppStorage::saveWorkoutSchedule)
    }

    private fun normalizeDailyProgress() {
        val today = LocalDate.now()
        if (_todayProgress.value.date != today) {
            _todayProgress.value = GoalProgress(date = today)
            _discipline.value = _discipline.value.copy(completedToday = false)
        }
    }

    private fun updateRewardState() {
        val targets = _personalizedPlan.value?.metrics
        val completedToday = when {
            targets == null -> _todayProgress.value.caloriesConsumed >= 1800 && _todayProgress.value.proteinConsumed >= 90
            else -> _todayProgress.value.caloriesConsumed >= targets.caloriesTarget &&
                    _todayProgress.value.proteinConsumed >= targets.proteinTarget &&
                    _todayProgress.value.waterLitersConsumed >= targets.waterLitersTarget
        }
        val wasCompleted = _discipline.value.completedToday
        val streakDays = if (completedToday && !wasCompleted) _discipline.value.streakDays + 1 else _discipline.value.streakDays
        val milestone = listOf(5, 10, 15, 21, 30).firstOrNull { it > streakDays } ?: 30
        val points = if (completedToday && !wasCompleted) _discipline.value.rewardPoints + 25 else _discipline.value.rewardPoints
        val encouragement = when {
            streakDays >= 15 -> "Elite consistency. Your routine is turning into identity."
            streakDays >= 10 -> "Double digits. This is where confidence becomes visible."
            streakDays >= 5 -> "Nice work. Your streak has real momentum now."
            else -> "One good day stacks into the next. Keep feeding the chain."
        }
        _discipline.value = _discipline.value.copy(
            streakDays = streakDays,
            rewardPoints = points,
            completedToday = completedToday,
            nextMilestone = milestone,
            encouragement = encouragement,
        )
    }

    private fun defaultDisciplineState(): DisciplineState = DisciplineState(
        streakDays = 3,
        rewardPoints = 120,
        remindersEnabled = true,
        completedToday = false,
        nextMilestone = 5,
        encouragement = "You are 2 solid days away from your next streak badge.",
    )
}
