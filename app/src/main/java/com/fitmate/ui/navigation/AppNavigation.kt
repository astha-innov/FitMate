package com.fitmate.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitmate.ui.settings.SettingsScreen
import com.fitmate.ui.profile.ProfileScreen
import com.fitmate.ui.progress.ProgressScreen
import com.fitmate.ui.viewmodel.CampusFitUiState
import com.fitmate.ui.viewmodel.CampusFitViewModel
import com.fitmate.ui.workout.WorkoutScreen
import com.fitmate.ui.coach.CoachChatScreen
import androidx.compose.material.icons.outlined.SmartToy
import com.fitmate.ui.coach.CoachIntroScreen
import androidx.compose.material.icons.outlined.Chat
import com.fitmate.ui.components.FitMateLogoMark
import com.fitmate.ui.diet.DietScreen
import androidx.compose.material.icons.outlined.RestaurantMenu

private val NeonCyan = Color(0xFF00E5FF)
private val DeepSpace = Color(0xFF05070A)
private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.05f)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFFFFFFF).copy(alpha = 0.7f)

enum class HomeTab(
    val label: String,
    val icon: ImageVector
) {
    PROFILE(
        "Profile",
        Icons.Outlined.Person
    ),
    WORKOUT(
        "Workout",
        Icons.AutoMirrored.Outlined.DirectionsRun
    ),
    DIET(
        "Diet",
        Icons.Outlined.RestaurantMenu
    ),
    PROGRESS(
        "Progress",
        Icons.Outlined.QueryStats
    ),
    COACH(
        "Coach",
        Icons.Outlined.SmartToy
    ),

    SETTINGS(
        "Settings",
        Icons.Outlined.Menu
    )
}

private val bottomTabs = listOf(
    HomeTab.PROFILE,
    HomeTab.WORKOUT,
    HomeTab.DIET,
    HomeTab.PROGRESS,
    HomeTab.COACH,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    state: CampusFitUiState,
    viewModel: CampusFitViewModel
) {
    var selectedTab by rememberSaveable {
        mutableStateOf(HomeTab.PROFILE)
    }
    var showCoachChat by rememberSaveable {
        mutableStateOf(false)
    }

    val currentTab = remember(selectedTab) {
        selectedTab
    }

    Scaffold(
        containerColor = DeepSpace,

        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepSpace,
                    titleContentColor = TextPrimary
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentTab == HomeTab.PROFILE) {
                            FitMateLogoMark(modifier = Modifier.size(34.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column {
                            Text(
                                text = when (currentTab) {
                                    HomeTab.PROFILE -> "FitMate"
                                    HomeTab.WORKOUT -> "Workout"
                                    HomeTab.DIET -> "Diet"
                                    HomeTab.PROGRESS -> "Progress"
                                    HomeTab.COACH -> "Coach"
                                    HomeTab.SETTINGS -> "Settings"
                                },
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            val subtitle = when (currentTab) {
                                HomeTab.PROFILE -> "EXCUSES don’t burn CALORIES"
                                HomeTab.WORKOUT -> "No Pain. No Gain."
                                HomeTab.DIET -> "Eat clean. Get fit. Be happy."
                                HomeTab.PROGRESS -> "Track your fitness journey"
                                HomeTab.COACH -> "Ask smart. Train better."
                                HomeTab.SETTINGS -> null
                            }
                            subtitle?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                },
                actions = {
                    when (currentTab) {
                        HomeTab.PROFILE -> IconButton(
                            onClick = { selectedTab = HomeTab.SETTINGS }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Open settings",
                                tint = TextPrimary
                            )
                        }

                        HomeTab.PROGRESS -> IconButton(onClick = { /* Existing share action */ }) {
                            Icon(
                                imageVector = Icons.Rounded.IosShare,
                                contentDescription = "Share Analytics",
                                tint = TextPrimary
                            )
                        }

                        HomeTab.WORKOUT,
                        HomeTab.DIET -> WorkoutStreakIndicator(
                            streakDays = state.analytics.currentStreak
                        )

                        else -> Unit
                    }
                }
            )
        },

        bottomBar = {
            NavigationBar(
                containerColor = DeepSpace,
                tonalElevation = 0.dp
            ) {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,

                        onClick = {
                            selectedTab = tab

                            if (tab == HomeTab.COACH) {
                                showCoachChat = false
                            }
                        },

                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = GlassWhite,

                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),

                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },

                        label = {
                            Text(tab.label)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when (currentTab) {

                HomeTab.PROFILE ->
                    ProfileScreen(state)

                HomeTab.WORKOUT ->
                    WorkoutScreen(state, viewModel)

                HomeTab.DIET ->
                    DietScreen(state)

                HomeTab.PROGRESS ->
                    ProgressScreen(
                        state = state,
                        onOpenCoach = {
                            showCoachChat = false
                            selectedTab = HomeTab.COACH

                        }
                    )
                HomeTab.COACH -> {
                    if (showCoachChat) {
                        CoachChatScreen()
                    } else {
                        CoachIntroScreen(
                            onGetStarted = {
                                showCoachChat = true
                            }
                        )
                    }
                }

                HomeTab.SETTINGS ->
                    SettingsScreen(state, viewModel)
            }
        }
    }
}

@Composable
private fun WorkoutStreakIndicator(streakDays: Int) {
    Row(
        modifier = Modifier.padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.LocalFireDepartment,
            contentDescription = "Current workout streak",
            tint = TextPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = streakDays.toString(),
            color = TextPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

