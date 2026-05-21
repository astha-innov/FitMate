package com.fitmate.domain.model

import java.time.LocalDate

enum class GoalType(val label: String) {
    FAT_LOSS("Fat loss"),
    MUSCLE_GAIN("Muscle gain"),
    LEAN_BODY("Lean body"),
    STRESS_RELIEF("Reduce stress & relax"),
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

data class DashboardSnapshot(
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
