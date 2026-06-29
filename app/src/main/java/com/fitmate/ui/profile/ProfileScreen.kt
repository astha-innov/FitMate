package com.fitmate.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitmate.R
import com.fitmate.ScanFoodActivity
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.ui.viewmodel.CampusFitUiState
import androidx.compose.ui.geometry.Offset

// ── Asset Loading Helper ───────────────────────────────────────────────────────
@Composable
private fun rememberAssetBitmap(path: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(path) {
        try {
            context.assets.open(path).use { inputStream ->
                BitmapFactory.decodeStream(inputStream).asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }
}

// ── Drawable Bitmap Helper ─────────────────────────────────────────────────────
@Composable
private fun rememberDrawableBitmap(resId: Int): ImageBitmap? {
    val context = LocalContext.current
    return remember(resId) {
        try {
            val options = android.graphics.BitmapFactory.Options().apply {
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeResource(context.resources, resId, options)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

// ── Core Palette ───────────────────────────────────────────────────────────────
private val FitGreen      = Color(0xFF16C47F)
private val FitGreenLight = Color(0xFFE8FBF3)
private val FitGreenDim   = Color(0xFF0FA363)
private val CanvasWhite   = Color(0xFFF7F9FC)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextDark      = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val TextHint      = Color(0xFF9CA3AF)
private val DividerColor  = Color(0xFFF0F2F5)

// ── Scan card palette (dark theme) ────────────────────────────────────────────
private val ScanCardBg        = Color(0xFF111827)
private val ScanCardBgDeep    = Color(0xFF0A0F1A)
private val ScanCardBorder    = Color(0xFF1E2A3A)
private val ScanGreenGlow     = Color(0x3316C47F)
private val ScanIconBg        = Color(0xFF172236)
private val ScanIconBorder    = Color(0xFF1E3A5F)
private val ScanButtonBg      = Color(0xFF16C47F)
private val ScanButtonText    = Color(0xFFFFFFFF)

// ── ProfileScreen ──────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(state: CampusFitUiState) {
    val profile = state.profile
    val context = LocalContext.current
    val streakDays = state.analytics.currentStreak
    val points = 0
    val workoutDaysPerWeek = remember(state.workoutSchedule) {
        state.workoutSchedule
            ?.days
            ?.count { it.focus != WorkoutFocus.REST }
            ?: 0
    }
    var entered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { entered = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 28.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. HERO SECTION
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn() + slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) { -it / 4 }
            ) {
                ProfileHero(
                    streakDays = streakDays,
                    goalLabel = profile.goal.label
                )
            }

            // ── SCAN FOOD CARD moved immediately below hero ────────────────────
            ScanPackagedFoodCard(
                onClick = {
                    context.startActivity(
                        Intent(context, ScanFoodActivity::class.java)
                    )
                }
            )

            // Compact Base Stats
            FriendlyStatRow(
                items = listOf(
                    ProfileInfoItem(stringResource(R.string.profile_label_age), "${profile.age} ${stringResource(R.string.profile_unit_yrs)}"),
                    ProfileInfoItem(stringResource(R.string.profile_label_height), "${profile.heightCm} ${stringResource(R.string.unit_cm)}"),
                    ProfileInfoItem(stringResource(R.string.profile_label_weight), "${profile.weightKg} ${stringResource(R.string.unit_kg)}"),
                )
            )

            // 2. JOURNEY SECTION
            FriendlyInfoCard(
                title = stringResource(R.string.profile_journey_title),
                subtitle = stringResource(R.string.profile_journey_subtitle),
                icon = Icons.Outlined.MonitorWeight,
                headerImage = "profile/body_transformation.png",
                imageScale = ContentScale.Fit,
                imageHeight = 465,
                items = listOf(
                    ProfileInfoItem(stringResource(R.string.profile_label_goal), profile.goal.label),
                    ProfileInfoItem(stringResource(R.string.profile_label_level), profile.experienceLevel.label),
                    ProfileInfoItem(stringResource(R.string.profile_label_activity), profile.activityLevel.label),
                )
            )

            // 3. ACHIEVEMENT CENTER
            AchievementCard(streakDays = streakDays, points = points)

            // 4. TRAINING COMMITMENT
            FriendlyInfoCard(
                title = stringResource(R.string.profile_commitment_title),
                subtitle = stringResource(R.string.profile_commitment_subtitle),
                icon = Icons.Outlined.Schedule,
                items = listOf(
                    ProfileInfoItem(stringResource(R.string.profile_label_session), "${profile.workoutMinutes} ${stringResource(R.string.profile_unit_min)}"),
                    ProfileInfoItem(stringResource(R.string.profile_label_duration), "${profile.workoutMinutes} ${stringResource(R.string.profile_unit_min)}"),
                    ProfileInfoItem(
                        stringResource(R.string.profile_label_days_week),
                        if (workoutDaysPerWeek > 0) "$workoutDaysPerWeek ${stringResource(R.string.profile_unit_days)}" else stringResource(R.string.profile_not_set)
                    ),
                )
            )

            NutritionProfileCard(state)

            DailyHabitsCard()

            // Motivation Footer
            MotivationalCard()
        }
    }
}

@Composable
fun NutritionProfileCard(state: CampusFitUiState) {
    val profile = state.profile
    FriendlyInsightCard(
        title = stringResource(R.string.profile_nutrition_title),
        subtitle = stringResource(R.string.profile_nutrition_subtitle),
        emoji = stringResource(R.string.profile_emoji_nutrition),
        headerImage = "profile/healthy_diet.png",
        footerImage = "profile/meals.png"
    ) {
        LabelValueRow(stringResource(R.string.profile_diet_goal), profile.goal.label)
        LabelValueRow(stringResource(R.string.profile_preference), profile.foodPreference.label)

        state.personalizedPlan?.dietRecommendation?.title?.let {
            Text(
                text = it,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }
        state.personalizedPlan?.dietRecommendation?.meals?.take(3)?.forEach { meal ->
            FriendlyBulletRow(meal)
        }
    }
}

@Composable
fun DailyHabitsCard() {
    FriendlyInsightCard(
        title = stringResource(R.string.profile_habits_title),
        subtitle = stringResource(R.string.profile_habits_subtitle),
        emoji = stringResource(R.string.profile_emoji_habits)
    ) {
        FriendlyBulletRow(stringResource(R.string.profile_habit_water))
        FriendlyBulletRow(stringResource(R.string.profile_habit_workout))
        FriendlyBulletRow(stringResource(R.string.profile_habit_protein))
    }
}

// ── Hero ───────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileHero(streakDays: Int, goalLabel: String) {
    val heroImg = rememberAssetBitmap("profile/avatar2.jpg")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            if (heroImg != null) {
                Image(
                    bitmap = heroImg,
                    contentDescription = stringResource(R.string.profile_hero_bg_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FitGreenDim)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_greeting),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = stringResource(R.string.profile_motto),
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    StreakChip(streakDays = streakDays)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(FitGreen)
                    )
                    Text(
                        text = stringResource(R.string.profile_current_goal_prefix, goalLabel.uppercase()),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

// ── Streak chip ────────────────────────────────────────────────────────────────
@Composable
private fun StreakChip(streakDays: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardWhite.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalFireDepartment,
                contentDescription = stringResource(R.string.profile_streak_desc),
                tint = FitGreenDim,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "$streakDays",
                color = TextDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// ── Stat Row (3 tiles) ─────────────────────────────────────────────────────────
@Composable
private fun FriendlyStatRow(items: List<ProfileInfoItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.value,
                        color = TextDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = item.label.uppercase(),
                        color = TextHint,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        textAlign = TextAlign.Center
                    )
                }

                if (index < items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(DividerColor)
                    )
                }
            }
        }
    }
}

// ── Generic info card with optional image banner ───────────────────────────────
@Composable
private fun FriendlyInfoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    headerImage: String? = null,
    imageScale: ContentScale = ContentScale.Crop,
    imageHeight: Int = 180,
    items: List<ProfileInfoItem>
) {
    val imgBitmap = headerImage?.let { rememberAssetBitmap(it) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (imgBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight.dp)
                        .background(Color.White)
                ) {
                    Image(
                        bitmap = imgBitmap,
                        contentDescription = null,
                        contentScale = imageScale,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FitGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = FitGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = title,
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = subtitle,
                            color = TextHint,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                HorizontalDivider(color = DividerColor, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items.forEach { item ->
                        FriendlyMetricTile(
                            item = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendlyMetricTile(
    item: ProfileInfoItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CanvasWhite)
            .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = item.value,
            color = TextDark,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = item.label.uppercase(),
            color = TextHint,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

// ── Insight card (Diet / Habits) ───────────────────────────────────────────────
@Composable
private fun FriendlyInsightCard(
    title: String,
    subtitle: String,
    emoji: String,
    headerImage: String? = null,
    footerImage: String? = null,
    content: @Composable () -> Unit
) {
    val hImg = headerImage?.let { rememberAssetBitmap(it) }
    val fImg = footerImage?.let { rememberAssetBitmap(it) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (hImg != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.White)
                ) {
                    Image(
                        bitmap = hImg,
                        contentDescription = stringResource(R.string.profile_banner_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FitGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 20.sp
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = title,
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = subtitle,
                            color = TextHint,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                HorizontalDivider(
                    color = DividerColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    content()
                }

                if (fImg != null) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                    ) {
                        Image(
                            bitmap = fImg,
                            contentDescription = stringResource(R.string.profile_recommendation_desc),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

// ── Achievement Card ───────────────────────────────────────────────────────────
@Composable
private fun AchievementCard(streakDays: Int, points: Int) {
    val trophyImg = rememberAssetBitmap("profile/trophy2.png")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.profile_achievement_center),
                color = TextDark,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 12.dp
                )
            )

            if (trophyImg != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.White)
                ) {
                    Image(
                        bitmap = trophyImg,
                        contentDescription = stringResource(R.string.profile_achievements_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AchievementChip(
                        label = stringResource(R.string.profile_streak_label),
                        value = "${streakDays}${stringResource(R.string.profile_unit_day_short)}",
                        modifier = Modifier.weight(1f)
                    )
                    AchievementChip(
                        label = stringResource(R.string.profile_points_label),
                        value = "$points",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ── Label–Value row ───────────────────────────────────────────────────────────
@Composable
private fun LabelValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CanvasWhite)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = TextDark,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Bullet row ─────────────────────────────────────────────────────────────────
@Composable
private fun FriendlyBulletRow(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CanvasWhite)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(FitGreen)
        )
        Text(
            text = text,
            color = TextDark,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Achievement chips ─────────────────────────────────────────────────────────
@Composable
private fun AchievementChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FitGreenLight)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            color = TextDark,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp
        )
        Text(
            text = label,
            color = FitGreenDim,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

// ── Motivational card ─────────────────────────────────────────────────────────
@Composable
private fun MotivationalCard() {
    val modelImg = rememberAssetBitmap("drawable/model.png")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FitGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_keep_going),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = stringResource(R.string.profile_quote),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }

            if (modelImg != null) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = modelImg,
                        contentDescription = stringResource(R.string.profile_decorative_model_desc),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

// ── Premium Scan Packaged Food Card ────────────────────────────────────────────
// Full card is clickable. Right side shows amul_milk.jpg tilted at -12°,
// slightly overlapping the scanner frame brackets. Single onClick handler
// used for both the card and the Scan Now button.
@Composable
private fun ScanPackagedFoodCard(onClick: () -> Unit) {

    // Load the amul milk product image from res/drawable
    val milkImg = rememberDrawableBitmap(R.drawable.amul_milk)

    // The entire card is wrapped in a Box with clickable so every pixel
    // of the card surface (background, image, text, button area) triggers
    // the same navigation. The Card's own onClick is also set to the same
    // lambda for ripple feedback.
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null, // Card provides its own ripple
                onClick           = onClick
            ),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = ScanCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border    = BorderStroke(1.dp, ScanCardBorder),
        onClick   = onClick
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // ── Background glow (top-left radial) ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                FitGreen.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            radius = 400f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 20.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ── Left: Barcode scanner icon panel ───────────────────────────
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(ScanIconBg)
                        .border(
                            1.dp,
                            FitGreen.copy(alpha = 0.35f),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner glow ring
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        FitGreen.copy(alpha = 0.18f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    // Barcode lines drawn with Canvas
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(38.dp)) {
                        val w     = size.width
                        val h     = size.height
                        val color = FitGreen

                        // Corner bracket TL
                        drawLine(color, Offset(0f, h * 0.22f), Offset(0f, 0f),           strokeWidth = 3.5f)
                        drawLine(color, Offset(0f, 0f),         Offset(w * 0.22f, 0f),    strokeWidth = 3.5f)
                        // Corner bracket TR
                        drawLine(color, Offset(w, h * 0.22f), Offset(w, 0f),              strokeWidth = 3.5f)
                        drawLine(color, Offset(w, 0f),         Offset(w * 0.78f, 0f),     strokeWidth = 3.5f)
                        // Corner bracket BL
                        drawLine(color, Offset(0f, h * 0.78f), Offset(0f, h),             strokeWidth = 3.5f)
                        drawLine(color, Offset(0f, h),         Offset(w * 0.22f, h),      strokeWidth = 3.5f)
                        // Corner bracket BR
                        drawLine(color, Offset(w, h * 0.78f), Offset(w, h),               strokeWidth = 3.5f)
                        drawLine(color, Offset(w, h),         Offset(w * 0.78f, h),        strokeWidth = 3.5f)

                        // Barcode vertical lines (inner section)
                        val innerLeft  = w * 0.25f
                        val innerRight = w * 0.75f
                        val lineTop    = h * 0.2f
                        val lineBot    = h * 0.8f
                        val barWidths  = listOf(2.5f, 1.5f, 3.5f, 1.5f, 2.5f, 1.5f, 3.5f)
                        var x          = innerLeft
                        val gap        = (innerRight - innerLeft) / (barWidths.sum() + barWidths.size * 0.5f)
                        barWidths.forEach { bw ->
                            drawLine(
                                color       = FitGreen.copy(alpha = 0.85f),
                                start       = Offset(x, lineTop),
                                end         = Offset(x, lineBot),
                                strokeWidth = bw
                            )
                            x += bw + gap * 0.5f
                        }

                        // Horizontal scan line (center)
                        drawLine(
                            color       = FitGreen,
                            start       = Offset(innerLeft - 4f, h * 0.5f),
                            end         = Offset(innerRight + 4f, h * 0.5f),
                            strokeWidth = 2f
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // ── Center: Text block ─────────────────────────────────────────
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text       = "Scan Food (Barcode)",
                        color      = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 17.sp,
                        lineHeight = 22.sp
                    )
                    Text(
                        text      = "Scan any packaged food and instantly get calories, protein, carbs, fats & more.",
                        color     = Color.White.copy(alpha = 0.6f),
                        fontSize  = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    // Scan Now button — reuses the same onClick as the card
                    Button(
                        onClick          = onClick,
                        modifier         = Modifier.height(38.dp),
                        colors           = ButtonDefaults.buttonColors(
                            containerColor = FitGreen
                        ),
                        shape            = RoundedCornerShape(10.dp),
                        contentPadding   = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp,
                            vertical   = 0.dp
                        )
                    ) {
                        Text(
                            text       = "Scan Now",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 13.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text     = "›",
                            color    = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // ── Right: Scanner frame brackets + tilted amul_milk image ─────
                // The outer Box clips nothing; the image is allowed to slightly
                // overflow the frame area so it looks like it's "popping out".
                Box(
                    modifier = Modifier.size(width = 90.dp, height = 110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Cyan scanner frame brackets drawn behind the milk image
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val w      = size.width
                        val h      = size.height
                        val cyan   = Color(0xFF22D3EE)
                        val arm    = 16.dp.toPx()
                        val stroke = 2.5f

                        // TL bracket
                        drawLine(cyan, Offset(0f, arm),     Offset(0f, 0f),      stroke)
                        drawLine(cyan, Offset(0f, 0f),      Offset(arm, 0f),     stroke)
                        // TR bracket
                        drawLine(cyan, Offset(w, arm),      Offset(w, 0f),       stroke)
                        drawLine(cyan, Offset(w, 0f),       Offset(w - arm, 0f), stroke)
                        // BL bracket
                        drawLine(cyan, Offset(0f, h - arm), Offset(0f, h),       stroke)
                        drawLine(cyan, Offset(0f, h),       Offset(arm, h),      stroke)
                        // BR bracket
                        drawLine(cyan, Offset(w, h - arm),  Offset(w, h),        stroke)
                        drawLine(cyan, Offset(w, h),        Offset(w - arm, h),  stroke)

                        // Horizontal scan line
                        drawLine(
                            color       = FitGreen,
                            start       = Offset(4f, h * 0.52f),
                            end         = Offset(w - 4f, h * 0.52f),
                            strokeWidth = 1.8f
                        )
                    }

                    // amul_milk.jpg — tilted -12°, slightly larger than frame,
                    // with a soft shadow so it reads as a physical product.
                    if (milkImg != null) {
                        Image(
                            bitmap             = milkImg,
                            contentDescription = "Packaged food example",
                            contentScale       = ContentScale.Fit,
                            modifier           = Modifier
                                .size(84.dp)           // Slightly larger than the 72dp frame
                                .offset(x = 4.dp, y = (-4).dp) // Shift slightly so it overlaps frame
                                .shadow(
                                    elevation    = 8.dp,
                                    shape        = RoundedCornerShape(10.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.4f),
                                    spotColor    = Color.Black.copy(alpha = 0.5f)
                                )
                                .rotate(-12f)          // Tilt between -10° and -15° as requested
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        // Fallback placeholder when drawable is unavailable
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .rotate(-12f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ScanIconBg)
                                .border(1.dp, FitGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        )
                    }
                }
            }
        }
    }
}

// ── Model ─────────────────────────────────────────────────────────────────────
private data class ProfileInfoItem(
    val label: String,
    val value: String,
)