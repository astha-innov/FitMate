package com.fitmate.domain.usecase

import com.fitmate.domain.model.ExerciseLibraryEntry
import com.fitmate.domain.model.ExerciseMetricType
import com.fitmate.domain.model.ActivityLevel
import com.fitmate.domain.model.ExperienceLevel
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WeeklyWorkoutSchedule
import com.fitmate.domain.model.WorkoutDaySchedule
import com.fitmate.domain.model.WorkoutExerciseConfig
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.model.WorkoutPlanType
import com.fitmate.domain.model.WorkoutWeekday
import com.fitmate.domain.workout.ExerciseRecommendationSource

class GenerateWorkoutScheduleUseCase(
    exerciseLibrary: List<ExerciseLibraryEntry>,
    private val recommendationSource: ExerciseRecommendationSource,
) {
    private val exercisesByName = exerciseLibrary.associateBy(ExerciseLibraryEntry::name)

    operator fun invoke(profile: UserProfile): WeeklyWorkoutSchedule {
        val focusOccurrences = mutableMapOf<WorkoutFocus, Int>()
        val days = templateFor(profile).map { (weekday, focus) ->
            val occurrence = focusOccurrences.getOrDefault(focus, 0)
            focusOccurrences[focus] = occurrence + 1
            WorkoutDaySchedule(
                weekday = weekday,
                focus = focus,
                exercises = exercisesFor(focus, profile, occurrence),
            )
        }

        return WeeklyWorkoutSchedule(
            days = days,
            isCustom = false,
            planType = WorkoutPlanType.DEFAULT,
            generatedForGoal = profile.goal,
            version = CURRENT_VERSION,
        )
    }

    private fun templateFor(profile: UserProfile): List<Pair<WorkoutWeekday, WorkoutFocus>> {
        val baseTemplate = when (profile.goal) {
            GoalType.MUSCLE_GAIN -> listOf(
                WorkoutWeekday.MONDAY to WorkoutFocus.PUSH,
                WorkoutWeekday.TUESDAY to WorkoutFocus.PULL,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.LEGS,
                WorkoutWeekday.THURSDAY to WorkoutFocus.PUSH,
                WorkoutWeekday.FRIDAY to WorkoutFocus.PULL,
                WorkoutWeekday.SATURDAY to WorkoutFocus.LEGS,
                WorkoutWeekday.SUNDAY to WorkoutFocus.REST,
            )
            GoalType.FAT_LOSS -> listOf(
                WorkoutWeekday.MONDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.TUESDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.REST,
                WorkoutWeekday.THURSDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.FRIDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.SATURDAY to WorkoutFocus.CORE_CONDITIONING,
                WorkoutWeekday.SUNDAY to WorkoutFocus.REST,
            )
            GoalType.LEAN_BODY -> listOf(
                WorkoutWeekday.MONDAY to WorkoutFocus.PUSH,
                WorkoutWeekday.TUESDAY to WorkoutFocus.PULL,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.REST,
                WorkoutWeekday.THURSDAY to WorkoutFocus.LEGS,
                WorkoutWeekday.FRIDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.SATURDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.SUNDAY to WorkoutFocus.REST,
            )
            GoalType.CARDIO_STAMINA -> listOf(
                WorkoutWeekday.MONDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.TUESDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.REST,
                WorkoutWeekday.THURSDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.FRIDAY to WorkoutFocus.CORE_CONDITIONING,
                WorkoutWeekday.SATURDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.SUNDAY to WorkoutFocus.REST,
            )
            GoalType.FLEXIBILITY_MOBILITY -> listOf(
                WorkoutWeekday.MONDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.TUESDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.THURSDAY to WorkoutFocus.REST,
                WorkoutWeekday.FRIDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.SATURDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.SUNDAY to WorkoutFocus.REST,
            )
            GoalType.STRESS_RELIEF -> listOf(
                WorkoutWeekday.MONDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.TUESDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.CORE_CONDITIONING,
                WorkoutWeekday.THURSDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.FRIDAY to WorkoutFocus.REST,
                WorkoutWeekday.SATURDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.SUNDAY to WorkoutFocus.REST,
            )
        }
        val needsAdditionalRecovery =
            profile.experienceLevel == ExperienceLevel.BEGINNER ||
                profile.activityLevel == ActivityLevel.LOW
        if (!needsAdditionalRecovery) return baseTemplate

        val recoveryDay = when (profile.goal) {
            GoalType.MUSCLE_GAIN,
            GoalType.FAT_LOSS,
            GoalType.LEAN_BODY,
            GoalType.CARDIO_STAMINA -> WorkoutWeekday.FRIDAY
            GoalType.FLEXIBILITY_MOBILITY,
            GoalType.STRESS_RELIEF -> return baseTemplate
        }
        return baseTemplate.map { (weekday, focus) ->
            if (weekday == recoveryDay) weekday to WorkoutFocus.REST else weekday to focus
        }
    }

    private fun exercisesFor(
        focus: WorkoutFocus,
        profile: UserProfile,
        occurrence: Int,
    ): List<WorkoutExerciseConfig> {
        if (focus == WorkoutFocus.REST) return emptyList()

        val pool = recommendationSource.recommendationNames(focus, profile)
            .mapNotNull(exercisesByName::get)
        if (pool.isEmpty()) return emptyList()

        val exerciseCount = when {
            profile.workoutMinutes < 20 -> 2
            profile.workoutMinutes <= 30 -> 3
            profile.workoutMinutes <= 45 -> 4
            profile.workoutMinutes <= 60 -> 5
            profile.workoutMinutes <= 90 -> 7
            else -> 9
        }.coerceAtMost(pool.size)
        val offset = occurrence % pool.size
        val rotated = pool.drop(offset) + pool.take(offset)

        return rotated.take(exerciseCount).map { entry ->
            WorkoutExerciseConfig(
                exerciseName = entry.name,
                sets = setsFor(profile.goal, profile.experienceLevel),
                amount = amountFor(entry, profile.goal, profile.experienceLevel),
            )
        }
    }

    private fun setsFor(goal: GoalType, experience: ExperienceLevel): Int {
        val base = when (goal) {
            GoalType.MUSCLE_GAIN -> 4
            GoalType.FAT_LOSS -> 3
            GoalType.LEAN_BODY -> 3
            GoalType.CARDIO_STAMINA -> 3
            GoalType.FLEXIBILITY_MOBILITY -> 2
            GoalType.STRESS_RELIEF -> 2
        }
        return if (experience == ExperienceLevel.BEGINNER) base.coerceAtMost(3) else base
    }

    private fun amountFor(
        exercise: ExerciseLibraryEntry,
        goal: GoalType,
        experience: ExperienceLevel,
    ): Int {
        val advancedBonus = if (experience == ExperienceLevel.INTERMEDIATE) {
            if (exercise.metricType == ExerciseMetricType.SECONDS) 5 else 2
        } else {
            0
        }
        val target = if (exercise.metricType == ExerciseMetricType.SECONDS) {
            when (goal) {
                GoalType.MUSCLE_GAIN -> 30
                GoalType.FAT_LOSS -> 35
                GoalType.LEAN_BODY -> 30
                GoalType.CARDIO_STAMINA -> 45
                GoalType.FLEXIBILITY_MOBILITY -> 35
                GoalType.STRESS_RELIEF -> 30
            }
        } else {
            when (goal) {
                GoalType.MUSCLE_GAIN -> 8
                GoalType.FAT_LOSS -> 12
                GoalType.LEAN_BODY -> 10
                GoalType.CARDIO_STAMINA -> 16
                GoalType.FLEXIBILITY_MOBILITY -> 8
                GoalType.STRESS_RELIEF -> 10
            }
        }
        return (target + advancedBonus).coerceIn(exercise.minAmount, exercise.maxAmount)
    }

    companion object {
        const val CURRENT_VERSION = 2
    }
}
