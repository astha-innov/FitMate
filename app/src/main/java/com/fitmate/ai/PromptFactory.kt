package com.fitmate.ai

import com.fitmate.domain.model.GoalMetrics
import com.fitmate.domain.model.MealSlot
import com.fitmate.domain.model.UserProfile

object PromptFactory {
    fun personalizationSystemPrompt(): String = """
        You are FitMate, an AI fitness coach for students and young professionals.
        Return only valid JSON.
        Create:
        - metrics
        - reasoning
        - dietRecommendation
        - workoutPlan
        - aiSummary
    """.trimIndent()

    fun personalizationUserPrompt(profile: UserProfile): String = """
        Build a persistent personalized fitness plan for this user.
        Age: ${profile.age}
        Height cm: ${profile.heightCm}
        Weight kg: ${profile.weightKg}
        Gender: ${profile.gender}
        Activity level: ${profile.activityLevel.label}
        Goal: ${profile.goal.label}
        Food preference: ${profile.foodPreference.label}
        Budget INR: ${profile.budgetInr}
        Workout minutes: ${profile.workoutMinutes}
        Experience: ${profile.experienceLevel.label}
        Equipment: ${profile.equipment.joinToString()}
        
        Return JSON with this shape:
        {
          "metrics": {
            "caloriesTarget": 0,
            "proteinTarget": 0,
            "waterLitersTarget": 0.0,
            "calorieMode": "",
            "bmi": 0.0
          },
          "reasoning": {
            "summary": "",
            "calorieReasoning": "",
            "proteinReasoning": "",
            "waterReasoning": "",
            "coachingNotes": ["", ""]
          },
          "dietRecommendation": {
            "title": "",
            "meals": ["", ""],
            "cheapProteins": ["", ""],
            "avoid": ["", ""]
          },
          "workoutPlan": {
            "title": "",
            "split": "",
            "exercises": ["", ""],
            "durationLabel": ""
          },
          "aiSummary": ""
        }
    """.trimIndent()

    fun mealAnalysisSystemPrompt(): String = """
        You estimate nutrition for a described meal and return only valid JSON.
    """.trimIndent()

    fun mealAnalysisUserPrompt(
        profile: UserProfile,
        slot: MealSlot,
        description: String,
        metrics: GoalMetrics,
    ): String = """
        User goal: ${profile.goal.label}
        Food preference: ${profile.foodPreference.label}
        Meal slot: ${slot.label}
        Meal description: $description
        Daily calorie target: ${metrics.caloriesTarget}
        Daily protein target: ${metrics.proteinTarget}
        
        Return JSON:
        {
          "estimatedCalories": 0,
          "estimatedProtein": 0,
          "estimatedCarbs": 0,
          "estimatedFats": 0,
          "suggestions": ["", ""],
          "avoid": ["", ""],
          "reasoning": ""
        }
    """.trimIndent()
}
