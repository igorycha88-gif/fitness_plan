package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}

@Composable
fun adaptivePadding(): WindowInsets {
    return if (isTablet()) {
        WindowInsets(48.dp, 48.dp, 48.dp, 48.dp)
    } else {
        WindowInsets(24.dp, 24.dp, 24.dp, 24.dp)
    }
}
