package com.fitmate.domain.analytics

data class AnalyticsSnapshot(

    val currentStreak: Int = 0,

    val longestStreak: Int = 0,

    val totalWorkouts: Int = 0,

    val completedSets: Int = 0,

    val totalSets: Int = 0,

    val completionRate: Int = 0,

    val averageWorkoutDurationMinutes: Int = 0,

    val fitnessScore: Int = 0,

    val weeklyActivity: List<Int> = emptyList()
)
