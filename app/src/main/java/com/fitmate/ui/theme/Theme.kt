package com.fitmate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Coral,
    onPrimary = Cloud,
    primaryContainer = Sand,
    onPrimaryContainer = Ink,
    secondary = Mint,
    background = Cloud,
    onBackground = Ink,
    surface = Cloud,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Ink,
)

private val DarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = Ink,
    primaryContainer = NightCard,
    onPrimaryContainer = Cloud,
    secondary = Coral,
    background = Night,
    onBackground = Cloud,
    surface = NightCard,
    onSurface = Cloud,
    surfaceVariant = NightCard,
    onSurfaceVariant = Sand,
)

@Composable
fun CampusFitTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = FitMateTypography,
        content = content,
    )
}
