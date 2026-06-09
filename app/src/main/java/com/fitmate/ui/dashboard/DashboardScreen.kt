package com.fitmate.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitmate.domain.model.WorkoutFocus
import com.fitmate.ui.viewmodel.CampusFitUiState

private val ProfileBg = Color(0xFF05070A)
private val ProfileCard = Color(0xFF111720)
private val ProfileGlass = Color.White.copy(alpha = 0.06f)
private val ProfileBorder = Color.White.copy(alpha = 0.11f)
private val ProfileCyan = Color(0xFF00E5FF)
private val ProfileGreen = Color(0xFF00E676)
private val ProfileGold = Color(0xFFFFC857)
private val ProfileText = Color(0xFFF8FAFC)
private val ProfileMuted = Color.White.copy(alpha = 0.66f)

@Composable
fun DashboardScreen(state: CampusFitUiState) {
    val profile = state.profile
    val streakDays = state.dashboard?.disciplineState?.streakDays ?: 0
    val workoutDaysPerWeek = remember(state.workoutSchedule) {
        state.workoutSchedule
            ?.days
            ?.count { it.focus != WorkoutFocus.REST }
            ?: 0
    }
    var entered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        entered = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ProfileCyan.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            ProfileInfoCard(
                title = "Physical Stats",
                icon = Icons.Outlined.Person,
                accent = ProfileCyan,
                items = listOf(
                    ProfileInfoItem("Age", "${profile.age} yrs"),
                    ProfileInfoItem("Height", "${profile.heightCm} cm"),
                    ProfileInfoItem("Weight", "${profile.weightKg} kg"),
                )
            )

            ProfileInfoCard(
                title = "Fitness Goals",
                icon = Icons.Outlined.MonitorWeight,
                accent = ProfileGreen,
                items = listOf(
                    ProfileInfoItem("Workout Goal", profile.goal.label),
                    ProfileInfoItem("Fitness Level", profile.experienceLevel.label),
                    ProfileInfoItem("Activity", profile.activityLevel.label),
                )
            )

            ProfileInfoCard(
                title = "Training Commitment",
                icon = Icons.Outlined.Schedule,
                accent = ProfileGold,
                items = listOf(
                    ProfileInfoItem("Dedicated Time", "${profile.workoutMinutes} min"),
                    ProfileInfoItem("Preferred Duration", "${profile.workoutMinutes} min/session"),
                    ProfileInfoItem(
                        "Workout Days",
                        if (workoutDaysPerWeek > 0) "$workoutDaysPerWeek days/week" else "Plan not set"
                    ),
                )
            )

            InsightCard(
                title = "Diet",
                icon = "🥗",
                accent = ProfileGreen
            ) {
                InsightRow("Goal", profile.goal.label, ProfileGreen)
                InsightRow("Food Preference", profile.foodPreference.label, ProfileGreen)
                state.personalizedPlan?.dietRecommendation?.title?.let {
                    InsightText(it, ProfileMuted)
                }
                state.personalizedPlan?.dietRecommendation?.meals?.take(3)?.forEach { meal ->
                    InsightBullet(meal, ProfileGreen)
                }
            }

            InsightCard(
                title = "Habits",
                icon = "⚡",
                accent = ProfileGold
            ) {
                InsightBullet("Drink enough water", ProfileGold)
                InsightBullet("Complete workout", ProfileGold)
                InsightBullet("Hit protein goal", ProfileGold)
                state.dashboard?.disciplineState?.let { discipline ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MiniInsightChip("STREAK", "${discipline.streakDays}d", ProfileGold, Modifier.weight(1f))
                        MiniInsightChip("POINTS", "${discipline.rewardPoints}", ProfileCyan, Modifier.weight(1f))
                    }
                }
            }

            MotivationalCard()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProfileHero(
    streakDays: Int,
    goalLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileGlass),
        border = BorderStroke(1.dp, ProfileBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(ProfileCyan.copy(alpha = 0.34f), ProfileGreen.copy(alpha = 0.16f))
                                )
                            )
                            .border(1.dp, ProfileCyan.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FitnessCenter,
                            contentDescription = null,
                            tint = ProfileText,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Hello Champion 🏆",
                            color = ProfileText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Consistency beats intensity. Keep going.",
                            color = ProfileMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = ProfileCyan.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ProfileCyan.copy(alpha = 0.28f))
                ) {
                    Text(
                        text = goalLabel,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = ProfileCyan,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            StreakChip(streakDays = streakDays)
        }
    }
}

@Composable
private fun StreakChip(streakDays: Int) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.28f),
        border = BorderStroke(1.dp, ProfileGold.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalFireDepartment,
                contentDescription = "Streak",
                tint = ProfileGold,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "$streakDays",
                color = ProfileText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ProfileInfoCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    items: List<ProfileInfoItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileCard),
        border = BorderStroke(1.dp, ProfileBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = title,
                    color = ProfileText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.forEach { item ->
                    ProfileMetricTile(
                        item = item,
                        accent = accent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMetricTile(
    item: ProfileInfoItem,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.07f))
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = item.value,
            color = ProfileText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = item.label,
            color = ProfileMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MotivationalCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, ProfileGreen.copy(alpha = 0.24f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            ProfileGreen.copy(alpha = 0.12f),
                            ProfileCyan.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Text(
                text = "Your profile is your training compass. Keep the plan honest, repeat the basics, and let the streak grow quietly.",
                color = ProfileText.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    icon: String,
    accent: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileCard),
        border = BorderStroke(1.dp, ProfileBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon)
                }
                Text(
                    text = title,
                    color = ProfileText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.06f))
            .border(1.dp, accent.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = accent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = ProfileText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InsightText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun InsightBullet(
    text: String,
    accent: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(
            text = text,
            color = ProfileMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MiniInsightChip(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            color = ProfileText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = accent,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class ProfileInfoItem(
    val label: String,
    val value: String,
)
