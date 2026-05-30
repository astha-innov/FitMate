package com.fitmate.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fitmate.ui.components.SectionCard

@Composable
fun HabitsScreen() {

    var water by remember { mutableStateOf(false) }
    var workout by remember { mutableStateOf(false) }
    var protein by remember { mutableStateOf(false) }

    SectionCard("Daily Habits") {

        HabitItem(
            title = "Drank enough water",
            checked = water,
            onCheckedChange = { water = it }
        )

        HabitItem(
            title = "Completed workout",
            checked = workout,
            onCheckedChange = { workout = it }
        )

        HabitItem(
            title = "Hit protein goal",
            checked = protein,
            onCheckedChange = { protein = it }
        )
    }
}

@Composable
private fun HabitItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HabitsScreenPreview() {
    HabitsScreen()
}