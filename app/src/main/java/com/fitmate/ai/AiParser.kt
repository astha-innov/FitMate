package com.fitmate.ai

import com.fitmate.domain.model.DietRecommendation
import com.fitmate.domain.model.GoalMetrics
import com.fitmate.domain.model.GoalReasoning
import com.fitmate.domain.model.MealAnalysis
import com.fitmate.domain.model.MealSlot
import com.fitmate.domain.model.PersonalizedPlan
import com.fitmate.domain.model.WorkoutPlan
import org.json.JSONArray
import org.json.JSONObject

object AiParser {
    fun parsePersonalizedPlan(json: JSONObject): PersonalizedPlan {
        val metricsJson = json.getJSONObject("metrics")
        val reasoningJson = json.getJSONObject("reasoning")
        val dietJson = json.getJSONObject("dietRecommendation")
        val workoutJson = json.getJSONObject("workoutPlan")
        return PersonalizedPlan(
            metrics = GoalMetrics(
                caloriesTarget = metricsJson.getInt("caloriesTarget"),
                proteinTarget = metricsJson.getInt("proteinTarget"),
                waterLitersTarget = metricsJson.getDouble("waterLitersTarget"),
                calorieMode = metricsJson.getString("calorieMode"),
                bmi = metricsJson.getDouble("bmi"),
            ),
            reasoning = GoalReasoning(
                summary = reasoningJson.getString("summary"),
                calorieReasoning = reasoningJson.getString("calorieReasoning"),
                proteinReasoning = reasoningJson.getString("proteinReasoning"),
                waterReasoning = reasoningJson.getString("waterReasoning"),
                coachingNotes = toStringList(reasoningJson.getJSONArray("coachingNotes")),
            ),
            dietRecommendation = DietRecommendation(
                title = dietJson.getString("title"),
                meals = toStringList(dietJson.getJSONArray("meals")),
                cheapProteins = toStringList(dietJson.getJSONArray("cheapProteins")),
                avoid = toStringList(dietJson.getJSONArray("avoid")),
            ),
            workoutPlan = WorkoutPlan(
                title = workoutJson.getString("title"),
                split = workoutJson.getString("split"),
                exercises = toStringList(workoutJson.getJSONArray("exercises")),
                durationLabel = workoutJson.getString("durationLabel"),
            ),
            aiSummary = json.getString("aiSummary"),
        )
    }

    fun parseMealAnalysis(slot: MealSlot, description: String, json: JSONObject): MealAnalysis = MealAnalysis(
        slot = slot,
        description = description,
        estimatedCalories = json.getInt("estimatedCalories"),
        estimatedProtein = json.getInt("estimatedProtein"),
        estimatedCarbs = json.optInt("estimatedCarbs", 0),
        estimatedFats = json.optInt("estimatedFats", 0),
        suggestions = toStringList(json.optJSONArray("suggestions") ?: JSONArray()),
        avoid = toStringList(json.optJSONArray("avoid") ?: JSONArray()),
        reasoning = json.optString("reasoning", ""),
    )

    private fun toStringList(array: JSONArray): List<String> = buildList {
        for (index in 0 until array.length()) add(array.getString(index))
    }
}
