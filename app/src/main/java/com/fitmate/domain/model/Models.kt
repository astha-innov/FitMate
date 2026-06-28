package com.fitmate.domain.model

import java.time.LocalDate

enum class GoalType(val label: String) {
    FAT_LOSS("Fat loss"),
    MUSCLE_GAIN("Muscle gain"),
    LEAN_BODY("Lean body"),
    STRESS_RELIEF("Reduce stress & relax"),
    CARDIO_STAMINA("Cardio / Stamina"),
    FLEXIBILITY_MOBILITY("Increasing flexibility and mobility"),
}

enum class FoodPreference(val label: String) {
    VEGETARIAN("Vegetarian"),
    EGGETARIAN("Eggetarian"),
    NON_VEGETARIAN("Non-vegetarian"),
}

enum class ActivityLevel(val label: String, val multiplier: Double) {
    LOW("Low", 1.2),
    MODERATE("Moderate", 1.45),
    HIGH("High", 1.7),
}

enum class ExperienceLevel(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
}

enum class AiProviderMode(val label: String) {
    REMOTE_API("Remote API"),
    LOCAL_LLM("Local LLM"),
}

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
}

enum class MealSlot(val label: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    SNACKS("Snacks"),
    DINNER("Dinner"),
}

data class UserProfile(
    val age: Int = 20,
    val heightCm: Int = 170,
    val weightKg: Int = 65,
    val gender: String = "Male",
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goal: GoalType = GoalType.LEAN_BODY,
    val foodPreference: FoodPreference = FoodPreference.EGGETARIAN,
    val budgetInr: Int = 150,
    val workoutMinutes: Int = 45,
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val equipment: Set<String> = setOf("Dumbbells", "Mat"),
)

data class AiConfig(
    val providerMode: AiProviderMode = AiProviderMode.REMOTE_API,
    val baseUrl: String = "",
    val apiKey: String = "",
    val modelName: String = "gpt-4.1-mini",
    val localEndpoint: String = "http://10.0.2.2:11434",
    val localModelName: String = "llama3.1:8b",
)

data class GoalMetrics(
    val caloriesTarget: Int,
    val proteinTarget: Int,
    val waterLitersTarget: Double,
    val calorieMode: String,
    val bmi: Double,
)

data class GoalProgress(
    val date: LocalDate,
    val caloriesConsumed: Int = 0,
    val proteinConsumed: Int = 0,
    val waterLitersConsumed: Double = 0.0,
) {
    fun caloriesRatio(target: Int): Float = if (target <= 0) 0f else (caloriesConsumed.toFloat() / target).coerceIn(0f, 1f)
    fun proteinRatio(target: Int): Float = if (target <= 0) 0f else (proteinConsumed.toFloat() / target).coerceIn(0f, 1f)
    fun waterRatio(target: Double): Float = if (target <= 0.0) 0f else (waterLitersConsumed / target).toFloat().coerceIn(0f, 1f)
}

data class GoalReasoning(
    val summary: String,
    val calorieReasoning: String,
    val proteinReasoning: String,
    val waterReasoning: String,
    val coachingNotes: List<String>,
)

data class DietRecommendation(
    val title: String,
    val meals: List<String>,
    val cheapProteins: List<String>,
    val avoid: List<String>,
)

data class WorkoutPlan(
    val title: String,
    val split: String,
    val exercises: List<String>,
    val durationLabel: String,
)

enum class WorkoutWeekday(val label: String) {
    SUNDAY("Sunday"),
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
}

enum class WorkoutFocus(val label: String) {
    PUSH("Push"),
    PULL("Pull"),
    CHEST_BICEPS("Chest + Biceps"),
    BACK_REAR_DELTS("Back + Rear Delts"),
    LEGS("Legs"),
    SHOULDERS_TRICEPS("Shoulders + Triceps"),
    CORE_CONDITIONING("Core + Conditioning"),
    CONDITIONING("Conditioning"),
    FULL_BODY("Full Body"),
    MOBILITY("Mobility + Recovery"),
    ARMS_ABS("ARMS + ABS"),
    LEGS_SHOULDERS("LEGS + SHOULDERS"),
    UPPER_BODY_POWER("UPPER BODY POWER"),
    ARM_SPECIALIZATION_WEAK_POINTS("ARM SPECIALIZATION + WEAK POINTS"),
    REST("Rest Day"),
}

data class WorkoutExerciseConfig(
    val exerciseName: String,
    val sets: Int,
    val amount: Int,
)

data class WorkoutDaySchedule(
    val weekday: WorkoutWeekday,
    val focus: WorkoutFocus,
    val exercises: List<WorkoutExerciseConfig> = emptyList(),
)

enum class WorkoutPlanType {
    DEFAULT,
    CUSTOM,
}

data class WeeklyWorkoutSchedule(
    val days: List<WorkoutDaySchedule>,
    val isCustom: Boolean,
    val planType: WorkoutPlanType = if (isCustom) WorkoutPlanType.CUSTOM else WorkoutPlanType.DEFAULT,
    val generatedForGoal: GoalType? = null,
    val version: Int = 1,
)

data class WorkoutExerciseProgress(
    val exerciseName: String,
    val completedSets: Int = 0,
    val totalSets: Int,
    val lastElapsedSeconds: Int = 0,
    val sessionCount: Int = 0,
)

data class WorkoutDayLog(
    val date: LocalDate,
    val weekday: WorkoutWeekday,
    val focus: WorkoutFocus,
    val exercises: List<WorkoutExerciseProgress> = emptyList(),
)

enum class WorkoutDayStatus {
    COMPLETED,
    PARTIAL,
    MISSED,
    REST,
    NONE,
}

data class PersonalizedPlan(
    val metrics: GoalMetrics,
    val reasoning: GoalReasoning,
    val dietRecommendation: DietRecommendation,
    val workoutPlan: WorkoutPlan,
    val aiSummary: String,
)

data class StreakMilestone(
    val days: Int,
    val title: String,
    val reward: String,
)

data class DisciplineState(
    val streakDays: Int,
    val rewardPoints: Int,
    val remindersEnabled: Boolean,
    val completedToday: Boolean,
    val nextMilestone: Int,
    val encouragement: String,
)

data class MealAnalysis(
    val slot: MealSlot,
    val description: String,
    val estimatedCalories: Int,
    val estimatedProtein: Int,
    val estimatedCarbs: Int,
    val estimatedFats: Int,
    val suggestions: List<String>,
    val avoid: List<String>,
    val reasoning: String,
)

data class MealLog(
    val id: String,
    val date: LocalDate,
    val analysis: MealAnalysis,
)

data class DayProgressSummary(
    val dateLabel: String,
    val calories: Int,
    val calorieTarget: Int,
    val protein: Int,
    val proteinTarget: Int,
    val completed: Boolean,
)

data class ProfileSnapshot(
    val metrics: GoalMetrics,
    val progress: GoalProgress,
    val reasoning: GoalReasoning,
    val disciplineState: DisciplineState,
    val milestoneMap: List<StreakMilestone>,
)

data class MealsSnapshot(
    val latestAnalysis: MealAnalysis?,
    val todayMeals: List<MealLog>,
    val progress: GoalProgress,
    val weeklySummary: List<DayProgressSummary>,
)

data class ExerciseLibraryEntry(
    val name: String,
    val muscleGroup: String,
    val instructions: String,
    val postureImage: String = "",
    val detailGifAsset: String = "",
    val instructionMarkdownAsset: String = "",
    val metricType: ExerciseMetricType = ExerciseMetricType.REPS,
    val defaultAmount: Int = 12,
    val minAmount: Int = 6,
    val maxAmount: Int = 20,
    val easyMaxWorkload: Int = 36,
    val mediumMaxWorkload: Int = 72,
)

enum class ExerciseMetricType(val unitLabel: String) {
    REPS("reps"),
    SECONDS("sec"),
}

