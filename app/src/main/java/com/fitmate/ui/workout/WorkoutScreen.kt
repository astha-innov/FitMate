package com.fitmate.ui.workout

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fitmate.ui.viewmodel.CampusFitUiState
import com.fitmate.ui.components.SectionCard

@Composable
fun WorkoutScreen(
    state: CampusFitUiState
) {
    val workout = state.personalizedPlan?.workoutPlan ?: return

    SectionCard(workout.title) {

        Text("Split: ${workout.split}")

        Text("Duration: ${workout.durationLabel}")

        workout.exercises.forEach {
            Text("- $it")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutScreenPreview() {
}