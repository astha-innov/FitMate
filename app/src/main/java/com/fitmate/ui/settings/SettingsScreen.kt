package com.fitmate.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import com.fitmate.domain.model.AppThemeMode
import androidx.compose.ui.unit.dp
import com.fitmate.ui.viewmodel.CampusFitUiState
import com.fitmate.ui.viewmodel.CampusFitViewModel
import com.fitmate.ui.components.NumericField
import com.fitmate.ui.components.SectionCard

@Composable
fun SettingsScreen(
    state: CampusFitUiState,
    viewModel: CampusFitViewModel
) {

    var age by rememberSaveable {
        mutableStateOf(state.profile.age.toString())
    }

    var height by rememberSaveable {
        mutableStateOf(state.profile.heightCm.toString())
    }

    var weight by rememberSaveable {
        mutableStateOf(state.profile.weightKg.toString())
    }

    var workout by rememberSaveable {
        mutableStateOf(state.profile.workoutMinutes.toString())
    }

    SectionCard("Appearance") {

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            FilterChip(
                selected = state.themeMode ==
                        AppThemeMode.LIGHT,
                onClick = {
                    viewModel.updateThemeMode(
                        AppThemeMode.LIGHT
                    )
                },
                label = {
                    Text("Light")
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.LightMode,
                        null
                    )
                }
            )

            FilterChip(
                selected = state.themeMode ==
                        AppThemeMode.DARK,
                onClick = {
                    viewModel.updateThemeMode(
                        AppThemeMode.DARK
                    )
                },
                label = {
                    Text("Dark")
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.DarkMode,
                        null
                    )
                }
            )
        }
    }

    SectionCard("Personal info memory") {

        NumericField(
            "Age",
            age,
            null
        ) {
            age = it.filter(Char::isDigit)
        }

        NumericField(
            "Height (cm)",
            height,
            null
        ) {
            height = it.filter(Char::isDigit)
        }

        NumericField(
            "Weight (kg)",
            weight,
            null
        ) {
            weight = it.filter(Char::isDigit)
        }

        NumericField(
            "Workout time (minutes)",
            workout,
            null
        ) {
            workout = it.filter(Char::isDigit)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text("Daily reminders")

            Switch(
                checked = state.profileSnapshot
                    ?.disciplineState
                    ?.remindersEnabled == true,

                onCheckedChange = {
                    viewModel.toggleReminders()
                }
            )
        }

        Button(
            onClick = {
                viewModel.updateProfile(
                    state.profile.copy(
                        age =
                            age.toIntOrNull()
                                ?: state.profile.age,

                        heightCm =
                            height.toIntOrNull()
                                ?: state.profile.heightCm,

                        weightKg =
                            weight.toIntOrNull()
                                ?: state.profile.weightKg,

                        workoutMinutes =
                            workout.toIntOrNull()
                                ?: state.profile.workoutMinutes
                    )
                )
            },
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Update personal info")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
}
