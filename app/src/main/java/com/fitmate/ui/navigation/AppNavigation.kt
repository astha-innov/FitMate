package com.fitmate.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitmate.ui.dashboard.DashboardScreen
import com.fitmate.ui.meals.MealsScreen
import com.fitmate.ui.profile.ProfileScreen
import com.fitmate.ui.progress.ProgressScreen
import com.fitmate.ui.viewmodel.CampusFitUiState
import com.fitmate.ui.viewmodel.CampusFitViewModel
import com.fitmate.ui.workout.WorkoutScreen

private val NeonCyan = Color(0xFF00E5FF)
private val DeepSpace = Color(0xFF05070A)
private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.05f)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFFFFFFF).copy(alpha = 0.7f)

enum class HomeTab(
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD(
        "Dashboard",
        Icons.Outlined.Bolt
    ),
    MEALS(
        "Meals",
        Icons.Outlined.LocalDining
    ),
    WORKOUT(
        "Workout",
        Icons.AutoMirrored.Outlined.DirectionsRun
    ),
    PROGRESS(
        "Progress",
        Icons.Outlined.QueryStats
    ),
    PROFILE(
        "Profile",
        Icons.Outlined.Person
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    state: CampusFitUiState,
    viewModel: CampusFitViewModel
) {
    var selectedTab by rememberSaveable {
        mutableStateOf(HomeTab.DASHBOARD)
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
                    Column {
                        Text(
                            text = "FitMate",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = state.personalizedPlan?.aiSummary
                                ?: "AI-powered fitness",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            )
        },

        bottomBar = {
            NavigationBar(
                containerColor = DeepSpace,
                tonalElevation = 0.dp
            ) {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,

                        onClick = {
                            if (selectedTab != tab) {
                                selectedTab = tab
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

                HomeTab.DASHBOARD ->
                    DashboardScreen(state)

                HomeTab.MEALS ->
                    MealsScreen(state, viewModel)

                HomeTab.WORKOUT ->
                    WorkoutScreen(state)

                HomeTab.PROGRESS ->
                    ProgressScreen(state)

                HomeTab.PROFILE ->
                    ProfileScreen(state)
            }
        }
    }
}