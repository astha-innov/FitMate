package com.fitmate.domain.analytics

import com.fitmate.domain.model.WorkoutDayLog
import java.time.LocalDate

class AnalyticsEngine {

    fun generate(
        workoutLogs: List<WorkoutDayLog>
    ): AnalyticsSnapshot {

        if (workoutLogs.isEmpty()) {
            return AnalyticsSnapshot()
        }

        val completedSets =
            workoutLogs.sumOf { day ->
                day.exercises.sumOf {
                    it.completedSets
                }
            }

        val totalSets =
            workoutLogs.sumOf { day ->
                day.exercises.sumOf {
                    it.totalSets
                }
            }

        val completionRate =
            if (totalSets == 0) 0
            else ((completedSets * 100f) / totalSets).toInt()

        val averageDuration =
            workoutLogs
                .flatMap { it.exercises }
                .map { it.lastElapsedSeconds }
                .average()
                .toInt() / 60

        val weeklyActivity =
            generateWeeklyActivity(workoutLogs)

        return AnalyticsSnapshot(
            currentStreak =
                calculateCurrentStreak(workoutLogs),

            longestStreak =
                calculateLongestStreak(workoutLogs),

            totalWorkouts =
                workoutLogs.size,

            completedSets =
                completedSets,

            totalSets =
                totalSets,

            completionRate =
                completionRate,

            averageWorkoutDurationMinutes =
                averageDuration,

            fitnessScore =
                minOf(
                    100,
                    (completionRate * 0.7).toInt() +
                            workoutLogs.size
                ),

            weeklyActivity =
                weeklyActivity
        )
    }

    private fun generateWeeklyActivity(
        logs: List<WorkoutDayLog>
    ): List<Int> {

        val today = LocalDate.now()

        return (6 downTo 0).map { offset ->

            val day =
                today.minusDays(offset.toLong())

            logs.count {
                it.date == day
            }
        }
    }

    private fun calculateCurrentStreak(
        logs: List<WorkoutDayLog>
    ): Int {

        var streak = 0
        var currentDate = LocalDate.now()

        while (
            logs.any {
                it.date == currentDate
            }
        ) {
            streak++
            currentDate =
                currentDate.minusDays(1)
        }

        return streak
    }

    private fun calculateLongestStreak(
        logs: List<WorkoutDayLog>
    ): Int {

        val dates =
            logs.map { it.date }
                .distinct()
                .sorted()

        if (dates.isEmpty()) return 0

        var current = 1
        var longest = 1

        for (i in 1 until dates.size) {

            if (
                dates[i - 1]
                    .plusDays(1) == dates[i]
            ) {
                current++
                longest =
                    maxOf(longest, current)
            } else {
                current = 1
            }
        }

        return longest
    }
}