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
import com.fitmate.domain.workout.WorkoutExerciseCatalog

class GenerateWorkoutScheduleUseCase(
    exerciseLibrary: List<ExerciseLibraryEntry>,
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
                WorkoutWeekday.SUNDAY to WorkoutFocus.PUSH,
                WorkoutWeekday.MONDAY to WorkoutFocus.PULL,
                WorkoutWeekday.TUESDAY to WorkoutFocus.LEGS,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.PUSH,
                WorkoutWeekday.THURSDAY to WorkoutFocus.PULL,
                WorkoutWeekday.FRIDAY to WorkoutFocus.LEGS,
                WorkoutWeekday.SATURDAY to WorkoutFocus.REST,
            )
            GoalType.FAT_LOSS -> listOf(
                WorkoutWeekday.SUNDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.MONDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.TUESDAY to WorkoutFocus.REST,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.THURSDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.FRIDAY to WorkoutFocus.CORE_CONDITIONING,
                WorkoutWeekday.SATURDAY to WorkoutFocus.REST,
            )
            GoalType.LEAN_BODY -> listOf(
                WorkoutWeekday.SUNDAY to WorkoutFocus.PUSH,
                WorkoutWeekday.MONDAY to WorkoutFocus.PULL,
                WorkoutWeekday.TUESDAY to WorkoutFocus.REST,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.LEGS,
                WorkoutWeekday.THURSDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.FRIDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.SATURDAY to WorkoutFocus.REST,
            )
            GoalType.CARDIO_STAMINA -> listOf(
                WorkoutWeekday.SUNDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.MONDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.TUESDAY to WorkoutFocus.REST,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.THURSDAY to WorkoutFocus.CORE_CONDITIONING,
                WorkoutWeekday.FRIDAY to WorkoutFocus.CONDITIONING,
                WorkoutWeekday.SATURDAY to WorkoutFocus.REST,
            )
            GoalType.FLEXIBILITY_MOBILITY -> listOf(
                WorkoutWeekday.SUNDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.MONDAY to WorkoutFocus.REST,
                WorkoutWeekday.TUESDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.THURSDAY to WorkoutFocus.REST,
                WorkoutWeekday.FRIDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.SATURDAY to WorkoutFocus.MOBILITY,
            )
            GoalType.STRESS_RELIEF -> listOf(
                WorkoutWeekday.SUNDAY to WorkoutFocus.FULL_BODY,
                WorkoutWeekday.MONDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.TUESDAY to WorkoutFocus.REST,
                WorkoutWeekday.WEDNESDAY to WorkoutFocus.CORE_CONDITIONING,
                WorkoutWeekday.THURSDAY to WorkoutFocus.MOBILITY,
                WorkoutWeekday.FRIDAY to WorkoutFocus.REST,
                WorkoutWeekday.SATURDAY to WorkoutFocus.FULL_BODY,
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

        val pool = WorkoutExerciseCatalog.namesFor(focus)
            .mapNotNull(exercisesByName::get)
            .let { filterForEquipment(it, profile) }
        if (pool.isEmpty()) return emptyList()

        val exerciseCount = when {
            profile.workoutMinutes < 30 -> 2
            profile.workoutMinutes < 50 -> 3
            else -> 4
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

    private fun filterForEquipment(
        exercises: List<ExerciseLibraryEntry>,
        profile: UserProfile,
    ): List<ExerciseLibraryEntry> {
        val explicitlyBodyweightOnly = profile.equipment.any {
            it.equals("No equipment", ignoreCase = true) ||
                it.equals("Bodyweight only", ignoreCase = true)
        }
        if (!explicitlyBodyweightOnly) return exercises

        val bodyweightExercises = setOf(
            "Push-Ups",
            "Body Tricep Press",
            "Bench Dips",
            "Dynamic Back Stretch",
            "Elevated Back Lunge",
            "Bottoms Up",
            "Mountain Climber",
            "Jumping Jack",
            "Plank",
            "Sit-Up",
            "Decline Reverse Crunch",
            "Bent Knee Hip Raise",
        )
        return exercises.filter { it.name in bodyweightExercises }.ifEmpty { exercises }
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
