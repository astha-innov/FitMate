package com.fitmate.ui.meals

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import com.fitmate.domain.model.MealSlot
import com.fitmate.ui.viewmodel.CampusFitUiState
import com.fitmate.ui.viewmodel.CampusFitViewModel
import com.fitmate.ui.components.EnumSelection
import com.fitmate.ui.components.GoalProgressLine
import com.fitmate.ui.components.NumericOrTextField
import com.fitmate.ui.components.SectionCard

@Composable
fun MealsScreen(
    state: CampusFitUiState,
    viewModel: CampusFitViewModel
) {
    val meals = state.meals ?: return

    var slot by rememberSaveable {
        mutableStateOf(MealSlot.BREAKFAST)
    }

    var description by rememberSaveable {
        mutableStateOf("")
    }

    Text(
        "Today's meal",
        style = MaterialTheme.typography.headlineSmall
    )

    SectionCard("Mess plate analyser") {

        EnumSelection(
            "Meal slot",
            MealSlot.entries,
            slot,
            { it.label }
        ) {
            slot = it
        }

        NumericOrTextField(
            "Describe your meal",
            description,
            null,
            minLines = 4
        ) {
            description = it
        }

        Button(
            onClick = {
                viewModel.analyzeMeal(
                    slot,
                    description
                )
            },
            shape = RoundedCornerShape(18)
        ) {
            Text("Analyse meal with AI")
        }
    }

    state.profileSnapshot?.let { profileSnapshot ->

        SectionCard("Today's goal progress") {

            GoalProgressLine(
                "Calories",
                meals.progress.caloriesConsumed,
                profileSnapshot.metrics.caloriesTarget,
                meals.progress.caloriesRatio(
                    profileSnapshot.metrics.caloriesTarget
                )
            )

            GoalProgressLine(
                "Protein",
                meals.progress.proteinConsumed,
                profileSnapshot.metrics.proteinTarget,
                meals.progress.proteinRatio(
                    profileSnapshot.metrics.proteinTarget
                )
            )
        }
    }

    meals.latestAnalysis?.let { analysis ->

        SectionCard("Latest meal analysis") {

            Text(
                "${analysis.slot.label}: ${analysis.estimatedCalories} kcal, ${analysis.estimatedProtein} g protein",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                analysis.reasoning,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Suggestions")

            analysis.suggestions.forEach {
                Text("- $it")
            }

            Text("What to avoid")

            analysis.avoid.forEach {
                Text("- $it")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MealsScreenPreview() {
}

