package com.fitmate.data

import com.fitmate.domain.model.ActivityLevel
import com.fitmate.domain.model.ExperienceLevel
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.UserProfile
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.domain.workout.DifficultyLevel
import com.fitmate.domain.workout.EquipmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocalExerciseCatalogTest {
    private val assets = sequenceOf(
        File("app/src/main/assets"),
        File("src/main/assets"),
    ).first(File::isDirectory)

    @Test
    fun `catalog has stable unique ids and complete metadata`() {
        val exercises = LocalExerciseCatalog.exercises

        assertEquals(exercises.size, exercises.map { it.id }.distinct().size)
        assertTrue(exercises.size >= 57)
        exercises.forEach { exercise ->
            assertTrue(exercise.id.matches(Regex("[a-z0-9_]+")))
            assertTrue(exercise.primaryMuscles.isNotEmpty())
            assertTrue(exercise.goals.isNotEmpty())
            assertTrue(exercise.splitTypes.isNotEmpty())
            assertTrue(exercise.summaryInstructions.isNotBlank())
            assertEquals(exercise, LocalExerciseCatalog.findById(exercise.id))
            assertEquals(exercise, LocalExerciseCatalog.findByName(exercise.name.lowercase()))
        }
    }

    @Test
    fun `all mapped instruction assets exist`() {
        LocalExerciseCatalog.exercises.forEach { exercise ->
            exercise.instructionAsset?.let {
                assertTrue("Missing instructions for ${exercise.name}: $it", File(assets, "instructions/$it").isFile)
            }
        }
    }

    @Test
    fun `every packaged instruction asset is mapped`() {
        val mappedInstructions = LocalExerciseCatalog.exercises.mapNotNull { it.instructionAsset }.toSet()
        val packagedInstructions = File(assets, "instructions").listFiles().orEmpty()
            .filter(File::isFile)
            .map(File::getName)
            .toSet()

        assertEquals(packagedInstructions, mappedInstructions)
    }

    @Test
    fun `renamed exercises resolve to canonical entries without duplicates`() {
        val cableChestPress = LocalExerciseCatalog.findByName("Cable Chest Press")
        val declineReverseCrunch = LocalExerciseCatalog.findByName("Decline Reverse Crunch")

        assertEquals(cableChestPress, LocalExerciseCatalog.findByName("Chest Press"))
        assertEquals(declineReverseCrunch, LocalExerciseCatalog.findByName("Decline Crunch"))
        assertEquals("Cable Chest Press", LocalExerciseDatabase.exerciseByName("Chest Press")?.name)
        assertEquals("Decline Reverse Crunch", LocalExerciseDatabase.exerciseByName("Decline Crunch")?.name)
        assertTrue(LocalExerciseCatalog.exercises.none { it.name == "Chest Press" || it.name == "Decline Crunch" })
    }

    @Test
    fun `renamed dumbbell curl media preserves its instruction mapping`() {
        val dumbbellCurl = LocalExerciseCatalog.findByName("Dumbbell Curl")

        assertNotNull(dumbbellCurl)
        assertEquals("Dumbbell curl.jpg", dumbbellCurl?.detailMediaAsset)
        assertEquals("Dumbbell Curl.md", dumbbellCurl?.instructionAsset)
        assertNotNull(LocalExerciseDatabase.exerciseByName("Dumbbell Curl"))
    }

    @Test
    fun `bench press keeps its remote media key and packaged instructions`() {
        val benchPress = LocalExerciseCatalog.findByName("bench-press")

        assertNotNull(benchPress)
        assertEquals("bench press.webp", benchPress?.detailMediaAsset)
        assertEquals("Bench Press.md", benchPress?.instructionAsset)
        assertTrue(File(assets, "instructions/Bench Press.md").isFile)
    }

    @Test
    fun `recommendations honor beginner bodyweight constraints deterministically`() {
        val profile = UserProfile(
            goal = GoalType.FAT_LOSS,
            experienceLevel = ExperienceLevel.BEGINNER,
            activityLevel = ActivityLevel.LOW,
            equipment = setOf("Bodyweight only"),
        )

        val first = LocalExerciseCatalog.recommend(WorkoutFocus.FULL_BODY, profile)
        val second = LocalExerciseCatalog.recommend(WorkoutFocus.FULL_BODY, profile)

        assertTrue(first.isNotEmpty())
        assertEquals(first, second)
        assertTrue(first.all { it.equipment == EquipmentType.BODYWEIGHT })
        assertTrue(first.all { it.difficulty == DifficultyLevel.BEGINNER })
    }

    @Test
    fun `goal ranking prioritizes suitable movements`() {
        val fatLoss = LocalExerciseCatalog.recommend(
            WorkoutFocus.FULL_BODY,
            UserProfile(
                goal = GoalType.FAT_LOSS,
                experienceLevel = ExperienceLevel.INTERMEDIATE,
                activityLevel = ActivityLevel.MODERATE,
                equipment = setOf("Full gym"),
            )
        )
        val muscleGain = LocalExerciseCatalog.recommend(
            WorkoutFocus.PUSH,
            UserProfile(
                goal = GoalType.MUSCLE_GAIN,
                experienceLevel = ExperienceLevel.INTERMEDIATE,
                activityLevel = ActivityLevel.HIGH,
                equipment = setOf("Full gym", "Barbell"),
            )
        )
        val mobility = LocalExerciseCatalog.recommend(
            WorkoutFocus.MOBILITY,
            UserProfile(goal = GoalType.FLEXIBILITY_MOBILITY)
        )

        assertEquals(listOf("Jumping Jack", "Mountain Climber", "Battling Ropes"), fatLoss.take(3).map { it.name })
        assertEquals("Bench Press", muscleGain.first().name)
        assertEquals("Dynamic Back Stretch", mobility.first().name)
    }
}
