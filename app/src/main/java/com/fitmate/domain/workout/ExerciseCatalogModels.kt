package com.fitmate.domain.workout

import com.fitmate.domain.model.ExerciseLibraryEntry
import com.fitmate.domain.model.ExerciseMetricType
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WorkoutFocus

enum class WorkoutSplitType {
    PUSH,
    PULL,
    FULL_BODY,
    MOBILITY_RECOVERY,
    ARMS_ABS,
    LEGS_SHOULDERS,
    UPPER_BODY_POWER,
    ARM_SPECIALIZATION_WEAK_POINTS,
}

enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}

enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    CORE,
    QUADRICEPS,
    HAMSTRINGS,
    GLUTES,
    CALVES,
    FULL_BODY,
    MOBILITY,
}

enum class EquipmentType {
    BODYWEIGHT,
    DUMBBELLS,
    BARBELL,
    CABLE,
    MACHINE,
    RESISTANCE_BAND,
    BATTLE_ROPE,
}

interface ExerciseRecommendationSource {
    fun recommendationNames(focus: WorkoutFocus, profile: UserProfile): List<String>
}

data class LocalExercise(
    val id: String,
    val name: String,
    val mediaAsset: String?,
    val detailMediaAsset: String?,
    val instructionAsset: String?,
    val primaryMuscles: Set<MuscleGroup>,
    val secondaryMuscles: Set<MuscleGroup>,
    val equipment: EquipmentType,
    val difficulty: DifficultyLevel,
    val goals: Set<GoalType>,
    val splitTypes: Set<WorkoutSplitType>,
    val summaryInstructions: String,
    val metricType: ExerciseMetricType = ExerciseMetricType.REPS,
    val defaultAmount: Int = 12,
    val minAmount: Int = 6,
    val maxAmount: Int = 20,
    val easyMaxWorkload: Int = 36,
    val mediumMaxWorkload: Int = 72,
) {
    fun toLibraryEntry(): ExerciseLibraryEntry = ExerciseLibraryEntry(
        name = name,
        muscleGroup = primaryMuscles.joinToString(" + ") { it.displayName() },
        instructions = summaryInstructions,
        postureImage = mediaAsset.orEmpty(),
        detailGifAsset = detailMediaAsset.orEmpty(),
        instructionMarkdownAsset = instructionAsset.orEmpty(),
        metricType = metricType,
        defaultAmount = defaultAmount,
        minAmount = minAmount,
        maxAmount = maxAmount,
        easyMaxWorkload = easyMaxWorkload,
        mediumMaxWorkload = mediumMaxWorkload,
    )
}

private fun MuscleGroup.displayName(): String = name
    .lowercase()
    .replace('_', ' ')
    .replaceFirstChar(Char::uppercase)
