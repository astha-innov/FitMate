package com.fitmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun GoalProgressLine(
    label: String,
    current: Int,
    target: Int,
    progress: Float,
    suffix: String = ""
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "$label - $current/$target${
                if (suffix.isNotBlank()) " $suffix" else ""
            }",
            style = MaterialTheme.typography.titleMedium
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(24.dp))
        )
    }
}

@Composable
fun GoalProgressLine(
    label: String,
    current: Double,
    target: Double,
    progress: Float,
    suffix: String = ""
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "$label - ${"%.1f".format(current)}/${"%.1f".format(target)}${
                if (suffix.isNotBlank()) " $suffix" else ""
            }",
            style = MaterialTheme.typography.titleMedium
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(24.dp))
        )
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge
            )

            content()
        }
    }
}

@Composable
fun NumericField(
    label: String,
    value: String,
    errorText: String?,
    onValueChange: (String) -> Unit
) {
    FieldBase(
        label,
        value,
        errorText,
        1,
        onValueChange
    )
}

@Composable
fun NumericOrTextField(
    label: String,
    value: String,
    errorText: String?,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    FieldBase(
        label,
        value,
        errorText,
        minLines,
        onValueChange
    )
}

@Composable
fun FieldBase(
    label: String,
    value: String,
    errorText: String?,
    minLines: Int,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(label)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            isError = errorText != null,
            singleLine = minLines == 1,
            minLines = minLines
        )

        if (errorText != null) {
            Text(
                errorText,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> EnumSelection(
    label: String,
    options: Iterable<T>,
    selected: T,
    display: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            label,
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            options.forEach { option ->

                FilterChip(
                    selected = option == selected,
                    onClick = {
                        onSelected(option)
                    },
                    label = {
                        Text(display(option))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextSelection(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            label,
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            options.forEach { option ->

                FilterChip(
                    selected = option == selected,
                    onClick = {
                        onSelected(option)
                    },
                    label = {
                        Text(option)
                    }
                )
            }
        }
    }
}

fun requireField(
    value: String
): String? =
    if (value.isBlank())
        "This field is required"
    else
        null

fun Boolean.thenError(
    error: String?
): String? =
    if (this)
        error
    else
        null