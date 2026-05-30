package com.fitmate.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun

import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.LocalDining

import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueryStats

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitmate.ui.dashboard.DashboardScreen
import com.fitmate.ui.profile.ProfileScreen
import com.fitmate.ui.meals.MealsScreen
import com.fitmate.ui.progress.ProgressScreen
import com.fitmate.ui.viewmodel.CampusFitUiState
import com.fitmate.ui.viewmodel.CampusFitViewModel
import com.fitmate.ui.workout.WorkoutScreen
enum class HomeTab(val label: String) {
    DASHBOARD("Dashboard"),
    MEALS("Meals"),
    WORKOUT("Workout"),
    PROGRESS("Progress"),
    PROFILE("Profile")
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

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Column {

                        Text(
                            text = "FitMate",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = state.personalizedPlan?.aiSummary
                                ?: "AI-powered fitness",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        },

        bottomBar = {

            NavigationBar {

                HomeTab.entries.forEach { tab ->

                    NavigationBarItem(
                        selected = selectedTab == tab,

                        onClick = {
                            selectedTab = tab
                        },

                        icon = {
                            Icon(
                                imageVector = tab.icon(),
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            when (selectedTab) {

                HomeTab.DASHBOARD ->
                    DashboardScreen(state)

                HomeTab.MEALS ->
                    MealsScreen(state, viewModel)



                HomeTab.WORKOUT ->
                    WorkoutScreen(state)



                HomeTab.PROFILE ->

                ProfileScreen(state)
                HomeTab.PROGRESS ->
                    ProgressScreen(state)


            }
            }
        }
    }
private fun HomeTab.icon(): ImageVector =
    when (this) {
        HomeTab.DASHBOARD -> Icons.Outlined.Bolt
        HomeTab.MEALS -> Icons.Outlined.LocalDining
        HomeTab.WORKOUT -> Icons.AutoMirrored.Outlined.DirectionsRun
        HomeTab.PROGRESS -> Icons.Outlined.QueryStats
        HomeTab.PROFILE -> Icons.Outlined.Person
    }