package com.fitmate.data

import com.fitmate.domain.model.ActivityLevel
import com.fitmate.domain.model.ExerciseLibraryEntry
import com.fitmate.domain.model.ExerciseMetricType
import com.fitmate.domain.model.ExperienceLevel
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.workout.DifficultyLevel
import com.fitmate.domain.workout.EquipmentType
import com.fitmate.domain.workout.ExerciseRecommendationSource
import com.fitmate.domain.workout.LocalExercise
import com.fitmate.domain.workout.MuscleGroup
import com.fitmate.domain.workout.WorkoutSplitType

object LocalExerciseCatalog : ExerciseRecommendationSource {
    val exercises: List<LocalExercise> by lazy {
        (LegacyExerciseEntries.entries + additionalEntries())
            .distinctBy(ExerciseLibraryEntry::name)
            .map(::enrich)
            .sortedBy(LocalExercise::id)
    }

    private val byId by lazy { exercises.associateBy(LocalExercise::id) }
    private val byNormalizedName by lazy { exercises.associateBy { normalizeId(it.name) } }

    fun findById(id: String): LocalExercise? = byId[normalizeId(id)]

    fun findByName(name: String): LocalExercise? = byNormalizedName[normalizeId(name)]

    fun forFocus(focus: WorkoutFocus): List<LocalExercise> {
        if (focus == WorkoutFocus.REST) return emptyList()
        val acceptedSplits = splitTypesForFocus(focus)
        return exercises.filter { exercise -> exercise.splitTypes.any(acceptedSplits::contains) }
    }

    fun recommend(focus: WorkoutFocus, profile: UserProfile): List<LocalExercise> {
        val candidates = forFocus(focus)
        if (candidates.isEmpty()) return emptyList()

        val maxDifficulty = when {
            profile.experienceLevel == ExperienceLevel.BEGINNER -> DifficultyLevel.BEGINNER
            profile.activityLevel == ActivityLevel.HIGH -> DifficultyLevel.ADVANCED
            else -> DifficultyLevel.INTERMEDIATE
        }
        val difficultyMatches = candidates.filter { it.difficulty.ordinal <= maxDifficulty.ordinal }
            .ifEmpty { candidates }
        val bodyweightOnly = profile.equipment.any {
            it.equals("No equipment", ignoreCase = true) ||
                it.equals("Bodyweight only", ignoreCase = true)
        }
        val equipmentMatches = if (bodyweightOnly) {
            difficultyMatches.filter { it.equipment == EquipmentType.BODYWEIGHT }
                .ifEmpty { difficultyMatches }
        } else {
            difficultyMatches
        }

        return equipmentMatches.sortedWith(
            compareByDescending<LocalExercise> { profile.goal in it.goals }
                .thenByDescending { goalPriority(it.name, profile.goal) }
                .thenByDescending { equipmentPreferenceScore(it.equipment, profile.equipment) }
                .thenBy { it.difficulty.ordinal }
                .thenBy(LocalExercise::id)
        )
    }

    override fun recommendationNames(focus: WorkoutFocus, profile: UserProfile): List<String> =
        recommend(focus, profile).map(LocalExercise::name)

    private fun enrich(entry: ExerciseLibraryEntry): LocalExercise {
        val splitTypes = splitTypesForName(entry.name)
        return LocalExercise(
            id = normalizeId(entry.name),
            name = entry.name,
            mediaAsset = mediaAssetOverrides[entry.name]
                ?: entry.postureImage.takeIf(String::isNotBlank),
            detailMediaAsset = detailAssetOverrides[entry.name]
                ?: entry.detailGifAsset.takeIf(String::isNotBlank),
            instructionAsset = instructionAssetOverrides[entry.name]
                ?: entry.instructionMarkdownAsset.takeIf(String::isNotBlank),
            primaryMuscles = primaryMuscles(entry.name, entry.muscleGroup),
            secondaryMuscles = secondaryMuscles(entry.name),
            equipment = equipmentFor(entry.name),
            difficulty = difficultyFor(entry.name),
            goals = goalsFor(entry.name, splitTypes),
            splitTypes = splitTypes,
            summaryInstructions = entry.instructions.ifBlank { DEFAULT_INSTRUCTION_FALLBACK },
            metricType = entry.metricType,
            defaultAmount = entry.defaultAmount,
            minAmount = entry.minAmount,
            maxAmount = entry.maxAmount,
            easyMaxWorkload = entry.easyMaxWorkload,
            mediumMaxWorkload = entry.mediumMaxWorkload,
        )
    }

    private fun splitTypesForFocus(focus: WorkoutFocus): Set<WorkoutSplitType> = when (focus) {
        WorkoutFocus.PUSH,
        WorkoutFocus.CHEST_BICEPS,
        WorkoutFocus.SHOULDERS_TRICEPS -> setOf(WorkoutSplitType.PUSH)
        WorkoutFocus.PULL,
        WorkoutFocus.BACK_REAR_DELTS -> setOf(WorkoutSplitType.PULL)
        WorkoutFocus.LEGS -> setOf(WorkoutSplitType.LEGS_SHOULDERS, WorkoutSplitType.FULL_BODY)
        WorkoutFocus.CORE_CONDITIONING -> setOf(WorkoutSplitType.ARMS_ABS, WorkoutSplitType.FULL_BODY)
        WorkoutFocus.CONDITIONING -> setOf(WorkoutSplitType.FULL_BODY)
        WorkoutFocus.FULL_BODY -> setOf(WorkoutSplitType.FULL_BODY)
        WorkoutFocus.MOBILITY -> setOf(WorkoutSplitType.MOBILITY_RECOVERY)
        WorkoutFocus.ARMS_ABS -> setOf(WorkoutSplitType.ARMS_ABS)
        WorkoutFocus.LEGS_SHOULDERS -> setOf(WorkoutSplitType.LEGS_SHOULDERS)
        WorkoutFocus.UPPER_BODY_POWER -> setOf(WorkoutSplitType.UPPER_BODY_POWER)
        WorkoutFocus.ARM_SPECIALIZATION_WEAK_POINTS ->
            setOf(WorkoutSplitType.ARM_SPECIALIZATION_WEAK_POINTS)
        WorkoutFocus.REST -> emptySet()
    }

    private fun splitTypesForName(name: String): Set<WorkoutSplitType> = buildSet {
        splitAssignments.forEach { (split, names) -> if (name in names) add(split) }
        if (isEmpty()) add(WorkoutSplitType.FULL_BODY)
    }

    private fun goalsFor(name: String, splits: Set<WorkoutSplitType>): Set<GoalType> = buildSet {
        add(GoalType.LEAN_BODY)
        if (WorkoutSplitType.MOBILITY_RECOVERY in splits) {
            add(GoalType.FLEXIBILITY_MOBILITY)
            add(GoalType.STRESS_RELIEF)
        }
        if (WorkoutSplitType.FULL_BODY in splits || name in conditioningExercises) {
            add(GoalType.FAT_LOSS)
            add(GoalType.CARDIO_STAMINA)
            add(GoalType.STRESS_RELIEF)
        }
        if (splits.any { it != WorkoutSplitType.MOBILITY_RECOVERY } && name !in conditioningExercises) {
            add(GoalType.MUSCLE_GAIN)
        }
    }

    private fun primaryMuscles(name: String, legacyGroup: String): Set<MuscleGroup> {
        val text = "$name $legacyGroup".lowercase()
        return buildSet {
            if (listOf("chest", "bench press", "butterfly", "flyes", "push-up").any(text::contains)) add(MuscleGroup.CHEST)
            if (listOf("back", "row", "pulldown", "dead hang", "shrug").any(text::contains)) add(MuscleGroup.BACK)
            if (listOf("shoulder", "lateral raise", "upright", "rear delt", "face pull").any(text::contains)) add(MuscleGroup.SHOULDERS)
            if (listOf("bicep", "curl").any(text::contains) && !text.contains("wrist")) add(MuscleGroup.BICEPS)
            if (listOf("tricep", "pushdown", "dips").any(text::contains)) add(MuscleGroup.TRICEPS)
            if (listOf("forearm", "wrist", "farmer", "reverse curl").any(text::contains)) add(MuscleGroup.FOREARMS)
            if (listOf("core", "plank", "crunch", "sit-up", "leg raise", "bottoms up").any(text::contains)) add(MuscleGroup.CORE)
            if (listOf("quad", "squat", "leg press", "lunge").any(text::contains)) add(MuscleGroup.QUADRICEPS)
            if (listOf("hamstring", "romanian", "posterior").any(text::contains)) add(MuscleGroup.HAMSTRINGS)
            if (listOf("glute", "hip", "lunge", "squat").any(text::contains)) add(MuscleGroup.GLUTES)
            if (text.contains("calf")) add(MuscleGroup.CALVES)
            if (text.contains("mobility") || text.contains("stretch")) add(MuscleGroup.MOBILITY)
            if (isEmpty()) add(MuscleGroup.FULL_BODY)
        }
    }

    private fun secondaryMuscles(name: String): Set<MuscleGroup> = when {
        name in setOf("Push-Ups", "Bench Press", "Chest Press", "Machine Chest Press") ->
            setOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)
        name.contains("Row") || name.contains("Pulldown") ->
            setOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
        name.contains("Squat") || name.contains("Lunge") || name == "Leg Press" ->
            setOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS)
        name == "Farmer Carries" -> setOf(MuscleGroup.CORE, MuscleGroup.SHOULDERS)
        else -> emptySet()
    }

    private fun equipmentFor(name: String): EquipmentType = when {
        name in bodyweightExercises -> EquipmentType.BODYWEIGHT
        name.contains("Dumbbell", true) || name == "Farmer Carries" || name == "Tricep Kickbacks" -> EquipmentType.DUMBBELLS
        name.contains("Barbell", true) || name.contains("EZ Bar", true) || name == "Bench Press" || name == "Romanian Deadlift" -> EquipmentType.BARBELL
        name.contains("Cable", true) || name.contains("Pushdown", true) || name.contains("Wrist Roller", true) -> EquipmentType.CABLE
        name.contains("Bands", true) -> EquipmentType.RESISTANCE_BAND
        name == "Battling Ropes" -> EquipmentType.BATTLE_ROPE
        else -> EquipmentType.MACHINE
    }

    private fun difficultyFor(name: String): DifficultyLevel = when {
        name in advancedExercises -> DifficultyLevel.ADVANCED
        name in beginnerExercises -> DifficultyLevel.BEGINNER
        else -> DifficultyLevel.INTERMEDIATE
    }

    private fun equipmentPreferenceScore(type: EquipmentType, equipment: Set<String>): Int {
        val normalized = equipment.joinToString(" ").lowercase()
        return when (type) {
            EquipmentType.BODYWEIGHT -> if ("mat" in normalized || "bodyweight" in normalized) 2 else 1
            EquipmentType.DUMBBELLS -> if ("dumbbell" in normalized) 2 else 0
            EquipmentType.BARBELL -> if ("barbell" in normalized) 2 else 0
            EquipmentType.CABLE -> if ("cable" in normalized) 2 else 0
            EquipmentType.MACHINE -> if ("machine" in normalized || "gym" in normalized) 2 else 0
            EquipmentType.RESISTANCE_BAND -> if ("band" in normalized) 2 else 0
            EquipmentType.BATTLE_ROPE -> if ("rope" in normalized) 2 else 0
        }
    }

    private fun goalPriority(name: String, goal: GoalType): Int {
        val preferred = when (goal) {
            GoalType.FAT_LOSS,
            GoalType.CARDIO_STAMINA -> conditioningPriority
            GoalType.MUSCLE_GAIN -> muscleGainPriority
            GoalType.LEAN_BODY -> leanBodyPriority
            GoalType.FLEXIBILITY_MOBILITY -> mobilityPriority
            GoalType.STRESS_RELIEF -> stressReliefPriority
        }
        val index = preferred.indexOf(name)
        return if (index < 0) 0 else preferred.size - index
    }

    private fun normalizeId(value: String): String = value
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')

    private fun additionalEntries(): List<ExerciseLibraryEntry> = listOf(
        entry("Bayesian Cable Curls", "Biceps", "Keep the shoulder fixed and curl through a full controlled range.", "Bayesian-Cable-Curl.gif", "Bayesian Cable Curls.gif", "Bayesian-Cable-Curl.md"),
        entry("Bulgarian Split Squat", "Quadriceps + Glutes", "Stay balanced, lower under control, and drive through the front foot.", "bulgarian-split-squat.jpg", "Bulgarian Split Squat.gif", "Bulgarian Split Squats.md"),
        entry("Cable Curls", "Biceps", "Keep your elbows still and squeeze the biceps without leaning back.", "Cable-Curls.jpg", "Cable Curls.gif", "Cable-Curls.md"),
        entry("Cable Flyes", "Chest", "Use a soft elbow bend and bring the hands together with chest control.", "Cables-Flyes.jpeg", "cable flyes.gif", "Cable-Flyes.md"),
        entry("Cable Lateral Raise", "Shoulders", "Raise to shoulder height without shrugging or swinging.", "Cable Lateral Raises.gif", "Cable Lateral Raise.gif", "Cable Lateral Raises.md"),
        entry("Chest Supported Machine Row", "Back", "Keep the chest supported and pull the elbows behind the torso.", "Chest-Supported-Machine-Row.jpg", "Chest Supported Machine Row.gif", "Chest-Supported-Machine-Row.md"),
        entry("Dead Hangs", "Back + Forearms", "Hang with controlled shoulders and breathe steadily.", "Dead Hangs.webp", "Dead Hangs.webp", "Dead Hangs.md", ExerciseMetricType.SECONDS, 30, 15, 90),
        entry("Dumbbell Curl", "Biceps", "Curl without swinging and lower the dumbbells slowly.", "Dumbbell Curl.webp", "Dumbbell Curl.gif", "Dumbbell Curl.md"),
        entry("Dumbbell Lateral Raises", "Shoulders", "Lead with the elbows and stop around shoulder height.", "Dumbbell-Lateral-Raises.webp", "Dumbbell Lateral Raises.gif", "Dumbbell-Lateral-Raises.md"),
        entry("Dumbbell Shrugs", "Back + Shoulders", "Lift the shoulders straight up, pause, and lower fully.", "Dumbbell Shrugs.webp", "Dumbbell Shrugs.gif", "Dumbbell Shrugs.md"),
        entry("EZ Bar Curl", "Biceps", "Keep the wrists neutral and curl without moving the upper arms.", "EZ-Bar-Curl.jpg", "EZ Bar Curl.gif", "EZ-Bar-Curl.md"),
        entry("Face Pull", "Shoulders + Back", "Pull toward the face and rotate the hands apart at the finish.", "Face-Pull.webp", "Face Pull.gif", "Face-Pulls.md"),
        entry("Farmer Carries", "Full Body + Forearms", "Walk tall with a braced core and firm grip.", "Farmer Carries.gif", "Farmer Carries.gif", "Farmer Carries.md", ExerciseMetricType.SECONDS, 30, 15, 90),
        entry("Hammer Curl", "Biceps + Forearms", "Keep a neutral grip and avoid swinging the torso.", "Hammer-Curls.webp", "Hammer Curl.gif", "Hammer-Curls.md"),
        entry("Hamstring Curl", "Hamstrings", "Curl smoothly, pause at peak contraction, and control the return.", "Hamstring-Curl.webp", "Hamstring curl.gif", "Hamstring Curl.md"),
        entry("Hanging Leg Raises", "Core", "Brace the torso and raise the legs without swinging.", "Hanging-Leg-Raises.jpg", "Hanging Leg Raises.gif", "Hanging-Leg-Raises.md"),
        entry("Leg Press", "Quadriceps + Glutes", "Lower to a comfortable depth and press without locking the knees.", "Leg-Press.jpeg", "Leg-Press.gif", "Leg Press.md"),
        entry("Machine Chest Press", "Chest", "Keep the shoulders down and press with steady chest tension.", "Machine-Chest-Press.jpeg", "Machine Chest Press.gif", "Machine-Chest-Press.md"),
        entry("Overhead Cable Tricep Extensions", "Triceps", "Keep the elbows pointed forward and extend fully without arching.", "Overhead-Cable-Tricept-Extensions.webp", "Overhead Cable Tricep Extensions.gif", "Overhead-Cable-Tricept-Extensions.md"),
        entry("Rear Delt Flyes", "Shoulders + Back", "Open the arms with rear-delt control and avoid shrugging.", "Rear-Delt-Flyes.webp", "Rear Delt Flyes.gif", "Rear-Delt-Flyes.md"),
        entry("Reverse Curls", "Forearms + Biceps", "Use an overhand grip and keep the wrists straight.", "Reverse-Curls.gif", "Reverse Curls.gif", "Reverse-Curls.md"),
        entry("Reverse Wrist Curl", "Forearms", "Move only at the wrists through a controlled range.", "Reverse-Wrist-Curl.gif", "Reverse Wrist Curl.gif", "Reverse-Wrist-Curls.md"),
        entry("Rope Pushdowns", "Triceps", "Keep the elbows tucked and separate the rope at full extension.", "Rope-Pushdowns.jpg", "Rope Pushdowns.gif", "Rope-Pushdowns.md"),
        entry("Squats", "Quadriceps + Glutes", "Brace the trunk, sit between the hips, and drive through the feet.", "Squats.gif", "Squats.gif", "Squats.md"),
        entry("Standing Calf Raises", "Calves", "Rise fully onto the toes, pause, and lower through the heel.", "Standing Calf raises.gif", "Standing Calf raises.gif", "Standing Calf Raises.md"),
        entry("Straight Arm Pulldown", "Back", "Keep the arms nearly straight and pull from the lats.", "Straight-Arm-Pulldowm.jpg", "Straight-Arm-Pulldown.gif", "Straight-Arm-Pulldown.md"),
        entry("Tricep Kickbacks", "Triceps", "Hold the upper arm still and extend the elbow completely.", "Tricep Kickback.webp", "Tricep Kickbacks.gif", "Tricep Kickback.md"),
        entry("Upright Row", "Shoulders", "Lead with the elbows and stop before the shoulders feel pinched.", "Upright row.jpg", "Upright Row.gif", "Upright Row.md"),
        entry("Wide Grip Lat Pulldown", "Back", "Pull toward the upper chest while keeping the torso stable.", "wide-grip-lat-pulldown.webp", "Wide Grip Lat Pulldown.gif", "Wide-Grip-Lat-pulldown.md"),
        entry("Wrist Roller", "Forearms", "Keep the arms steady and roll the load using controlled wrist turns.", "Wrist Roller.webp", "Wrist Roller.gif", "Wrist Roller.md", ExerciseMetricType.SECONDS, 30, 15, 90),
    )

    private fun entry(
        name: String,
        muscleGroup: String,
        instructions: String,
        postureImage: String,
        detailGifAsset: String,
        instructionMarkdownAsset: String,
        metricType: ExerciseMetricType = ExerciseMetricType.REPS,
        defaultAmount: Int = 12,
        minAmount: Int = 6,
        maxAmount: Int = 20,
    ) = ExerciseLibraryEntry(
        name = name,
        muscleGroup = muscleGroup,
        instructions = instructions,
        postureImage = postureImage,
        detailGifAsset = detailGifAsset,
        instructionMarkdownAsset = instructionMarkdownAsset,
        metricType = metricType,
        defaultAmount = defaultAmount,
        minAmount = minAmount,
        maxAmount = maxAmount,
    )

    private val detailAssetOverrides = mapOf(
        "Bench Press" to "Bench Press.gif",
        "Cable Chest Press" to "Cable Chest Press.gif",
        "Incline Inner Biceps Curls" to "Incline Inner Biceps Curls.gif",
        "Barbell Rear Delt Row" to "Barbell Rear Delt Row.gif",
        "Elevated Cable Rows" to "Elevated Cable Rows.gif",
        "Deadlift with Bands" to "Deadlift with Bands.gif",
        "Barbell Lunge" to "Barbell Lunge.gif",
        "Cable Hip Adduction" to "Cable Hip Adduction.gif",
        "Shoulder Raise" to "Shoulder Raise.gif",
        "Body Tricep Press" to "Body Tricep Press.gif",
        "Bench Dips" to "Bench Dips.gif",
        "Cable Crunch" to "Cable Crunch.gif",
        "Decline Reverse Crunch" to "Decline Reverse Crunch.gif",
        "Bent Knee Hip Raise" to "Bent Knee Hip Raise.gif",
        "Battling Ropes" to "Battling Ropes.gif",
        "Bottoms Up" to "Bottoms Up.gif",
        "Romanian Deadlift" to "Romanian-Deadlift.gif",
    )

    private val mediaAssetOverrides = mapOf(
        "Romanian Deadlift" to "Romanian Deadlift.jpg",
    )

    private val instructionAssetOverrides = mapOf(
        "Bench Press" to "Bench Press.md",
        "Romanian Deadlift" to "Romanian Deadlift instructions.md",
    )

    private val splitAssignments = mapOf(
        WorkoutSplitType.PUSH to setOf(
            "Push-Ups", "Cable Chest Press", "Bench Press", "Chest Press", "Butterfly",
            "Cable Flyes", "Machine Chest Press", "Shoulder Raise", "Cable Lateral Raise",
            "Dumbbell Lateral Raises", "Body Tricep Press", "Tricep Extension", "Bench Dips",
            "Overhead Cable Tricep Extensions", "Rope Pushdowns", "Tricep Kickbacks",
        ),
        WorkoutSplitType.PULL to setOf(
            "Barbell Rear Delt Row", "Elevated Cable Rows", "Chest Supported Machine Row",
            "Wide Grip Lat Pulldown", "Straight Arm Pulldown", "Face Pull", "Rear Delt Flyes",
            "Dead Hangs", "Dumbbell Shrugs", "Incline Inner Biceps Curls", "Bayesian Cable Curls",
            "Cable Curls", "Dumbbell Curl", "EZ Bar Curl", "Hammer Curl", "Reverse Curls",
        ),
        WorkoutSplitType.FULL_BODY to setOf(
            "Push-Ups", "Hack Squat", "Barbell Lunge", "Elevated Back Lunge", "Bulgarian Split Squat",
            "Leg Press", "Squats", "Romanian Deadlift", "Deadlift with Bands", "Farmer Carries",
            "Mountain Climber", "Jumping Jack", "Battling Ropes", "Bottoms Up", "Plank",
        ),
        WorkoutSplitType.MOBILITY_RECOVERY to setOf(
            "Dynamic Back Stretch", "Elevated Back Lunge", "Dead Hangs", "Shoulder Raise", "Plank",
        ),
        WorkoutSplitType.ARMS_ABS to setOf(
            "Incline Inner Biceps Curls", "Bayesian Cable Curls", "Cable Curls", "Dumbbell Curl",
            "EZ Bar Curl", "Hammer Curl", "Reverse Curls", "Body Tricep Press", "Tricep Extension",
            "Bench Dips", "Overhead Cable Tricep Extensions", "Rope Pushdowns", "Tricep Kickbacks",
            "Plank", "Cable Crunch", "Decline Reverse Crunch", "Bent Knee Hip Raise", "Sit-Up",
            "Decline Crunch", "Hanging Leg Raises",
        ),
        WorkoutSplitType.LEGS_SHOULDERS to setOf(
            "Hack Squat", "Barbell Lunge", "Elevated Back Lunge", "Bulgarian Split Squat", "Leg Press",
            "Squats", "Romanian Deadlift", "Hamstring Curl", "Standing Calf Raises", "Cable Hip Adduction",
            "Shoulder Raise", "Cable Lateral Raise", "Dumbbell Lateral Raises", "Upright Row",
        ),
        WorkoutSplitType.UPPER_BODY_POWER to setOf(
            "Push-Ups", "Bench Press", "Chest Press", "Cable Chest Press", "Machine Chest Press",
            "Barbell Rear Delt Row", "Elevated Cable Rows", "Chest Supported Machine Row",
            "Wide Grip Lat Pulldown", "Shoulder Raise", "Bench Dips", "Farmer Carries",
        ),
        WorkoutSplitType.ARM_SPECIALIZATION_WEAK_POINTS to setOf(
            "Incline Inner Biceps Curls", "Bayesian Cable Curls", "Cable Curls", "Dumbbell Curl",
            "EZ Bar Curl", "Hammer Curl", "Reverse Curls", "Reverse Wrist Curl", "Wrist Roller",
            "Body Tricep Press", "Tricep Extension", "Overhead Cable Tricep Extensions",
            "Rope Pushdowns", "Tricep Kickbacks", "Bench Dips",
        ),
    )

    private val bodyweightExercises = setOf(
        "Push-Ups", "Bench Dips", "Body Tricep Press", "Dynamic Back Stretch", "Elevated Back Lunge",
        "Bulgarian Split Squat", "Bottoms Up", "Mountain Climber", "Jumping Jack", "Plank", "Sit-Up",
        "Decline Crunch", "Decline Reverse Crunch", "Bent Knee Hip Raise", "Hanging Leg Raises", "Dead Hangs",
    )

    private val conditioningExercises = setOf(
        "Battling Ropes", "Jumping Jack", "Mountain Climber", "Bottoms Up", "Farmer Carries",
    )

    private val conditioningPriority = listOf(
        "Jumping Jack", "Mountain Climber", "Battling Ropes", "Bottoms Up",
        "Farmer Carries", "Squats", "Push-Ups", "Plank",
    )

    private val muscleGainPriority = listOf(
        "Bench Press", "Machine Chest Press", "Hack Squat", "Squats", "Romanian Deadlift",
        "Deadlift with Bands", "Barbell Rear Delt Row", "Wide Grip Lat Pulldown", "Leg Press",
    )

    private val leanBodyPriority = listOf(
        "Push-Ups", "Squats", "Elevated Cable Rows", "Barbell Lunge",
        "Mountain Climber", "Shoulder Raise", "Plank",
    )

    private val mobilityPriority = listOf(
        "Dynamic Back Stretch", "Elevated Back Lunge", "Dead Hangs", "Shoulder Raise", "Plank",
    )

    private val stressReliefPriority = listOf(
        "Dynamic Back Stretch", "Plank", "Elevated Back Lunge", "Dead Hangs",
        "Shoulder Raise", "Farmer Carries",
    )

    private val advancedExercises = setOf(
        "Bench Press", "Hack Squat", "Barbell Lunge", "Bulgarian Split Squat", "Romanian Deadlift",
        "Deadlift with Bands", "Hanging Leg Raises", "Farmer Carries",
    )

    private val beginnerExercises = bodyweightExercises + setOf(
        "Butterfly", "Machine Chest Press", "Leg Press", "Hamstring Curl", "Standing Calf Raises",
        "Cable Curls", "Rope Pushdowns", "Cable Lateral Raise",
    )

    const val DEFAULT_INSTRUCTION_FALLBACK =
        "Detailed instructions are not available for this exercise yet. Use controlled form and stop if you feel pain."
}

object LocalExerciseDatabase {
    val exercises: List<ExerciseLibraryEntry> = LocalExerciseCatalog.exercises
        .map(LocalExercise::toLibraryEntry)

    private val byNormalizedName = exercises.associateBy { normalizeName(it.name) }

    fun exerciseByName(name: String): ExerciseLibraryEntry? = byNormalizedName[normalizeName(name)]

    private fun normalizeName(value: String): String = value
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
}
