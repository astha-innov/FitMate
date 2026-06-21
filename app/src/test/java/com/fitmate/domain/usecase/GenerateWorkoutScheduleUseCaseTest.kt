package com.fitmate.domain.usecase

import com.fitmate.data.LocalExerciseDatabase
import com.fitmate.data.LocalExerciseCatalog
import com.fitmate.domain.model.ActivityLevel
import com.fitmate.domain.model.ExperienceLevel
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.model.WorkoutPlanType
import com.fitmate.domain.model.WorkoutWeekday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateWorkoutScheduleUseCaseTest {
    private val generator = GenerateWorkoutScheduleUseCase(
        LocalExerciseDatabase.exercises,
        LocalExerciseCatalog,
    )

    @Test
    fun `every goal generates a complete recommended week with valid exercises`() {
        GoalType.entries.forEach { goal ->
            val schedule = generator(profile(goal = goal))

            assertEquals(7, schedule.days.size)
            assertEquals(WorkoutPlanType.DEFAULT, schedule.planType)
            assertEquals(goal, schedule.generatedForGoal)
            assertEquals(GenerateWorkoutScheduleUseCase.CURRENT_VERSION, schedule.version)
            assertTrue(schedule.days.any { it.focus == WorkoutFocus.REST })
            assertEquals(
                listOf(
                    WorkoutWeekday.MONDAY,
                    WorkoutWeekday.TUESDAY,
                    WorkoutWeekday.WEDNESDAY,
                    WorkoutWeekday.THURSDAY,
                    WorkoutWeekday.FRIDAY,
                    WorkoutWeekday.SATURDAY,
                    WorkoutWeekday.SUNDAY,
                ),
                schedule.days.map { it.weekday },
            )
            assertEquals(WorkoutFocus.REST, schedule.days.last().focus)
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
        val expectedCounts = mapOf(
            30 to 3,
            45 to 4,
            60 to 5,
            90 to 7,
            120 to 9,
        )

        expectedCounts.forEach { (minutes, expectedCount) ->
            val activeDays = generator(
                profile(
                    goal = GoalType.MUSCLE_GAIN,
                    experienceLevel = ExperienceLevel.INTERMEDIATE,
                    activityLevel = ActivityLevel.HIGH,
                    workoutMinutes = minutes,
                )
            ).days.filter { it.focus != WorkoutFocus.REST }

            assertTrue(activeDays.all { it.exercises.size == expectedCount })
        }

        val compactTotal = generator(profile(goal = GoalType.LEAN_BODY, workoutMinutes = 30))
            .days.sumOf { it.exercises.size }
        val extendedTotal = generator(profile(goal = GoalType.LEAN_BODY, workoutMinutes = 120))
            .days.sumOf { it.exercises.size }
        assertTrue(extendedTotal > compactTotal)
    }

    @Test
    fun `new custom split options resolve to valid exercises`() {
        val newFocusOptions = listOf(
            WorkoutFocus.ARMS_ABS,
            WorkoutFocus.LEGS_SHOULDERS,
            WorkoutFocus.UPPER_BODY_POWER,
            WorkoutFocus.ARM_SPECIALIZATION_WEAK_POINTS,
        )

        newFocusOptions.forEach { focus ->
            val exerciseNames = LocalExerciseCatalog.forFocus(focus).map { it.name }
            assertTrue(exerciseNames.isNotEmpty())
            assertTrue(exerciseNames.all { LocalExerciseDatabase.exerciseByName(it) != null })
        }
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
