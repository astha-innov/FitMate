package com.fitmate.ui.workout

import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.fitmate.ui.viewmodel.CampusFitUiState
import kotlinx.coroutines.delay

// --- Custom Color Palette ---
val NeonCyan = Color(0xFF00E5FF)
val DeepSpace = Color(0xFF05070A)
val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.05f)
val SurfaceBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFFFFFFF).copy(alpha = 0.7f)

// Helper to load exact asset names
private fun String.toAssetPath(): String = "file:///android_asset/exercises/$this"

@OptIn(ExperimentalFoundationApi::class)
@Suppress("UNUSED_PARAMETER") // Silences the warning until you connect the ViewModel
@Composable
fun WorkoutScreen(
    state: CampusFitUiState? = null
) {
    val context = LocalContext.current

    // Configures Coil to support GIF decoding
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val weeklyData = remember { generateWeeklyData() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace), // Forces background to DeepSpace
        containerColor = DeepSpace    // Forces Scaffold container to DeepSpace
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepSpace) // Extra safeguard for LazyColumn background
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // 1. Auto-Scrolling Hero GIF Carousel
            item {
                HeroGifCarousel(imageLoader = imageLoader)
            }

            // 2. Stats and Badges
            item {
                WeeklyStatsAndBadges()
            }

            // 3. 5-Day Split
            items(weeklyData) { dayPlan ->
                WorkoutDayCard(dayPlan, imageLoader)
            }

            // 4. Motivational Banner
            item {
                MotivationBanner(imageLoader)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroGifCarousel(imageLoader: ImageLoader) {
    val heroGifs = remember {
        listOf(
            "gym_buddy.gif",
            "doggo.gif",
            "chest_press.gif",
            "bench_press.gif",
            "lifting_weights.gif"
        )
    }

    // Set a large initial page to allow "infinite" scrolling behavior
    // 5000 is evenly divisible by 5, so it maps cleanly to index 0
    val pageCount = 10000
    val pagerState = rememberPagerState(
        initialPage = 5000,
        pageCount = { pageCount }
    )

    // Auto-scroll loop
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3500)
            pagerState.animateScrollToPage(
                page = pagerState.currentPage + 1,
                animationSpec = tween(durationMillis = 800) // Smooth slide transition
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(280.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSpace), // Blend empty space
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false // No user interaction required
            ) { page ->
                val actualIndex = page % heroGifs.size

                AsyncImage(
                    model = heroGifs[actualIndex].toAssetPath(),
                    imageLoader = imageLoader,
                    contentDescription = "Workout Carousel",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillWidth // Ensures full GIF fits edge-to-edge horizontally
                )
            }

            // Dark gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DeepSpace.copy(alpha = 0.4f),
                                DeepSpace.copy(alpha = 0.95f)
                            ),
                            startY = 200f // Push gradient lower
                        )
                    )
            )

            // Text Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Weekly Training",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stay consistent. Stay strong.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun WeeklyStatsAndBadges() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Weekly Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(modifier = Modifier.weight(1f), title = "Exercises", value = "24")
            StatCard(modifier = Modifier.weight(1f), title = "Total Sets", value = "72")
            StatCard(modifier = Modifier.weight(1f), title = "Cals Burned", value = "1240")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Achievements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item { BadgeChip("🔥 Consistent") }
            item { BadgeChip("💪 Strength Builder") }
            item { BadgeChip("🏆 Weekly Warrior") }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        border = BorderStroke(1.dp, SurfaceBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun BadgeChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GlassWhite,
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WorkoutDayCard(dayPlan: WorkoutDay, imageLoader: ImageLoader) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        border = BorderStroke(1.dp, SurfaceBorder),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)) {
            // Day Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = dayPlan.dayTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dayPlan.splitName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
                if (dayPlan.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Pending",
                        tint = TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            // Exercises List
            dayPlan.exercises.forEachIndexed { index, exercise ->
                ExerciseRow(exercise, imageLoader)
                if (index < dayPlan.exercises.size - 1) {
                    HorizontalDivider(
                        color = SurfaceBorder,
                        modifier = Modifier.padding(horizontal = 72.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseRow(exercise: Exercise, imageLoader: ImageLoader) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(20.dp)
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = exercise.assetName.toAssetPath(),
                imageLoader = imageLoader,
                contentDescription = exercise.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepSpace)
                    .padding(1.dp)
                    .clip(RoundedCornerShape(15.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${exercise.sets} Sets × ${exercise.reps}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand details",
                tint = NeonCyan
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(20.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExerciseDetailChip(Icons.Default.AdsClick, exercise.targetMuscle)
                ExerciseDetailChip(Icons.Default.LocalFireDepartment, "${exercise.calories} kcal")
                ExerciseDetailChip(Icons.Default.Speed, exercise.difficulty)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Form Tips",
                style = MaterialTheme.typography.labelLarge,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = exercise.formTips,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Common Mistakes",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFFF5252), // Softer red for dark mode
                fontWeight = FontWeight.Bold
            )
            Text(
                text = exercise.commonMistakes,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ExerciseDetailChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = GlassWhite,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = NeonCyan
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun MotivationBanner(imageLoader: ImageLoader) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "gym_motivation.gif".toAssetPath(),
                imageLoader = imageLoader,
                contentDescription = "Motivation",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f // Dimmer for dark mode readability
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DeepSpace.copy(alpha = 0.9f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = "DISCIPLINE BEATS MOTIVATION",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stay Consistent. Trust The Process.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

// --- Data Models and Data Factory ---

data class WorkoutDay(
    val dayTitle: String,
    val splitName: String,
    val isCompleted: Boolean,
    val exercises: List<Exercise>
)

data class Exercise(
    val name: String,
    val assetName: String,
    val sets: Int,
    val reps: String,
    val targetMuscle: String,
    val calories: Int,
    val difficulty: String,
    val formTips: String = "Maintain proper form and control the eccentric movement.",
    val commonMistakes: String = "Using momentum instead of muscle contraction."
)

private fun generateWeeklyData(): List<WorkoutDay> {
    return listOf(
        WorkoutDay(
            dayTitle = "DAY 1",
            splitName = "CHEST + BICEPS",
            isCompleted = true,
            exercises = listOf(
                Exercise("Pushups", "pushup.gif", 4, "Failure", "Chest/Triceps", 60, "Beginner"),
                Exercise("Cable Chest Press", "cable_chest_press.jpg", 3, "10-12", "Chest", 75, "Intermediate"),
                Exercise("Butterfly", "butterfly.jpg", 3, "12-15", "Inner Chest", 50, "Beginner"),
                Exercise("Incline Inner Biceps Curls", "incline_inner_biceps_curls.jpg", 4, "10-12", "Biceps", 45, "Advanced")
            )
        ),
        WorkoutDay(
            dayTitle = "DAY 2",
            splitName = "BACK + REAR DELTS",
            isCompleted = true,
            exercises = listOf(
                Exercise("Barbell Rear Delt Row", "barbell_rearDelt_row.jpg", 4, "10", "Rear Deltoids", 80, "Intermediate"),
                Exercise("Elevated Cable Rows", "elevated_cable_rows.jpg", 3, "12", "Lats/Mid Back", 70, "Intermediate"),
                Exercise("Deadlift with Bands", "deadlift_with_bands.jpg", 4, "5-8", "Lower Back/Hamstrings", 120, "Advanced"),
                Exercise("Deadlift with Chains", "deadlift_with_chains.jpg", 3, "3-5", "Full Posterior", 150, "Expert"),
                Exercise("Dynamic Back Stretch", "dynamic_back_stretch.jpg", 2, "60 sec", "Mobility", 15, "Beginner")
            )
        ),
        WorkoutDay(
            dayTitle = "DAY 3",
            splitName = "LEGS",
            isCompleted = true,
            exercises = listOf(
                Exercise("Hack Squat", "hack_squat.jpg", 4, "10-12", "Quadriceps", 100, "Intermediate"),
                Exercise("Barbell Lunge", "barbell_lunge.jpg", 3, "12 per leg", "Glutes/Quads", 90, "Advanced"),
                Exercise("Elevated Back Lunge", "elevated_back_lunge.jpg", 3, "10 per leg", "Glutes", 85, "Intermediate"),
                Exercise("Cable Hip Adduction", "cable_hip_aduction.jpg", 3, "15", "Inner Thighs", 40, "Beginner")
            )
        ),
        WorkoutDay(
            dayTitle = "DAY 4",
            splitName = "SHOULDERS + TRICEPS",
            isCompleted = false,
            exercises = listOf(
                Exercise("Shoulder Raise", "shoulder_raise.jpg", 4, "15", "Lateral Deltoids", 55, "Beginner"),
                Exercise("Body Tricep Press", "body_tricep_press.jpg", 3, "10-12", "Triceps", 60, "Intermediate"),
                Exercise("Tricep Extension", "tricep_extension.jpg", 3, "12-15", "Long Head Triceps", 50, "Beginner"),
                Exercise("Bench Dips", "bench_dips.jpg", 3, "Failure", "Triceps/Chest", 45, "Beginner")
            )
        ),
        WorkoutDay(
            dayTitle = "DAY 5",
            splitName = "CORE + CONDITIONING",
            isCompleted = false,
            exercises = listOf(
                Exercise("Cable Crunch", "cable_crunch.jpg", 4, "15-20", "Abs", 50, "Intermediate"),
                Exercise("Decline Reverse Crunch", "decline_reverse_crunch.jpg", 3, "15", "Lower Abs", 60, "Advanced"),
                Exercise("Bent Knee Hip Raise", "bent_knee_hip_raise.jpg", 3, "20", "Core", 40, "Beginner"),
                Exercise("Battling Ropes", "battling_ropes.jpg", 5, "30 sec", "Full Body Conditioning", 120, "Expert"),
                Exercise("Bottoms Up", "bottoms_up.jpg", 3, "12", "Core Stability", 55, "Advanced")
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun WorkoutScreenPreview() {
    MaterialTheme {
        WorkoutScreen(null)
    }
}