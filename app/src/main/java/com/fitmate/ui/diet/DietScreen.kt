package com.fitmate.ui.diet

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fitmate.ui.viewmodel.CampusFitUiState
import com.fitmate.ui.components.SectionCard

@Composable
fun DietScreen(
    state: CampusFitUiState
) {
    val diet = state.personalizedPlan?.dietRecommendation ?: return

    SectionCard(diet.title) {

        diet.meals.forEach {
            Text("- $it")
        }

        HorizontalDivider()

        Text("Affordable proteins")

        diet.cheapProteins.forEach {
            Text("- $it")
        }

        HorizontalDivider()

        Text("Avoid")

        diet.avoid.forEach {
            Text("- $it")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DietScreenPreview() {
}