package com.fitmate.data

import android.content.Context
import android.content.SharedPreferences
import com.fitmate.domain.model.ActivityLevel
import com.fitmate.domain.model.AiConfig
import com.fitmate.domain.model.AiProviderMode
import com.fitmate.domain.model.AppThemeMode
import com.fitmate.domain.model.DietRecommendation
import com.fitmate.domain.model.DisciplineState
import com.fitmate.domain.model.ExperienceLevel
import com.fitmate.domain.model.FoodPreference
import com.fitmate.domain.model.GoalMetrics
import com.fitmate.domain.model.GoalProgress
import com.fitmate.domain.model.GoalReasoning
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.MealAnalysis
import com.fitmate.domain.model.MealLog
import com.fitmate.domain.model.MealSlot
import com.fitmate.domain.model.PersonalizedPlan
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WorkoutDayLog
import com.fitmate.domain.model.WeeklyWorkoutSchedule
import com.fitmate.domain.model.WorkoutExerciseProgress
import com.fitmate.domain.model.WorkoutDaySchedule
import com.fitmate.domain.model.WorkoutExerciseConfig
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.model.WorkoutPlan
import com.fitmate.domain.model.WorkoutWeekday
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

object AppStorage {
    private lateinit var prefs: SharedPreferences
    private const val KEY_LAST_USER_ID = "last_user_id"

    fun init(context: Context) {
        prefs = context.getSharedPreferences("fitmate_prefs", Context.MODE_PRIVATE)
    }

    fun isReady(): Boolean = ::prefs.isInitialized

    fun saveSetupCompleted(completed: Boolean) {
        prefs.edit().putBoolean("setup_completed", completed).apply()
    }

    fun loadSetupCompleted(): Boolean = prefs.getBoolean("setup_completed", false)

    fun saveProfile(profile: UserProfile) {
        prefs.edit().putString("profile_json", profileToJson(profile).toString()).apply()
    }

    fun loadProfile(): UserProfile? {
        val raw = prefs.getString("profile_json", null) ?: return null
        return profileFromJson(JSONObject(raw))
    }

    fun saveAiConfig(config: AiConfig) {
        prefs.edit().putString("ai_config_json", aiConfigToJson(config).toString()).apply()
    }

    fun loadAiConfig(): AiConfig? {
        val raw = prefs.getString("ai_config_json", null) ?: return null
        return aiConfigFromJson(JSONObject(raw))
    }

    fun saveThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun loadThemeMode(): AppThemeMode = AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.LIGHT.name)!!)

    fun savePlan(plan: PersonalizedPlan) {
        prefs.edit().putString("plan_json", planToJson(plan).toString()).apply()
    }

    fun loadPlan(): PersonalizedPlan? {
        val raw = prefs.getString("plan_json", null) ?: return null
        return planFromJson(JSONObject(raw))
    }

    fun saveDiscipline(state: DisciplineState) {
        prefs.edit().putString("discipline_json", disciplineToJson(state).toString()).apply()
    }

    fun loadDiscipline(): DisciplineState? {
        val raw = prefs.getString("discipline_json", null) ?: return null
        return disciplineFromJson(JSONObject(raw))
    }

    fun saveGoalProgress(progress: GoalProgress) {
        prefs.edit().putString("goal_progress_json", goalProgressToJson(progress).toString()).apply()
    }

    fun loadGoalProgress(): GoalProgress? {
        val raw = prefs.getString("goal_progress_json", null) ?: return null
        return goalProgressFromJson(JSONObject(raw))
    }

    fun saveMealLogs(logs: List<MealLog>) {
        prefs.edit().putString("meal_logs_json", JSONArray(logs.map(::mealLogToJson)).toString()).apply()
    }

    fun loadMealLogs(): List<MealLog> {
        val raw = prefs.getString("meal_logs_json", null) ?: return emptyList()
        val array = JSONArray(raw)
        return List(array.length()) { index -> mealLogFromJson(array.getJSONObject(index)) }
    }

    fun saveWorkoutSchedule(schedule: WeeklyWorkoutSchedule) {
        prefs.edit().putString("workout_schedule_json", workoutScheduleToJson(schedule).toString()).apply()
    }

    fun loadWorkoutSchedule(): WeeklyWorkoutSchedule? {
        val raw = prefs.getString("workout_schedule_json", null) ?: return null
        return workoutScheduleFromJson(JSONObject(raw))
    }

    fun saveWorkoutLogs(logs: List<WorkoutDayLog>) {
        prefs.edit().putString("workout_logs_json", JSONArray(logs.map(::workoutDayLogToJson)).toString()).apply()
    }

    fun loadWorkoutLogs(): List<WorkoutDayLog> {
        val raw = prefs.getString("workout_logs_json", null) ?: return emptyList()
        val array = JSONArray(raw)
        return List(array.length()) { index -> workoutDayLogFromJson(array.getJSONObject(index)) }
    }

    fun saveLastUserId(userId: String?) {
        prefs.edit().putString(KEY_LAST_USER_ID, userId).apply()
    }

    fun loadLastUserId(): String? = prefs.getString(KEY_LAST_USER_ID, null)

    fun clearUserScopedData() {
        prefs.edit()
            .remove("setup_completed")
            .remove("profile_json")
            .remove("ai_config_json")
            .remove("plan_json")
            .remove("discipline_json")
            .remove("goal_progress_json")
            .remove("meal_logs_json")
            .remove("workout_schedule_json")
            .remove("workout_logs_json")
            .apply()
    }

    internal fun profileToJson(profile: UserProfile): JSONObject = JSONObject()
        .put("age", profile.age)
        .put("heightCm", profile.heightCm)
        .put("weightKg", profile.weightKg)
        .put("gender", profile.gender)
        .put("activityLevel", profile.activityLevel.name)
        .put("goal", profile.goal.name)
        .put("foodPreference", profile.foodPreference.name)
        .put("budgetInr", profile.budgetInr)
        .put("workoutMinutes", profile.workoutMinutes)
        .put("experienceLevel", profile.experienceLevel.name)
        .put("equipment", JSONArray(profile.equipment.toList()))

    internal fun profileFromJson(json: JSONObject): UserProfile = UserProfile(
        age = json.optInt("age", 20),
        heightCm = json.optInt("heightCm", 170),
        weightKg = json.optInt("weightKg", 65),
        gender = json.optString("gender", "Male"),
        activityLevel = parseActivityLevel(json.optString("activityLevel", ActivityLevel.MODERATE.name)),
        goal = parseGoal(json.optString("goal", GoalType.LEAN_BODY.name)),
        foodPreference = FoodPreference.valueOf(json.optString("foodPreference", FoodPreference.EGGETARIAN.name)),
        budgetInr = json.optInt("budgetInr", 150),
        workoutMinutes = json.optInt("workoutMinutes", 45),
        experienceLevel = parseExperienceLevel(json.optString("experienceLevel", ExperienceLevel.BEGINNER.name)),
        equipment = json.optJSONArray("equipment")?.let(::jsonArrayToStringList)?.toSet() ?: setOf("Dumbbells", "Mat"),
    )

    internal fun aiConfigToJson(config: AiConfig): JSONObject = JSONObject()
        .put("providerMode", config.providerMode.name)
        .put("baseUrl", config.baseUrl)
        .put("apiKey", config.apiKey)
        .put("modelName", config.modelName)
        .put("localEndpoint", config.localEndpoint)
        .put("localModelName", config.localModelName)

    internal fun aiConfigFromJson(json: JSONObject): AiConfig = AiConfig(
        providerMode = AiProviderMode.valueOf(json.optString("providerMode", AiProviderMode.REMOTE_API.name)),
        baseUrl = json.optString("baseUrl", ""),
        apiKey = json.optString("apiKey", ""),
        modelName = json.optString("modelName", "gpt-4.1-mini"),
        localEndpoint = json.optString("localEndpoint", "http://10.0.2.2:11434"),
        localModelName = json.optString("localModelName", "llama3.1:8b"),
    )

    internal fun planToJson(plan: PersonalizedPlan): JSONObject = JSONObject()
        .put("metrics", goalMetricsToJson(plan.metrics))
        .put("reasoning", goalReasoningToJson(plan.reasoning))
        .put("diet", dietToJson(plan.dietRecommendation))
        .put("workout", workoutToJson(plan.workoutPlan))
        .put("aiSummary", plan.aiSummary)

    internal fun planFromJson(json: JSONObject): PersonalizedPlan = PersonalizedPlan(
        metrics = goalMetricsFromJson(json.getJSONObject("metrics")),
        reasoning = goalReasoningFromJson(json.getJSONObject("reasoning")),
        dietRecommendation = dietFromJson(json.getJSONObject("diet")),
        workoutPlan = workoutFromJson(json.getJSONObject("workout")),
        aiSummary = json.getString("aiSummary"),
    )

    internal fun disciplineToJson(state: DisciplineState): JSONObject = JSONObject()
        .put("streakDays", state.streakDays)
        .put("rewardPoints", state.rewardPoints)
        .put("remindersEnabled", state.remindersEnabled)
        .put("completedToday", state.completedToday)
        .put("nextMilestone", state.nextMilestone)
        .put("encouragement", state.encouragement)

    internal fun disciplineFromJson(json: JSONObject): DisciplineState = DisciplineState(
        streakDays = json.optInt("streakDays", 3),
        rewardPoints = json.optInt("rewardPoints", 120),
        remindersEnabled = json.optBoolean("remindersEnabled", true),
        completedToday = json.optBoolean("completedToday", false),
        nextMilestone = json.optInt("nextMilestone", 5),
        encouragement = json.optString("encouragement", "One good day stacks into the next. Keep feeding the chain."),
    )

    internal fun goalProgressToJson(progress: GoalProgress): JSONObject = JSONObject()
        .put("date", progress.date.toString())
        .put("caloriesConsumed", progress.caloriesConsumed)
        .put("proteinConsumed", progress.proteinConsumed)
        .put("waterLitersConsumed", progress.waterLitersConsumed)

    internal fun goalProgressFromJson(json: JSONObject): GoalProgress = GoalProgress(
        date = LocalDate.parse(json.optString("date", LocalDate.now().toString())),
        caloriesConsumed = json.optInt("caloriesConsumed", 0),
        proteinConsumed = json.optInt("proteinConsumed", 0),
        waterLitersConsumed = json.optDouble("waterLitersConsumed", 0.0),
    )

    internal fun mealLogToJson(log: MealLog): JSONObject = JSONObject()
        .put("id", log.id)
        .put("date", log.date.toString())
        .put("analysis", mealAnalysisToJson(log.analysis))

    internal fun mealLogFromJson(json: JSONObject): MealLog = MealLog(
        id = json.getString("id"),
        date = LocalDate.parse(json.getString("date")),
        analysis = mealAnalysisFromJson(json.getJSONObject("analysis")),
    )

    internal fun mealAnalysisToJson(analysis: MealAnalysis): JSONObject = JSONObject()
        .put("slot", analysis.slot.name)
        .put("description", analysis.description)
        .put("estimatedCalories", analysis.estimatedCalories)
        .put("estimatedProtein", analysis.estimatedProtein)
        .put("estimatedCarbs", analysis.estimatedCarbs)
        .put("estimatedFats", analysis.estimatedFats)
        .put("suggestions", JSONArray(analysis.suggestions))
        .put("avoid", JSONArray(analysis.avoid))
        .put("reasoning", analysis.reasoning)

    internal fun mealAnalysisFromJson(json: JSONObject): MealAnalysis = MealAnalysis(
        slot = MealSlot.valueOf(json.optString("slot", MealSlot.BREAKFAST.name)),
        description = json.optString("description", ""),
        estimatedCalories = json.optInt("estimatedCalories", 0),
        estimatedProtein = json.optInt("estimatedProtein", 0),
        estimatedCarbs = json.optInt("estimatedCarbs", 0),
        estimatedFats = json.optInt("estimatedFats", 0),
        suggestions = json.optJSONArray("suggestions")?.let(::jsonArrayToStringList) ?: emptyList(),
        avoid = json.optJSONArray("avoid")?.let(::jsonArrayToStringList) ?: emptyList(),
        reasoning = json.optString("reasoning", ""),
    )

    internal fun goalMetricsToJson(metrics: GoalMetrics): JSONObject = JSONObject()
        .put("caloriesTarget", metrics.caloriesTarget)
        .put("proteinTarget", metrics.proteinTarget)
        .put("waterLitersTarget", metrics.waterLitersTarget)
        .put("calorieMode", metrics.calorieMode)
        .put("bmi", metrics.bmi)

    internal fun goalMetricsFromJson(json: JSONObject): GoalMetrics = GoalMetrics(
        caloriesTarget = json.getInt("caloriesTarget"),
        proteinTarget = json.getInt("proteinTarget"),
        waterLitersTarget = json.getDouble("waterLitersTarget"),
        calorieMode = json.getString("calorieMode"),
        bmi = json.getDouble("bmi"),
    )

    internal fun goalReasoningToJson(reasoning: GoalReasoning): JSONObject = JSONObject()
        .put("summary", reasoning.summary)
        .put("calorieReasoning", reasoning.calorieReasoning)
        .put("proteinReasoning", reasoning.proteinReasoning)
        .put("waterReasoning", reasoning.waterReasoning)
        .put("coachingNotes", JSONArray(reasoning.coachingNotes))

    internal fun goalReasoningFromJson(json: JSONObject): GoalReasoning = GoalReasoning(
        summary = json.getString("summary"),
        calorieReasoning = json.getString("calorieReasoning"),
        proteinReasoning = json.getString("proteinReasoning"),
        waterReasoning = json.getString("waterReasoning"),
        coachingNotes = jsonArrayToStringList(json.getJSONArray("coachingNotes")),
    )

    internal fun dietToJson(diet: DietRecommendation): JSONObject = JSONObject()
        .put("title", diet.title)
        .put("meals", JSONArray(diet.meals))
        .put("cheapProteins", JSONArray(diet.cheapProteins))
        .put("avoid", JSONArray(diet.avoid))

    internal fun dietFromJson(json: JSONObject): DietRecommendation = DietRecommendation(
        title = json.getString("title"),
        meals = jsonArrayToStringList(json.getJSONArray("meals")),
        cheapProteins = jsonArrayToStringList(json.getJSONArray("cheapProteins")),
        avoid = jsonArrayToStringList(json.getJSONArray("avoid")),
    )

    internal fun workoutToJson(workout: WorkoutPlan): JSONObject = JSONObject()
        .put("title", workout.title)
        .put("split", workout.split)
        .put("durationLabel", workout.durationLabel)
        .put("exercises", JSONArray(workout.exercises))

    internal fun workoutFromJson(json: JSONObject): WorkoutPlan = WorkoutPlan(
        title = json.getString("title"),
        split = json.getString("split"),
        exercises = jsonArrayToStringList(json.getJSONArray("exercises")),
        durationLabel = json.getString("durationLabel"),
    )

    internal fun workoutScheduleToJson(schedule: WeeklyWorkoutSchedule): JSONObject = JSONObject()
        .put("isCustom", schedule.isCustom)
        .put("days", JSONArray(schedule.days.map(::workoutDayScheduleToJson)))

    internal fun workoutScheduleFromJson(json: JSONObject): WeeklyWorkoutSchedule = WeeklyWorkoutSchedule(
        days = json.optJSONArray("days")?.let { array ->
            List(array.length()) { index ->
                workoutDayScheduleFromJson(array.getJSONObject(index))
            }
        } ?: emptyList(),
        isCustom = json.optBoolean("isCustom", false),
    )

    internal fun workoutDayScheduleToJson(day: WorkoutDaySchedule): JSONObject = JSONObject()
        .put("weekday", day.weekday.name)
        .put("focus", day.focus.name)
        .put("exercises", JSONArray(day.exercises.map(::workoutExerciseConfigToJson)))

    internal fun workoutDayScheduleFromJson(json: JSONObject): WorkoutDaySchedule = WorkoutDaySchedule(
        weekday = WorkoutWeekday.valueOf(json.getString("weekday")),
        focus = WorkoutFocus.valueOf(json.getString("focus")),
        exercises = json.optJSONArray("exercises")?.let { array ->
            List(array.length()) { index ->
                workoutExerciseConfigFromJson(array.getJSONObject(index))
            }
        } ?: emptyList(),
    )

    internal fun workoutExerciseConfigToJson(exercise: WorkoutExerciseConfig): JSONObject = JSONObject()
        .put("exerciseName", exercise.exerciseName)
        .put("sets", exercise.sets)
        .put("amount", exercise.amount)

    internal fun workoutExerciseConfigFromJson(json: JSONObject): WorkoutExerciseConfig = WorkoutExerciseConfig(
        exerciseName = json.getString("exerciseName"),
        sets = json.optInt("sets", 3),
        amount = json.optInt("amount", json.optString("reps", "10").filter(Char::isDigit).toIntOrNull() ?: 10),
    )

    internal fun workoutDayLogToJson(log: WorkoutDayLog): JSONObject = JSONObject()
        .put("date", log.date.toString())
        .put("weekday", log.weekday.name)
        .put("focus", log.focus.name)
        .put("exercises", JSONArray(log.exercises.map(::workoutExerciseProgressToJson)))

    internal fun workoutDayLogFromJson(json: JSONObject): WorkoutDayLog = WorkoutDayLog(
        date = LocalDate.parse(json.optString("date", LocalDate.now().toString())),
        weekday = WorkoutWeekday.valueOf(json.optString("weekday", WorkoutWeekday.MONDAY.name)),
        focus = WorkoutFocus.valueOf(json.optString("focus", WorkoutFocus.REST.name)),
        exercises = json.optJSONArray("exercises")?.let { array ->
            List(array.length()) { index ->
                workoutExerciseProgressFromJson(array.getJSONObject(index))
            }
        } ?: emptyList(),
    )

    internal fun workoutExerciseProgressToJson(progress: WorkoutExerciseProgress): JSONObject = JSONObject()
        .put("exerciseName", progress.exerciseName)
        .put("completedSets", progress.completedSets)
        .put("totalSets", progress.totalSets)
        .put("lastElapsedSeconds", progress.lastElapsedSeconds)
        .put("sessionCount", progress.sessionCount)

    internal fun workoutExerciseProgressFromJson(json: JSONObject): WorkoutExerciseProgress = WorkoutExerciseProgress(
        exerciseName = json.optString("exerciseName", ""),
        completedSets = json.optInt("completedSets", 0),
        totalSets = json.optInt("totalSets", 0),
        lastElapsedSeconds = json.optInt("lastElapsedSeconds", 0),
        sessionCount = json.optInt("sessionCount", 0),
    )

    private fun jsonArrayToStringList(array: JSONArray): List<String> = buildList {
        for (index in 0 until array.length()) add(array.getString(index))
    }

    private fun parseGoal(raw: String): GoalType = when (raw) {
        "MAINTENANCE" -> GoalType.STRESS_RELIEF
        else -> GoalType.valueOf(raw)
    }

    private fun parseActivityLevel(raw: String): ActivityLevel = runCatching {
        ActivityLevel.valueOf(raw)
    }.getOrDefault(ActivityLevel.MODERATE)

    private fun parseExperienceLevel(raw: String): ExperienceLevel = runCatching {
        ExperienceLevel.valueOf(raw)
    }.getOrDefault(ExperienceLevel.BEGINNER)
}
