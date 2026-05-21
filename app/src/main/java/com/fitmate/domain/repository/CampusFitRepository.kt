package com.fitmate.domain.repository

import com.fitmate.domain.model.AiConfig
import com.fitmate.domain.model.AppThemeMode
import com.fitmate.domain.model.DisciplineState
import com.fitmate.domain.model.GoalProgress
import com.fitmate.domain.model.MealAnalysis
import com.fitmate.domain.model.MealLog
import com.fitmate.domain.model.PersonalizedPlan
import com.fitmate.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface CampusFitRepository {
    val profile: StateFlow<UserProfile>
    val aiConfig: StateFlow<AiConfig>
    val themeMode: StateFlow<AppThemeMode>
    val setupCompleted: StateFlow<Boolean>
    val personalizedPlan: StateFlow<PersonalizedPlan?>
    val discipline: StateFlow<DisciplineState>
    val todayProgress: StateFlow<GoalProgress>
    val mealLogs: StateFlow<List<MealLog>>
    val latestMealAnalysis: StateFlow<MealAnalysis?>

    fun updateProfile(profile: UserProfile)
    fun updateAiConfig(config: AiConfig)
    fun updateThemeMode(mode: AppThemeMode)
    fun toggleReminders()
    fun saveMealAnalysis(analysis: MealAnalysis)
    fun addWater(amountLiters: Double)
    fun savePersonalizedPlan(plan: PersonalizedPlan)
    fun markSetupCompleted(completed: Boolean)
    fun resetSetup()
}
