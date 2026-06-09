package com.fitmate.ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitmate.ui.components.SectionCard

@Composable
fun AICoachScreen() {

    var prompt by remember {
        mutableStateOf("")
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        SectionCard("AI Fitness Coach") {

            Text(
                "Ask anything about workouts, diet, recovery, muscle gain, or fat loss."
            )

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Ask AI Coach") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ask")
            }
        }

        SectionCard("Coach Response") {
            Text("AI response will appear here.")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AICoachScreenPreview() {
    AICoachScreen()
}
