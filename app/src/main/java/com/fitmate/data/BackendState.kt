package com.fitmate.data

import com.fitmate.domain.model.AiConfig
import com.fitmate.domain.model.AppThemeMode
import com.fitmate.domain.model.DisciplineState
import com.fitmate.domain.model.GoalProgress
import com.fitmate.domain.model.MealLog
import com.fitmate.domain.model.PersonalizedPlan
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WeeklyWorkoutSchedule

data class BackendState(
    val profile: UserProfile? = null,
    val aiConfig: AiConfig? = null,
    val themeMode: AppThemeMode? = null,
    val setupCompleted: Boolean? = null,
    val personalizedPlan: PersonalizedPlan? = null,
    val discipline: DisciplineState? = null,
    val todayProgress: GoalProgress? = null,
    val mealLogs: List<MealLog>? = null,
    val workoutSchedule: WeeklyWorkoutSchedule? = null,
)
