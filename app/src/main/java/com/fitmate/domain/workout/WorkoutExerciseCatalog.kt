package com.fitmate.domain.workout

import com.fitmate.domain.model.WorkoutFocus

object WorkoutExerciseCatalog {
    fun namesFor(focus: WorkoutFocus): List<String> {
        return when (focus) {
            WorkoutFocus.PUSH -> listOf(
                "Push-Ups", "Bench Press", "Cable Chest Press",
                "Shoulder Raise", "Tricep Extension", "Bench Dips",
            )
            WorkoutFocus.PULL -> listOf(
                "Barbell Rear Delt Row", "Elevated Cable Rows", "Deadlift with Bands",
                "Incline Inner Biceps Curls", "Romanian Deadlift", "Dynamic Back Stretch",
            )
            WorkoutFocus.CHEST_BICEPS -> listOf(
                "Push-Ups", "Cable Chest Press", "Butterfly", "Incline Inner Biceps Curls",
            )
            WorkoutFocus.BACK_REAR_DELTS -> listOf(
                "Barbell Rear Delt Row", "Elevated Cable Rows",
                "Deadlift with Bands", "Dynamic Back Stretch",
            )
            WorkoutFocus.LEGS -> listOf(
                "Hack Squat", "Barbell Lunge", "Elevated Back Lunge",
                "Romanian Deadlift", "Cable Hip Adduction",
            )
            WorkoutFocus.SHOULDERS_TRICEPS -> listOf(
                "Shoulder Raise", "Body Tricep Press", "Tricep Extension", "Bench Dips",
            )
            WorkoutFocus.CORE_CONDITIONING -> listOf(
                "Plank", "Cable Crunch", "Mountain Climber", "Bent Knee Hip Raise", "Jumping Jack",
            )
            WorkoutFocus.CONDITIONING -> listOf(
                "Battling Ropes", "Jumping Jack", "Mountain Climber", "Bottoms Up", "Plank",
            )
            WorkoutFocus.FULL_BODY -> listOf(
                "Push-Ups", "Elevated Cable Rows", "Hack Squat", "Mountain Climber", "Plank",
            )
            WorkoutFocus.MOBILITY -> listOf(
                "Dynamic Back Stretch", "Elevated Back Lunge", "Shoulder Raise", "Plank",
            )
            WorkoutFocus.REST -> emptyList()
        }
    }
}
