package com.fitmate.domain.usecase

import com.fitmate.data.LocalExerciseDatabase
import com.fitmate.domain.model.ActivityLevel
import com.fitmate.domain.model.ExperienceLevel
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.model.WorkoutPlanType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateWorkoutScheduleUseCaseTest {
    private val generator = GenerateWorkoutScheduleUseCase(LocalExerciseDatabase.exercises)

    @Test
    fun `every goal generates a complete recommended week with valid exercises`() {
        GoalType.entries.forEach { goal ->
            val schedule = generator(profile(goal = goal))

            assertEquals(7, schedule.days.size)
            assertEquals(WorkoutPlanType.DEFAULT, schedule.planType)
            assertEquals(goal, schedule.generatedForGoal)
            assertEquals(GenerateWorkoutScheduleUseCase.CURRENT_VERSION, schedule.version)
            assertTrue(schedule.days.any { it.focus == WorkoutFocus.REST })
            schedule.days
                .flatMap { it.exercises }
                .forEach { config ->
                    assertTrue(LocalExerciseDatabase.exerciseByName(config.exerciseName) != null)
                    assertTrue(config.sets > 0)
                    assertTrue(config.amount > 0)
                }
        }
    }

    @Test
    fun `muscle gain uses a push pull legs split`() {
        val schedule = generator(
            profile(
                goal = GoalType.MUSCLE_GAIN,
                experienceLevel = ExperienceLevel.INTERMEDIATE,
                activityLevel = ActivityLevel.HIGH,
            )
        )

        assertEquals(
            listOf(
                WorkoutFocus.PUSH,
                WorkoutFocus.PULL,
                WorkoutFocus.LEGS,
                WorkoutFocus.PUSH,
                WorkoutFocus.PULL,
                WorkoutFocus.LEGS,
                WorkoutFocus.REST,
            ),
            schedule.days.map { it.focus },
        )
        assertTrue(
            schedule.days
                .filter { it.focus != WorkoutFocus.REST }
                .flatMap { it.exercises }
                .all { it.sets == 4 }
        )
    }

    @Test
    fun `different goals produce different weekly focus patterns`() {
        val signatures = GoalType.entries.associateWith { goal ->
            generator(profile(goal = goal)).days.map { it.focus }
        }

        assertNotEquals(signatures[GoalType.MUSCLE_GAIN], signatures[GoalType.FAT_LOSS])
        assertNotEquals(signatures[GoalType.LEAN_BODY], signatures[GoalType.CARDIO_STAMINA])
        assertNotEquals(signatures[GoalType.FLEXIBILITY_MOBILITY], signatures[GoalType.STRESS_RELIEF])
    }

    @Test
    fun `beginner muscle plan includes more recovery than intermediate high activity plan`() {
        val beginner = generator(
            profile(
                goal = GoalType.MUSCLE_GAIN,
                experienceLevel = ExperienceLevel.BEGINNER,
                activityLevel = ActivityLevel.LOW,
            )
        )
        val intermediate = generator(
            profile(
                goal = GoalType.MUSCLE_GAIN,
                experienceLevel = ExperienceLevel.INTERMEDIATE,
                activityLevel = ActivityLevel.HIGH,
            )
        )

        assertTrue(
            beginner.days.count { it.focus == WorkoutFocus.REST } >
                intermediate.days.count { it.focus == WorkoutFocus.REST }
        )
    }

    @Test
    fun `workout duration controls exercises per active day`() {
        val compact = generator(profile(goal = GoalType.LEAN_BODY, workoutMinutes = 25))
        val extended = generator(profile(goal = GoalType.LEAN_BODY, workoutMinutes = 60))

        assertTrue(compact.days.filter { it.focus != WorkoutFocus.REST }.all { it.exercises.size <= 2 })
        assertTrue(
            extended.days
                .filter { it.focus != WorkoutFocus.REST }
                .sumOf { it.exercises.size } >
                compact.days
                    .filter { it.focus != WorkoutFocus.REST }
                    .sumOf { it.exercises.size }
        )
    }

    private fun profile(
        goal: GoalType,
        experienceLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
        activityLevel: ActivityLevel = ActivityLevel.MODERATE,
        workoutMinutes: Int = 45,
    ) = UserProfile(
        goal = goal,
        experienceLevel = experienceLevel,
        activityLevel = activityLevel,
        workoutMinutes = workoutMinutes,
    )
}
