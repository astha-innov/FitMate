package com.fitmate.ui.dashboard

import androidx.compose.foundation.background
import com.fitmate.ui.components.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitmate.ui.viewmodel.CampusFitUiState



@Composable
fun DashboardScreen(state: CampusFitUiState) {
    val dashboard = state.dashboard ?: return

    var showStreakDialog by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { showStreakDialog = true },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                Icons.Outlined.LocalFireDepartment,
                contentDescription = "Streak",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            "${dashboard.disciplineState.rewardPoints} reward points",
            style = MaterialTheme.typography.titleMedium
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                "Smart Body Goal System",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                dashboard.reasoning.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GoalProgressLine(
                "Calories",
                dashboard.progress.caloriesConsumed,
                dashboard.metrics.caloriesTarget,
                dashboard.progress.caloriesRatio(
                    dashboard.metrics.caloriesTarget
                )
            )

            GoalProgressLine(
                "Protein",
                dashboard.progress.proteinConsumed,
                dashboard.metrics.proteinTarget,
                dashboard.progress.proteinRatio(
                    dashboard.metrics.proteinTarget
                )
            )

            GoalProgressLine(
                "Water",
                dashboard.progress.waterLitersConsumed,
                dashboard.metrics.waterLitersTarget,
                dashboard.progress.waterRatio(
                    dashboard.metrics.waterLitersTarget
                ),
                "L"
            )

            Text("Body mode: ${dashboard.metrics.calorieMode}")

            Text(
                "BMI: ${"%.1f".format(dashboard.metrics.bmi)}"
            )
        }
    }

    SectionCard("AI reasoning") {

        Text(dashboard.reasoning.calorieReasoning)

        Text(dashboard.reasoning.proteinReasoning)

        Text(dashboard.reasoning.waterReasoning)

        HorizontalDivider()

        dashboard.reasoning.coachingNotes.forEach {
            Text("- $it")
        }
    }

    if (showStreakDialog) {

        AlertDialog(
            onDismissRequest = {
                showStreakDialog = false
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showStreakDialog = false
                    }
                ) {
                    Text("Close")
                }
            },

            title = {
                Text("Streak roadmap")
            },

            text = {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "${dashboard.disciplineState.streakDays} day streak"
                    )

                    Text(
                        dashboard.disciplineState.encouragement,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    dashboard.milestoneMap.forEach { milestone ->

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {

                                Text(
                                    "${milestone.days} days - ${milestone.title}",
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    milestone.reward,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}