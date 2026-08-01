package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    secondary = OrangeSecondary,
    onSecondary = Color.White,
    tertiary = CyanTertiary,
    background = SlateDarkBackground,
    onBackground = SlateDarkOnSurface,
    surface = SlateDarkSurface,
    onSurface = SlateDarkOnSurface,
    surfaceVariant = SlateDarkContainer
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    secondary = OrangeSecondary,
    onSecondary = Color.White,
    tertiary = CyanTertiary,
    background = SlateLightBackground,
    onBackground = SlateLightOnSurface,
    surface = SlateLightSurface,
    onSurface = SlateLightOnSurface,
    surfaceVariant = SlateLightContainer
)

@Composable
fun WorkoutAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}