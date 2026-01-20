package com.example.fitness_plan.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FitnessPrimary,
    onPrimary = TextOnPrimary,
    onPrimaryContainer = FitnessPrimaryDark,
    primaryContainer = FitnessPrimaryLight,

    secondary = FitnessSecondary,
    onSecondary = TextOnSecondary,
    onSecondaryContainer = FitnessSecondaryDark,
    secondaryContainer = FitnessSecondaryLight,

    tertiary = FitnessTertiary,
    onTertiary = TextOnPrimary,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = CardLight,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = TextOnPrimary,
    errorContainer = ErrorRedLight,

    outline = TextTertiary,
    outlineVariant = Color(0xFFE5E7EB)
)

private val DarkColorScheme = darkColorScheme(
    primary = FitnessPrimary,
    onPrimary = TextOnPrimary,
    onPrimaryContainer = FitnessPrimaryLight,
    primaryContainer = FitnessPrimaryDark,

    secondary = FitnessSecondary,
    onSecondary = TextOnSecondary,
    onSecondaryContainer = FitnessSecondaryLight,
    secondaryContainer = FitnessSecondaryDark,

    tertiary = FitnessTertiary,
    onTertiary = TextOnPrimary,

    background = BackgroundDark,
    onBackground = Color(0xFFE8E8EC),

    surface = SurfaceDark,
    onSurface = Color(0xFFE8E8EC),
    surfaceVariant = CardDark,
    onSurfaceVariant = TextTertiary,

    error = ErrorRed,
    onError = TextOnPrimary,
    errorContainer = ErrorRedLight,

    outline = TextTertiary,
    outlineVariant = Color(0xFF3A3A3A)
)

@Composable
fun Fitness_planTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
