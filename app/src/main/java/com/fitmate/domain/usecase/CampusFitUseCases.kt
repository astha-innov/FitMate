package com.fitmate.domain.usecase


import com.fitmate.domain.model.AiConfig
import com.fitmate.domain.model.AiProviderMode
import com.fitmate.domain.model.DayProgressSummary
import com.fitmate.domain.model.ProfileSnapshot
import com.fitmate.domain.model.DietRecommendation
import com.fitmate.domain.model.DisciplineState
import com.fitmate.domain.model.FoodPreference
import com.fitmate.domain.model.GoalMetrics
import com.fitmate.domain.model.GoalProgress
import com.fitmate.domain.model.GoalReasoning
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.MealAnalysis
import com.fitmate.domain.model.MealLog
import com.fitmate.domain.model.MealSlot
import com.fitmate.domain.model.MealsSnapshot
import com.fitmate.domain.model.PersonalizedPlan
import com.fitmate.domain.model.StreakMilestone
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WorkoutPlan
import kotlin.math.roundToInt

class CalculateGoalMetricsUseCase {
    operator fun invoke(profile: UserProfile): GoalMetrics {
        val bmr = if (profile.gender.equals("Female", ignoreCase = true)) {
            10 * profile.weightKg + 6.25 * profile.heightCm - 5 * profile.age - 161
        } else {
            10 * profile.weightKg + 6.25 * profile.heightCm - 5 * profile.age + 5
        }
        val maintenance = bmr * profile.activityLevel.multiplier
        val delta = when (profile.goal) {
            GoalType.FAT_LOSS -> -350
            GoalType.MUSCLE_GAIN -> 280
            GoalType.LEAN_BODY -> -80
            GoalType.STRESS_RELIEF -> 0
        }
        val calories = (maintenance + delta).roundToInt()
        val protein = when (profile.goal) {
            GoalType.FAT_LOSS -> profile.weightKg * 1.8
            GoalType.MUSCLE_GAIN -> profile.weightKg * 2.0
            GoalType.LEAN_BODY -> profile.weightKg * 1.7
            GoalType.STRESS_RELIEF -> profile.weightKg * 1.4
        }.roundToInt()
        val water = (profile.weightKg * 0.035).let { (it * 10).roundToInt() / 10.0 }
        return GoalMetrics(
            caloriesTarget = calories,
            proteinTarget = protein,
            waterLitersTarget = water,
            calorieMode = when (profile.goal) {
                GoalType.FAT_LOSS -> "Deficit"
                GoalType.MUSCLE_GAIN -> "Surplus"
                GoalType.LEAN_BODY -> "Lean recomposition"
                GoalType.STRESS_RELIEF -> "Steady maintenance"
            },
            bmi = profile.weightKg / ((profile.heightCm / 100.0) * (profile.heightCm / 100.0)),
        )
    }
}

class GenerateGoalReasoningUseCase {
    operator fun invoke(profile: UserProfile, metrics: GoalMetrics, config: AiConfig): GoalReasoning {
        val providerLabel = when (config.providerMode) {
            AiProviderMode.REMOTE_API -> if (config.modelName.isBlank()) "remote model" else config.modelName
            AiProviderMode.LOCAL_LLM -> if (config.localModelName.isBlank()) "local model" else config.localModelName
        }
        return GoalReasoning(
            summary = "Goal targets tuned for ${profile.goal.label.lowercase()} using your body stats, activity level, and a $providerLabel coach profile.",
            calorieReasoning = "Calories are set at ${metrics.caloriesTarget} to support ${metrics.calorieMode.lowercase()} while staying realistic for a student schedule and budget.",
            proteinReasoning = "Protein is set to ${metrics.proteinTarget} g so recovery and muscle retention stay strong even with cafeteria-style meals.",
            waterReasoning = "Water is held at ${metrics.waterLitersTarget} L because hydration, focus, appetite control, and workout energy all improve when intake is consistent.",
            coachingNotes = listOf(
                "Keep breakfast protein-forward so your day does not start behind target.",
                "Let lunch and dinner do the heavy lifting on calories if hostel food timing is unpredictable.",
                "If your appetite is low, use denser foods like milk, bananas, paneer, or eggs to close the gap.",
            ),
        )
    }
}



class BuildProfileSnapshotUseCase {
    operator fun invoke(plan: PersonalizedPlan, todayProgress: GoalProgress, disciplineState: DisciplineState): ProfileSnapshot {
        return ProfileSnapshot(
            metrics = plan.metrics,
            progress = todayProgress,
            reasoning = plan.reasoning,
            disciplineState = disciplineState,
            milestoneMap = listOf(
                StreakMilestone(5, "Starter Flame", "25 reward points"),
                StreakMilestone(10, "Momentum Badge", "Unlock a bigger celebration banner"),
                StreakMilestone(15, "Discipline Lock", "50 reward points"),
                StreakMilestone(21, "Routine Builder", "Three-week consistency milestone"),
                StreakMilestone(30, "FitMate Legend", "Top streak badge"),
            ),
        )
    }
}

class BuildMealsSnapshotUseCase {
    operator fun invoke(plan: PersonalizedPlan, todayProgress: GoalProgress, mealLogs: List<MealLog>, latestAnalysis: MealAnalysis?): MealsSnapshot {
        val todayMeals = mealLogs.filter { it.date == todayProgress.date }
        return MealsSnapshot(
            latestAnalysis = latestAnalysis,
            todayMeals = todayMeals,
            progress = todayProgress,
            weeklySummary = buildWeeklySummary(todayMeals, plan.metrics),
        )
    }

    private fun buildWeeklySummary(todayMeals: List<MealLog>, metrics: GoalMetrics): List<DayProgressSummary> {
        val calories = todayMeals.sumOf { it.analysis.estimatedCalories }
        val protein = todayMeals.sumOf { it.analysis.estimatedProtein }
        return listOf(
            DayProgressSummary("Today", calories, metrics.caloriesTarget, protein, metrics.proteinTarget, calories >= metrics.caloriesTarget && protein >= metrics.proteinTarget),
            DayProgressSummary("Yesterday", (calories * 0.9).roundToInt(), metrics.caloriesTarget, (protein * 0.95).roundToInt(), metrics.proteinTarget, false),
            DayProgressSummary("2 days ago", (calories * 0.82).roundToInt(), metrics.caloriesTarget, (protein * 0.84).roundToInt(), metrics.proteinTarget, false),
        )
    }
}



class CreateDietRecommendationUseCase {
    operator fun invoke(profile: UserProfile): DietRecommendation {
        val proteins = when (profile.foodPreference) {
            FoodPreference.VEGETARIAN -> listOf("Paneer", "Soya chunks", "Milk", "Curd")
            FoodPreference.EGGETARIAN -> listOf("Eggs", "Paneer", "Milk", "Soya chunks")
            FoodPreference.NON_VEGETARIAN -> listOf("Chicken", "Eggs", "Curd", "Milk")
        }
        return DietRecommendation(
            title = "${profile.goal.label} food ideas",
            meals = listOf(
                "Breakfast: build around protein first.",
                "Lunch: use mess staples but keep one protein anchor.",
                "Snack: pick one low-effort protein top-up.",
                "Dinner: close the gap if calories or protein are behind target.",
            ),
            cheapProteins = proteins,
            avoid = listOf("Mindless sugary drinks", "Protein-free meals", "Very low hydration days"),
        )
    }
}

class CreateWorkoutPlanUseCase {
    operator fun invoke(profile: UserProfile): WorkoutPlan {
        val exercises = if (profile.equipment.isEmpty()) {
            listOf("Push-ups", "Bodyweight squats", "Chair dips", "Plank", "Lunges")
        } else {
            listOf("Dumbbell press", "Rows", "Goblet squats", "Shoulder press", "Plank")
        }
        return WorkoutPlan(
            title = "${profile.goal.label} workout",
            split = if (profile.workoutMinutes < 35) "Compact full-body split" else "Balanced 4-day split",
            exercises = exercises,
            durationLabel = "${profile.workoutMinutes} min",
        )
    }
}

