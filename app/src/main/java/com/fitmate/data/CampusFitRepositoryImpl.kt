package com.fitmate.data

import com.fitmate.domain.model.AiConfig
import com.fitmate.domain.model.AppThemeMode
import com.fitmate.domain.model.DisciplineState
import com.fitmate.domain.model.GoalProgress
import com.fitmate.domain.model.MealAnalysis
import com.fitmate.domain.model.MealLog
import com.fitmate.domain.model.PersonalizedPlan
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WorkoutDayLog
import com.fitmate.domain.model.WorkoutDayStatus
import com.fitmate.domain.model.WorkoutExerciseProgress
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.model.WorkoutWeekday
import com.fitmate.domain.model.WeeklyWorkoutSchedule
import com.fitmate.domain.repository.CampusFitRepository
import com.google.firebase.auth.FirebaseAuth
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
    private var userScopeChecked = false
    private val authStateListener = FirebaseAuth.AuthStateListener {
        handleUserSessionChange()
    }

    private fun ensureUserScopeReady(): Boolean {
        if (!AppStorage.isReady()) return false
        if (!userScopeChecked) {
            val currentUserId = backendService.currentUserId()
            val lastUserId = AppStorage.loadLastUserId()
            if (currentUserId != null && currentUserId != lastUserId) {
                AppStorage.clearUserScopedData()
                AppStorage.saveLastUserId(currentUserId)
            } else if (currentUserId == null && lastUserId != null) {
                AppStorage.clearUserScopedData()
                AppStorage.saveLastUserId(null)
            }
            userScopeChecked = true
        }
        return true
    }

    private val _profile = MutableStateFlow(if (ensureUserScopeReady()) AppStorage.loadProfile() ?: UserProfile() else UserProfile())
    private val _aiConfig = MutableStateFlow(if (ensureUserScopeReady()) AppStorage.loadAiConfig() ?: AiConfig() else AiConfig())
    private val _themeMode = MutableStateFlow(if (AppStorage.isReady()) AppStorage.loadThemeMode() else AppThemeMode.LIGHT)
    private val _setupCompleted = MutableStateFlow(if (ensureUserScopeReady()) AppStorage.loadSetupCompleted() else false)
    private val _personalizedPlan = MutableStateFlow(if (ensureUserScopeReady()) AppStorage.loadPlan() else null)
    private val _discipline = MutableStateFlow(if (ensureUserScopeReady()) AppStorage.loadDiscipline() ?: defaultDisciplineState() else defaultDisciplineState())
    private val _todayProgress = MutableStateFlow(if (ensureUserScopeReady()) AppStorage.loadGoalProgress() ?: GoalProgress(LocalDate.now()) else GoalProgress(LocalDate.now()))
    private val _mealLogs = MutableStateFlow(if (ensureUserScopeReady()) AppStorage.loadMealLogs() else emptyList())
    private val _latestMealAnalysis = MutableStateFlow(_mealLogs.value.firstOrNull()?.analysis)
    private val _workoutSchedule = MutableStateFlow(if (ensureUserScopeReady()) AppStorage.loadWorkoutSchedule() else null)
    private val _workoutLogs = MutableStateFlow(if (ensureUserScopeReady()) AppStorage.loadWorkoutLogs() else emptyList())

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
    override val workoutLogs: StateFlow<List<WorkoutDayLog>> = _workoutLogs.asStateFlow()

    init {
        normalizeDailyProgress()
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
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
        persistLocalState()
        syncToBackend()
    }

    override fun addWater(amountLiters: Double) {
        normalizeDailyProgress()
        _todayProgress.value = _todayProgress.value.copy(waterLitersConsumed = _todayProgress.value.waterLitersConsumed + amountLiters)
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
        recalculateWorkoutDiscipline()
        persistLocalState()
        syncToBackend()
    }

    override fun recordWorkoutSet(
        weekday: WorkoutWeekday,
        focus: WorkoutFocus,
        exerciseName: String,
        totalSets: Int,
        elapsedSeconds: Int,
        incrementCompletedSet: Boolean,
    ) {
        val today = LocalDate.now()
        val currentDay = _workoutLogs.value.firstOrNull { it.date == today && it.weekday == weekday }
            ?: WorkoutDayLog(
                date = today,
                weekday = weekday,
                focus = focus,
                exercises = emptyList(),
            )

        val existingExercise = currentDay.exercises.firstOrNull { it.exerciseName == exerciseName }
            ?: WorkoutExerciseProgress(
                exerciseName = exerciseName,
                completedSets = 0,
                totalSets = totalSets,
                lastElapsedSeconds = 0,
                sessionCount = 0,
            )

        val updatedExercise = existingExercise.copy(
            completedSets = if (incrementCompletedSet) {
                (existingExercise.completedSets + 1).coerceAtMost(totalSets)
            } else {
                existingExercise.completedSets.coerceAtMost(totalSets)
            },
            totalSets = totalSets,
            lastElapsedSeconds = elapsedSeconds.coerceAtLeast(0),
            sessionCount = existingExercise.sessionCount + 1,
        )

        val updatedExercises = (currentDay.exercises.filterNot { it.exerciseName == exerciseName } + updatedExercise)
            .sortedBy(WorkoutExerciseProgress::exerciseName)

        val updatedDay = currentDay.copy(
            focus = focus,
            exercises = updatedExercises,
        )

        _workoutLogs.value = (_workoutLogs.value.filterNot { it.date == today && it.weekday == weekday } + updatedDay)
            .sortedByDescending(WorkoutDayLog::date)

        recalculateWorkoutDiscipline()
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
        state.workoutLogs?.let { _workoutLogs.value = it.sortedByDescending(WorkoutDayLog::date) }
        normalizeDailyProgress()
        recalculateWorkoutDiscipline()
        persistLocalState()
    }

    private fun syncToBackend() {
        scope.launch { runCatching { backendService.saveState(snapshotState()) } }
    }

    private fun handleUserSessionChange() {
        if (!AppStorage.isReady()) return

        val currentUserId = backendService.currentUserId()
        val lastUserId = AppStorage.loadLastUserId()

        if (currentUserId == lastUserId) return

        AppStorage.clearUserScopedData()
        AppStorage.saveLastUserId(currentUserId)
        userScopeChecked = true
        resetUserScopedState()

        if (currentUserId != null) {
            scope.launch { bootstrapBackend() }
        }
    }

    private fun resetUserScopedState() {
        _profile.value = UserProfile()
        _aiConfig.value = AiConfig()
        _setupCompleted.value = false
        _personalizedPlan.value = null
        _discipline.value = defaultDisciplineState()
        _todayProgress.value = GoalProgress(LocalDate.now())
        _mealLogs.value = emptyList()
        _latestMealAnalysis.value = null
        _workoutSchedule.value = null
        _workoutLogs.value = emptyList()
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
        workoutLogs = _workoutLogs.value.take(180),
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
        AppStorage.saveWorkoutLogs(_workoutLogs.value.take(180))
    }

    private fun normalizeDailyProgress() {
        val today = LocalDate.now()
        if (_todayProgress.value.date != today) {
            _todayProgress.value = GoalProgress(date = today)
            _discipline.value = _discipline.value.copy(completedToday = false)
        }
        pruneOldWorkoutLogs()
    }

    private fun pruneOldWorkoutLogs() {
        val cutoff = LocalDate.now().minusMonths(6)
        _workoutLogs.value = _workoutLogs.value
            .filter { !it.date.isBefore(cutoff) }
            .sortedByDescending(WorkoutDayLog::date)
    }

    private fun recalculateWorkoutDiscipline() {
        val today = LocalDate.now()
        val completedToday = workoutStatusForDate(today) == WorkoutDayStatus.COMPLETED
        var streakDays = 0
        var cursor = today

        while (true) {
            when (workoutStatusForDate(cursor)) {
                WorkoutDayStatus.COMPLETED -> {
                    streakDays += 1
                    cursor = cursor.minusDays(1)
                }
                WorkoutDayStatus.REST,
                WorkoutDayStatus.NONE -> {
                    cursor = cursor.minusDays(1)
                }
                WorkoutDayStatus.PARTIAL,
                WorkoutDayStatus.MISSED -> break
            }
            if (cursor.isBefore(today.minusYears(2))) break
        }

        val milestone = listOf(5, 10, 15, 21, 30).firstOrNull { it > streakDays } ?: 30
        val points = streakDays * 25
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

    private fun workoutStatusForDate(date: LocalDate): WorkoutDayStatus {
        val scheduleDay = _workoutSchedule.value
            ?.days
            ?.firstOrNull { it.weekday == date.toWorkoutWeekday() }

        if (scheduleDay == null) return WorkoutDayStatus.NONE
        if (scheduleDay.focus == WorkoutFocus.REST) return WorkoutDayStatus.REST

        val log = _workoutLogs.value.firstOrNull { it.date == date && it.weekday == scheduleDay.weekday }
            ?: return if (date.isBefore(LocalDate.now())) WorkoutDayStatus.MISSED else WorkoutDayStatus.NONE

        val isCompleted = scheduleDay.exercises.isNotEmpty() &&
            scheduleDay.exercises.all { config ->
                val progress = log.exercises.firstOrNull { it.exerciseName == config.exerciseName }
                progress != null && progress.completedSets >= config.sets
            }

        if (isCompleted) return WorkoutDayStatus.COMPLETED

        val hasStarted = log.exercises.any { it.sessionCount > 0 || it.completedSets > 0 }
        return if (hasStarted) WorkoutDayStatus.PARTIAL else if (date.isBefore(LocalDate.now())) WorkoutDayStatus.MISSED else WorkoutDayStatus.NONE
    }

    private fun defaultDisciplineState(): DisciplineState = DisciplineState(
        streakDays = 0,
        rewardPoints = 0,
        remindersEnabled = true,
        completedToday = false,
        nextMilestone = 5,
        encouragement = "One good day stacks into the next. Keep feeding the chain.",
    )
}

private fun LocalDate.toWorkoutWeekday(): WorkoutWeekday = when (dayOfWeek.value % 7) {
    0 -> WorkoutWeekday.SUNDAY
    1 -> WorkoutWeekday.MONDAY
    2 -> WorkoutWeekday.TUESDAY
    3 -> WorkoutWeekday.WEDNESDAY
    4 -> WorkoutWeekday.THURSDAY
    5 -> WorkoutWeekday.FRIDAY
    else -> WorkoutWeekday.SATURDAY
}
