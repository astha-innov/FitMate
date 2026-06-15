package com.fitmate.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

// ── Premium White Palette ──────────────────────────────────────────────────
private val FMBackground    = Color(0xFFFFFFFF)
private val FMCard          = Color(0xFFF8FAFC)
private val FMCardSelected  = Color(0xFFECFDF5)
private val FMGreen         = Color(0xFF10B981)
private val FMGreenLight    = Color(0xFF34D399)
private val FMTextPrimary   = Color(0xFF111827)
private val FMTextSecondary = Color(0xFF6B7280)
private val FMBorder        = Color(0xFFE5E7EB)
private val FMBorderSelected= Color(0xFF10B981)
// ──────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuestionsScreen(
    onContinue: (UserProfile) -> Unit
) {
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val totalSteps = 8

    var gender by rememberSaveable { mutableStateOf("Male") }
    var age by rememberSaveable { mutableFloatStateOf(25f) }
    var height by rememberSaveable { mutableFloatStateOf(170f) }
    var weight by rememberSaveable { mutableFloatStateOf(65f) }
    var selectedGoal by rememberSaveable { mutableStateOf(GoalType.entries.firstOrNull() ?: GoalType.LEAN_BODY) }
    var selectedActivity by rememberSaveable { mutableStateOf(ActivityLevel.entries.firstOrNull() ?: ActivityLevel.MODERATE) }
    var selectedFood by rememberSaveable { mutableStateOf(FoodPreference.entries.firstOrNull() ?: FoodPreference.EGGETARIAN) }
    var workoutMinutes by rememberSaveable { mutableFloatStateOf(45f) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            background = FMBackground,
            surface = FMCard,
            primary = FMGreen,
            onPrimary = FMBackground,
            onBackground = FMTextPrimary,
            onSurface = FMTextPrimary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FMBackground)
        ) {
            // Subtle decorative background blobs
            DecorativeBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // ── Top Bar ───────────────────────────────────────────────
                TopProgressSection(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    onBack = { if (currentStep > 0) currentStep-- }
                )

                // ── Question Content ──────────────────────────────────────
                AnimatedContent(
                    targetState = currentStep,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) { it } + fadeIn(tween(300)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                    ) { -it } + fadeOut(tween(200))
                                )
                        } else {
                            (slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) { -it } + fadeIn(tween(300)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                    ) { it } + fadeOut(tween(200))
                                )
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

                // ── Continue Button ───────────────────────────────────────
                ContinueButton(
                    isLastStep = currentStep == totalSteps - 1,
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
                                budgetInr = 0,
                                experienceLevel = ExperienceLevel.BEGINNER,
                                equipment = emptySet()
                            )
                            onContinue(profile)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-right soft green blob
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 120.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FMGreen.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )
        // Bottom-left accent blob
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FMGreenLight.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun TopProgressSection(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (currentStep + 1) / totalSteps.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                if (currentStep > 0) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FMCard)
                            .border(1.dp, FMBorder, RoundedCornerShape(14.dp))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FMTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Text(
                text = "Step ${currentStep + 1} of $totalSteps",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = FMTextSecondary,
                letterSpacing = 0.3.sp
            )

            // Placeholder for symmetry
            Spacer(modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(FMBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(FMGreen, FMGreenLight)
                        )
                    )
            )
        }
    }
}

@Composable
private fun ContinueButton(
    isLastStep: Boolean,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "btn_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .scale(scale)
                .shadow(
                    elevation = if (pressed) 2.dp else 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = FMGreen.copy(alpha = 0.25f),
                    spotColor = FMGreen.copy(alpha = 0.35f)
                ),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FMGreen,
                contentColor = FMBackground
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = if (isLastStep) "Generate my fitness plan" else "Continue",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }
    }
}

// ── Question Composables ──────────────────────────────────────────────────

@Composable
fun GenderSelection(selectedGender: String, onSelect: (String) -> Unit) {
    val genders = listOf("Male", "Female", "Other")
    QuestionTitle("What is your\ngender?")
    Spacer(modifier = Modifier.height(32.dp))
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        genders.forEachIndexed { index, g ->
            AnimatedOptionCard(
                text = g,
                isSelected = selectedGender == g,
                onClick = { onSelect(g) },
                animDelay = index * 60
            )
        }
    }
}

@Composable
fun GoalSelection(selectedGoal: GoalType, onSelect: (GoalType) -> Unit) {
    QuestionTitle("What's your\nmain goal?")
    Spacer(modifier = Modifier.height(32.dp))
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GoalType.entries.forEachIndexed { index, goal ->
            val labelText = runCatching {
                goal::class.java.getMethod("getLabel").invoke(goal) as String
            }.getOrElse {
                goal.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            }
            AnimatedOptionCard(
                text = labelText,
                isSelected = selectedGoal == goal,
                onClick = { onSelect(goal) },
                animDelay = index * 60
            )
        }
    }
}

@Composable
fun ActivitySelection(selectedActivity: ActivityLevel, onSelect: (ActivityLevel) -> Unit) {
    QuestionTitle("How active\nare you?")
    Spacer(modifier = Modifier.height(32.dp))
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ActivityLevel.entries.forEachIndexed { index, activity ->
            val labelText = runCatching {
                activity::class.java.getMethod("getLabel").invoke(activity) as String
            }.getOrElse {
                activity.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            }
            AnimatedOptionCard(
                text = labelText,
                isSelected = selectedActivity == activity,
                onClick = { onSelect(activity) },
                animDelay = index * 60
            )
        }
    }
}

@Composable
fun FoodSelection(selectedFood: FoodPreference, onSelect: (FoodPreference) -> Unit) {
    QuestionTitle("Any food\npreferences?")
    Spacer(modifier = Modifier.height(32.dp))
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FoodPreference.entries.forEachIndexed { index, food ->
            val labelText = runCatching {
                food::class.java.getMethod("getLabel").invoke(food) as String
            }.getOrElse {
                food.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            }
            AnimatedOptionCard(
                text = labelText,
                isSelected = selectedFood == food,
                onClick = { onSelect(food) },
                animDelay = index * 60
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
        // Value display card
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(FMCard)
                .border(1.dp, FMBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 40.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value.toInt().toString(),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    color = FMTextPrimary,
                    lineHeight = 72.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = unit,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = FMTextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Range labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = range.start.toInt().toString(),
                fontSize = 12.sp,
                color = FMTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = range.endInclusive.toInt().toString(),
                fontSize = 12.sp,
                color = FMTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SliderStepButton(
                label = "-",
                enabled = value > range.start,
                onClick = {
                    onValueChange((value - 1f).coerceAtLeast(range.start))
                }
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = FMGreen,
                    activeTrackColor = FMGreen,
                    inactiveTrackColor = FMBorder,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
            SliderStepButton(
                label = "+",
                enabled = value < range.endInclusive,
                onClick = {
                    onValueChange((value + 1f).coerceAtMost(range.endInclusive))
                }
            )
        }
    }
}

// ── Reusable UI Components ────────────────────────────────────────────────

@Composable
private fun SliderStepButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, if (enabled) FMGreen else FMBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = FMGreen,
            disabledContentColor = FMTextSecondary.copy(alpha = 0.45f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QuestionTitle(text: String) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(text) {
        visible = false
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 3 }
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                lineHeight = 42.sp,
                letterSpacing = (-0.5).sp
            ),
            color = FMTextPrimary,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun GlassCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    AnimatedOptionCard(
        text = text,
        isSelected = isSelected,
        onClick = onClick,
        animDelay = 0
    )
}

@Composable
fun AnimatedOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    animDelay: Int
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animDelay.toLong())
        appeared = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) FMCardSelected else FMCard,
        animationSpec = tween(200),
        label = "card_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) FMBorderSelected else FMBorder,
        animationSpec = tween(200),
        label = "card_border"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) FMGreen else FMTextPrimary,
        animationSpec = tween(200),
        label = "card_text"
    )

    AnimatedVisibility(
        visible = appeared,
        enter = fadeIn(tween(300)) + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialScale = 0.90f
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(cardScale)
                .shadow(
                    elevation = if (isSelected) 6.dp else 1.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = if (isSelected) FMGreen.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.04f),
                    spotColor = if (isSelected) FMGreen.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.06f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(bgColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable { onClick() }
                .padding(vertical = 20.dp, horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = text,
                    fontSize = 17.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    letterSpacing = 0.1.sp
                )
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(tween(200)) + scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        ),
                        initialScale = 0.5f
                    ),
                    exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.5f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(FMGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = FMBackground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
