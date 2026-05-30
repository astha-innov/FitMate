package com.fitmate.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitmate.domain.model.ActivityLevel

import com.fitmate.domain.model.ExperienceLevel
import com.fitmate.domain.model.FoodPreference
import com.fitmate.domain.model.GoalType
import com.fitmate.domain.model.UserProfile

// --- Color Palette ---
private val FitMateBlack = Color(0xFF0F0F11)
private val FitMateDarkGrey = Color(0xFF1C1C1E)
private val FitMateEmerald = Color(0xFF00E676)
private val FitMateWhite = Color(0xFFFFFFFF)
private val FitMateGlass = Color(0xFFFFFFFF).copy(alpha = 0.08f)
private val FitMateGlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuestionsScreen(
    onContinue: (UserProfile) -> Unit
) {
    // State Management
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val totalSteps = 8

    // Data Collection States
    var gender by rememberSaveable { mutableStateOf("Male") }
    var age by rememberSaveable { mutableFloatStateOf(25f) }
    var height by rememberSaveable { mutableFloatStateOf(170f) }
    var weight by rememberSaveable { mutableFloatStateOf(65f) }
    var selectedGoal by rememberSaveable { mutableStateOf(GoalType.entries.firstOrNull() ?: GoalType.LEAN_BODY) }
    var selectedActivity by rememberSaveable { mutableStateOf(ActivityLevel.entries.firstOrNull() ?: ActivityLevel.MODERATE) }
    var selectedFood by rememberSaveable { mutableStateOf(FoodPreference.entries.firstOrNull() ?:FoodPreference.EGGETARIAN) }
    var workoutMinutes by rememberSaveable { mutableFloatStateOf(45f) }

    // Provide custom colors to the entire screen
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = FitMateBlack,
            surface = FitMateDarkGrey,
            primary = FitMateEmerald,
            onPrimary = FitMateBlack,
            onBackground = FitMateWhite,
            onSurface = FitMateWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        ) {
            // --- Top Bar & Progress ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentStep > 0) currentStep-- },
                    modifier = Modifier.size(48.dp)
                ) {
                    if (currentStep > 0) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FitMateWhite
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { (currentStep + 1) / totalSteps.toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = FitMateEmerald,
                    trackColor = FitMateDarkGrey,
                )

                Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
            }

            // --- Animated Content for Questions ---
            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(400)) { width -> width } + fadeIn(tween(400)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(400)) { width -> -width } + fadeOut(tween(400)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(400)) { width -> -width } + fadeIn(tween(400)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(400)) { width -> width } + fadeOut(tween(400)))
                    }
                },
                label = "QuestionTransition"
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    when (step) {
                        0 -> GenderSelection(gender) { gender = it }
                        1 -> SliderQuestion("How old are you?", age, 14f..80f, "years") { age = it }
                        2 -> SliderQuestion("What's your height?", height, 120f..220f, "cm") { height = it }
                        3 -> SliderQuestion("What's your weight?", weight, 40f..150f, "kg") { weight = it }
                        4 -> GoalSelection(selectedGoal) { selectedGoal = it }
                        5 -> ActivitySelection(selectedActivity) { selectedActivity = it }
                        6 -> FoodSelection(selectedFood) { selectedFood = it }
                        7 -> SliderQuestion("Daily workout goal?", workoutMinutes, 10f..120f, "min/day") { workoutMinutes = it }
                    }
                }
            }

            // --- Bottom Actions ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            val profile = UserProfile(
                                age = age.toInt(),
                                heightCm = height.toInt(),
                                weightKg = weight.toInt(),
                                gender = gender,
                                activityLevel = selectedActivity,
                                goal = selectedGoal,
                                foodPreference = selectedFood,
                                workoutMinutes = workoutMinutes.toInt(),
                                // Defaults for fields not collected in onboarding
                                budgetInr = 0,
                                experienceLevel = ExperienceLevel.BEGINNER,
                                equipment = emptySet()
                            )
                            onContinue(profile)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FitMateEmerald,
                        contentColor = FitMateBlack
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = if (currentStep == totalSteps - 1) "Generate My AI Plan" else "Continue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- Question Composables ---

@Composable
fun GenderSelection(selectedGender: String, onSelect: (String) -> Unit) {
    QuestionTitle("What is your gender?")
    Spacer(modifier = Modifier.height(32.dp))
    val genders = listOf("Male", "Female", "Other")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        genders.forEach { g ->
            GlassCard(
                text = g,
                isSelected = selectedGender == g,
                onClick = { onSelect(g) }
            )
        }
    }
}

@Composable
fun GoalSelection(selectedGoal: GoalType, onSelect: (GoalType) -> Unit) {
    QuestionTitle("What's your main goal?")
    Spacer(modifier = Modifier.height(32.dp))

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GoalType.entries.forEach { goal ->
            // Assuming goal has a 'name' or 'label' property. Fallback to name.
            val labelText = runCatching { goal::class.java.getMethod("getLabel").invoke(goal) as String }
                .getOrElse { goal.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() } }

            GlassCard(
                text = labelText,
                isSelected = selectedGoal == goal,
                onClick = { onSelect(goal) }
            )
        }
    }
}

@Composable
fun ActivitySelection(selectedActivity: ActivityLevel, onSelect: (ActivityLevel) -> Unit) {
    QuestionTitle("How active are you?")
    Spacer(modifier = Modifier.height(32.dp))

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ActivityLevel.entries.forEach { activity ->
            val labelText = runCatching { activity::class.java.getMethod("getLabel").invoke(activity) as String }
                .getOrElse { activity.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() } }

            GlassCard(
                text = labelText,
                isSelected = selectedActivity == activity,
                onClick = { onSelect(activity) }
            )
        }
    }
}

@Composable
fun FoodSelection(selectedFood: FoodPreference, onSelect: (FoodPreference) -> Unit) {
    QuestionTitle("Any food preferences?")
    Spacer(modifier = Modifier.height(32.dp))

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FoodPreference.entries.forEach { food ->
            val labelText = runCatching { food::class.java.getMethod("getLabel").invoke(food) as String }
                .getOrElse { food.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() } }

            GlassCard(
                text = labelText,
                isSelected = selectedFood == food,
                onClick = { onSelect(food) }
            )
        }
    }
}

@Composable
fun SliderQuestion(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit
) {
    QuestionTitle(title)
    Spacer(modifier = Modifier.height(48.dp))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value.toInt().toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = FitMateWhite
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = unit,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = FitMateWhite.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = FitMateEmerald,
                activeTrackColor = FitMateEmerald,
                inactiveTrackColor = FitMateGlassBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- Reusable UI Components ---

@Composable
fun QuestionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp
        ),
        color = FitMateWhite,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun GlassCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) FitMateEmerald.copy(alpha = 0.15f) else FitMateGlass
    val borderColor = if (isSelected) FitMateEmerald else FitMateGlassBorder
    val textColor = if (isSelected) FitMateEmerald else FitMateWhite

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(vertical = 20.dp, horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}