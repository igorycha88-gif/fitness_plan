package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class DeviceType {
    COMPACT,    // Phone portrait, Fold folded (< 600dp)
    MEDIUM,     // Phone landscape, Fold half-opened (600-840dp)
    EXPANDED    // Tablet, Fold unfolded (> 840dp)
}

enum class DeviceOrientation {
    PORTRAIT,
    LANDSCAPE
}

data class AdaptiveInfo(
    val deviceType: DeviceType,
    val orientation: DeviceOrientation,
    val screenWidth: Dp,
    val screenHeight: Dp,
    val isFoldable: Boolean,
    val isFolded: Boolean
)

@Composable
fun rememberAdaptiveInfo(): AdaptiveInfo {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

    val orientation = if (screenWidthDp > screenHeightDp) {
        DeviceOrientation.LANDSCAPE
    } else {
        DeviceOrientation.PORTRAIT
    }

    val deviceType = when {
        screenWidthDp < 600 -> DeviceType.COMPACT
        screenWidthDp < 840 -> DeviceType.MEDIUM
        else -> DeviceType.EXPANDED
    }

    val isFoldable = screenWidthDp in listOf(360, 400, 768, 800, 904, 1812)
    val isFolded = screenWidthDp < 500

    return remember(configuration) {
        AdaptiveInfo(
            deviceType = deviceType,
            orientation = orientation,
            screenWidth = screenWidthDp.dp,
            screenHeight = screenHeightDp.dp,
            isFoldable = isFoldable,
            isFolded = isFolded
        )
    }
}

@Composable
fun getScreenPadding(): PaddingValues {
    val adaptiveInfo = rememberAdaptiveInfo()
    return when (adaptiveInfo.deviceType) {
        DeviceType.COMPACT -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        DeviceType.MEDIUM -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        DeviceType.EXPANDED -> PaddingValues(horizontal = 32.dp, vertical = 16.dp)
    }
}

@Composable
fun getCardElevation(): Dp {
    return when (rememberAdaptiveInfo().deviceType) {
        DeviceType.COMPACT -> 2.dp
        DeviceType.MEDIUM -> 4.dp
        DeviceType.EXPANDED -> 6.dp
    }
}

@Composable
fun getCornerRadius(): Dp {
    return when (rememberAdaptiveInfo().deviceType) {
        DeviceType.COMPACT -> 12.dp
        DeviceType.MEDIUM -> 16.dp
        DeviceType.EXPANDED -> 20.dp
    }
}

@Composable
fun getContentMaxWidth(): Dp {
    return when (rememberAdaptiveInfo().deviceType) {
        DeviceType.COMPACT -> androidx.compose.ui.unit.Dp.Infinity
        DeviceType.MEDIUM -> 600.dp
        DeviceType.EXPANDED -> 800.dp
    }
}

@Composable
fun getGridColumns(): Int {
    return when {
        rememberAdaptiveInfo().deviceType == DeviceType.EXPANDED -> 3
        rememberAdaptiveInfo().deviceType == DeviceType.MEDIUM -> 2
        else -> 1
    }
}

@Composable
fun adaptiveScrollColumn(
    modifier: Modifier = Modifier
): Modifier {
    val scrollState = rememberScrollState()
    return modifier
        .fillMaxWidth()
        .verticalScroll(scrollState)
}

@Composable
fun adaptivePadding(
    modifier: Modifier = Modifier
): Modifier {
    val padding = getScreenPadding()
    return modifier.padding(padding)
}

@Composable
fun getSpacing(): Dp {
    return when (rememberAdaptiveInfo().deviceType) {
        DeviceType.COMPACT -> 12.dp
        DeviceType.MEDIUM -> 16.dp
        DeviceType.EXPANDED -> 24.dp
    }
}

@Composable
fun getIconSize(): Dp {
    return when (rememberAdaptiveInfo().deviceType) {
        DeviceType.COMPACT -> 20.dp
        DeviceType.MEDIUM -> 24.dp
        DeviceType.EXPANDED -> 28.dp
    }
}

@Composable
fun getButtonHeight(): Dp {
    return when (rememberAdaptiveInfo().deviceType) {
        DeviceType.COMPACT -> 48.dp
        DeviceType.MEDIUM -> 52.dp
        DeviceType.EXPANDED -> 56.dp
    }
}

@Composable
fun getChartHeight(): Dp {
    return when (rememberAdaptiveInfo().deviceType) {
        DeviceType.COMPACT -> 200.dp
        DeviceType.MEDIUM -> 250.dp
        DeviceType.EXPANDED -> 300.dp
    }
}

@Composable
fun shouldShowTwoPane(): Boolean {
    val info = rememberAdaptiveInfo()
    return info.deviceType == DeviceType.EXPANDED ||
           (info.isFoldable && !info.isFolded)
}

@Composable
fun isCompactDevice(): Boolean {
    return rememberAdaptiveInfo().deviceType == DeviceType.COMPACT
}
