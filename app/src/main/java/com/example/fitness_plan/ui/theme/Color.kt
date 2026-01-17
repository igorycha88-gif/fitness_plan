package com.example.fitness_plan.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// 💪 Фитнес-цвета (темные + акценты)
val FitnessDark = Color(0xFF121212) // тёмный фон
val FitnessPrimary = Color(0xFF00C853) // ярко-зелёный (энергия)
val FitnessSecondary = Color(0xFFFF9800) // оранжевый (действие)
val FitnessOnPrimary = Color.White // текст на зелёном
val FitnessOnBackground = Color(0xFFE0E0E0) // светлый текст на тёмном фоне

val FitnessThemeColors = lightColorScheme(
    primary = FitnessPrimary,
    secondary = FitnessSecondary,
    background = FitnessDark,
    onBackground = FitnessOnBackground,
    onPrimary = FitnessOnPrimary
)