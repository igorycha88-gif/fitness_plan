package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

// Умный адаптивный TopBar
@Composable
fun AdaptiveTopBar(
    contentType: BarContentType = BarContentType.ICON_AND_TEXT,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dimensions = calculateAdaptiveBarDimensions(BarType.TOP, contentType)

    if (dimensions.height > 0.dp) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(dimensions.height)
                .windowInsetsPadding(WindowInsets.statusBars),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = getCardElevation()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensions.padding),
                contentAlignment = Alignment.CenterStart
            ) {
                content()
            }
        }
    }
}

// Умный адаптивный BottomBar
@Composable
fun AdaptiveBottomBar(
    contentType: BarContentType = BarContentType.ICON_ONLY,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dimensions = calculateAdaptiveBarDimensions(BarType.BOTTOM, contentType)

    if (dimensions.height > 0.dp) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(dimensions.height)
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = getCardElevation()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensions.padding),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

// Интеллектуальная адаптация размеров баров
@Composable
fun calculateAdaptiveBarDimensions(
    barType: BarType,
    contentType: BarContentType,
    screenInsets: ScreenInsets = calculateAdaptiveInsets()
): AdaptiveBarDimensions {
    val adaptiveInfo = rememberAdaptiveInfo()

    val baseHeight = when (barType) {
        BarType.TOP -> when (contentType) {
            BarContentType.EMPTY -> 0.dp // Полностью скрыт при отсутствии контента
            BarContentType.ICON_ONLY -> 48.dp
            BarContentType.TEXT_ONLY -> 56.dp
            BarContentType.ICON_AND_TEXT -> 64.dp
            BarContentType.FULL_CONTENT -> 72.dp
        }
        BarType.BOTTOM -> when (contentType) {
            BarContentType.EMPTY -> 0.dp // Полностью скрыт при отсутствии контента
            BarContentType.ICON_ONLY -> 56.dp // Минимальный для навигации
            BarContentType.TEXT_ONLY -> 64.dp
            BarContentType.ICON_AND_TEXT -> 72.dp
            BarContentType.FULL_CONTENT -> 80.dp
        }
    }

    // Адаптация под устройство
    val deviceMultiplier = when (adaptiveInfo.deviceType) {
        DeviceType.COMPACT -> 0.9f
        DeviceType.MEDIUM -> 1.0f
        DeviceType.EXPANDED -> 1.1f
    }

    // Адаптация под размер экрана
    val screenMultiplier = when {
        adaptiveInfo.screenHeight < 600.dp -> 0.85f
        adaptiveInfo.screenHeight > 1000.dp -> 1.15f
        else -> 1.0f
    }

    val finalHeight = if (contentType == BarContentType.EMPTY) {
        0.dp
    } else {
        val minHeight = when (barType) {
            BarType.TOP -> 40.dp
            BarType.BOTTOM -> 48.dp
        }
        val maxHeight = when (barType) {
            BarType.TOP -> 120.dp
            BarType.BOTTOM -> 120.dp
        }
        (baseHeight * deviceMultiplier * screenMultiplier).coerceIn(minHeight, maxHeight)
    }

    // Расчет отступов
    val padding = when (barType) {
        BarType.TOP -> PaddingValues(
            start = 16.dp * deviceMultiplier,
            end = 16.dp * deviceMultiplier,
            top = 8.dp * deviceMultiplier,
            bottom = 8.dp * deviceMultiplier
        )
        BarType.BOTTOM -> PaddingValues(
            start = 12.dp * deviceMultiplier,
            end = 12.dp * deviceMultiplier,
            top = 4.dp * deviceMultiplier,
            bottom = 4.dp * deviceMultiplier
        )
    }

    val contentPadding = when (barType) {
        BarType.TOP -> PaddingValues(
            horizontal = 8.dp * deviceMultiplier,
            vertical = 4.dp * deviceMultiplier
        )
        BarType.BOTTOM -> PaddingValues(
            horizontal = 6.dp * deviceMultiplier,
            vertical = 2.dp * deviceMultiplier
        )
    }

    return AdaptiveBarDimensions(
        height = finalHeight,
        padding = padding,
        contentPadding = contentPadding
    )
}

enum class BarType {
    TOP, BOTTOM
}

// Интеллектуальная адаптация контента без скролла
@Composable
fun calculateContentAdaptation(
    availableHeight: Dp,
    contentType: ContentType = ContentType.NORMAL
): ContentAdaptation {
    val adaptiveInfo = rememberAdaptiveInfo()
    val screenHeight = adaptiveInfo.screenHeight

    // Расчет соотношения доступного пространства
    val heightRatio = availableHeight / screenHeight

    // Базовые размеры
    val baseTextSize = when (adaptiveInfo.deviceType) {
        DeviceType.COMPACT -> 14.sp
        DeviceType.MEDIUM -> 16.sp
        DeviceType.EXPANDED -> 18.sp
    }

    val baseSpacing = when (adaptiveInfo.deviceType) {
        DeviceType.COMPACT -> 8.dp
        DeviceType.MEDIUM -> 12.dp
        DeviceType.EXPANDED -> 16.dp
    }

    // Интеллектуальные множители на основе доступного пространства
    val textMultiplier = when {
        heightRatio < 0.4f -> 0.8f  // Очень мало места
        heightRatio < 0.6f -> 0.9f  // Мало места
        heightRatio > 0.8f -> 1.1f  // Много места
        heightRatio > 1.0f -> 1.2f  // Очень много места
        else -> 1.0f
    }

    val spacingMultiplier = when {
        heightRatio < 0.4f -> 0.6f  // Компактнее spacing
        heightRatio < 0.6f -> 0.8f  // Меньше spacing
        heightRatio > 0.8f -> 1.1f  // Больше spacing
        else -> 1.0f
    }

    // Учитываем тип контента
    val contentMultiplier = when (contentType) {
        ContentType.COMPACT -> 0.9f
        ContentType.NORMAL -> 1.0f
        ContentType.SPACIOUS -> 1.1f
    }

    val finalTextMultiplier = textMultiplier * contentMultiplier
    val finalSpacingMultiplier = spacingMultiplier * contentMultiplier

    return ContentAdaptation(
        textSize = (baseTextSize.value * finalTextMultiplier).coerceIn(12f, 24f).sp,
        spacing = (baseSpacing * finalSpacingMultiplier).coerceIn(4.dp, 24.dp),
        iconSize = (getIconSize() * finalTextMultiplier).coerceIn(16.dp, 36.dp),
        buttonHeight = (getButtonHeight() * finalSpacingMultiplier).coerceIn(40.dp, 64.dp),
        cornerRadius = (getCornerRadius() * finalSpacingMultiplier).coerceIn(8.dp, 24.dp)
    )
}

enum class ContentType {
    COMPACT,    // Для очень компактного контента
    NORMAL,     // Стандартный контент
    SPACIOUS    // Для просторного контента
}

enum class BarContentType {
    EMPTY,      // Нет контента - минимальный размер
    ICON_ONLY,  // Только иконки
    TEXT_ONLY,  // Только текст
    ICON_AND_TEXT, // Иконки + текст
    FULL_CONTENT   // Полный контент (кнопки, формы и т.д.)
}

data class ContentAdaptation(
    val textSize: TextUnit,
    val spacing: Dp,
    val iconSize: Dp,
    val buttonHeight: Dp,
    val cornerRadius: Dp
)

data class AdaptiveBarDimensions(
    val height: Dp,
    val padding: PaddingValues,
    val contentPadding: PaddingValues
)

@Composable
fun getLoginImageMaxHeight(): Dp {
    val screenHeight = rememberAdaptiveInfo().screenHeight
    return (screenHeight * 0.35f).coerceIn(150.dp, 280.dp)
}

@Composable
fun getLoginScreenPadding(): Dp {
    return when (rememberAdaptiveInfo().deviceType) {
        DeviceType.COMPACT -> 16.dp
        DeviceType.MEDIUM -> 20.dp
        DeviceType.EXPANDED -> 24.dp
    }
}

@Composable
fun getLoginScreenSpacing(): Dp {
    return when (rememberAdaptiveInfo().deviceType) {
        DeviceType.COMPACT -> 12.dp
        DeviceType.MEDIUM -> 16.dp
        DeviceType.EXPANDED -> 24.dp
    }
}

@Composable
fun getExerciseImageMaxHeight(): Dp {
    val screenHeight = rememberAdaptiveInfo().screenHeight
    return (screenHeight * 0.45f).coerceIn(250.dp, 400.dp)
}
