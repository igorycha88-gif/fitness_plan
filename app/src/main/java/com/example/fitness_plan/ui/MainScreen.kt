package com.example.fitness_plan.ui

import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.domain.model.Exercise
import kotlin.math.roundToInt
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Главная", Icons.Default.Home)
    object Profile : Screen("profile", "Профиль", Icons.Default.AccountCircle)
    object Statistics : Screen("statistics", "Статистика", Icons.Default.List)
    object CycleHistory : Screen("cycle_history", "История циклов", Icons.Default.Home)
}

private val items = listOf(Screen.Home, Screen.Profile, Screen.Statistics)

// Функция определения режима навигации
@Composable
private fun getNavigationMode(context: android.content.Context): NavigationMode {
    return remember {
        try {
            val mode = Settings.Secure.getInt(
                context.contentResolver,
                "navigation_mode", 0
            )
            when (mode) {
                0 -> NavigationMode.BUTTONS_3 // 3 кнопки
                1 -> NavigationMode.BUTTONS_2 // 2 кнопки (без home)
                2 -> NavigationMode.GESTURES // Жесты
                else -> NavigationMode.BUTTONS_3
            }
        } catch (e: Exception) {
            // По умолчанию жесты на новых устройствах
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NavigationMode.GESTURES
            } else {
                NavigationMode.BUTTONS_3
            }
        }
    }
}

// Получение реальных метрик экрана
@Composable
private fun getScreenMetrics(context: android.content.Context, configuration: Configuration): ScreenMetrics {
    val density = LocalDensity.current
    val displayMetrics = remember {
        val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        metrics
    }

    return remember(displayMetrics, configuration) {
        ScreenMetrics(
            widthPx = displayMetrics.widthPixels,
            heightPx = displayMetrics.heightPixels,
            density = displayMetrics.density,
            widthDp = configuration.screenWidthDp,
            heightDp = configuration.screenHeightDp,
            smallestWidthDp = configuration.smallestScreenWidthDp
        )
    }
}

enum class NavigationMode {
    BUTTONS_3,    // 3 кнопки (back, home, recent)
    BUTTONS_2,    // 2 кнопки (back, recent)
    GESTURES      // Жестовая навигация
}

// Расчет адаптивных отступов с учетом реальных метрик
@Composable
private fun calculateAdaptiveInsets(
    isFoldDevice: Boolean,
    isFoldedMode: Boolean,
    navigationMode: NavigationMode,
    screenMetrics: ScreenMetrics
): AdaptiveInsets {
    val density = LocalDensity.current

    // Базовые отступы в зависимости от режима навигации (в пикселях)
    val baseNavBarHeightPx = when (navigationMode) {
        NavigationMode.GESTURES -> 0f  // Жесты не занимают место
        NavigationMode.BUTTONS_2 -> 48f * screenMetrics.density
        NavigationMode.BUTTONS_3 -> 56f * screenMetrics.density
    }

    // Адаптация под размер экрана (в пикселях)
    val screenHeightPx = screenMetrics.heightPx.toFloat()
    val screenWidthPx = screenMetrics.widthPx.toFloat()

    // Коэффициенты для разных типов устройств
    val deviceMultiplier = when {
        // Маленькие экраны (< 5 дюймов)
        screenMetrics.smallestWidthDp < 360 -> 0.7f
        // Средние экраны (5-6 дюймов)
        screenMetrics.smallestWidthDp < 400 -> 0.85f
        // Большие экраны (6+ дюймов)
        screenMetrics.smallestWidthDp < 600 -> 1.0f
        // Очень большие экраны (планшеты, foldables)
        else -> 1.1f
    }

    // Дополнительные отступы для Samsung Fold
    val foldMultiplier = if (isFoldDevice) {
        if (isFoldedMode) 1.3f else 1.1f
    } else {
        1.0f
    }

    // Адаптация под соотношение сторон
    val aspectRatioMultiplier = when {
        screenWidthPx / screenHeightPx > 0.7f -> 1.2f  // Широкие экраны
        screenWidthPx / screenHeightPx < 0.5f -> 0.8f  // Узкие экраны
        else -> 1.0f
    }

    // Финальный расчет
    val finalMultiplier = deviceMultiplier * foldMultiplier * aspectRatioMultiplier
    val navBarPaddingPx = baseNavBarHeightPx * finalMultiplier
    val navBarPadding = with(density) { navBarPaddingPx.toDp() }

    // Content padding всегда на 20dp больше NavBar padding
    val contentPadding = (navBarPadding + 20.dp).coerceIn(16.dp, 120.dp)

    // Ограничения для безопасности
    val safeNavBarPadding = navBarPadding.coerceIn(0.dp, 100.dp)
    val safeContentPadding = contentPadding.coerceIn(20.dp, 120.dp)

    return AdaptiveInsets(safeNavBarPadding, safeContentPadding)
}

data class ScreenMetrics(
    val widthPx: Int,
    val heightPx: Int,
    val density: Float,
    val widthDp: Int,
    val heightDp: Int,
    val smallestWidthDp: Int
)

data class AdaptiveInsets(
    val navBarPadding: Dp,
    val contentPadding: Dp
)

@Composable
fun MainScreen(
    mainNavController: NavHostController,
    profileViewModel: ProfileViewModel? = null,
    workoutViewModel: WorkoutViewModel? = null,
    onExerciseClick: (Exercise) -> Unit = {}
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry = bottomNavController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // Определение характеристик устройства
    val isFoldDevice = Build.MANUFACTURER.contains("samsung", ignoreCase = true) &&
                      Build.MODEL.contains("fold", ignoreCase = true)
    val isFoldedMode = configuration.screenWidthDp < 600
    val navigationMode = getNavigationMode(context)
    val screenMetrics = getScreenMetrics(context, configuration)

    // Расчет адаптивных отступов на основе реальных метрик
    val adaptiveInsets = calculateAdaptiveInsets(
        isFoldDevice = isFoldDevice,
        isFoldedMode = isFoldedMode,
        navigationMode = navigationMode,
        screenMetrics = screenMetrics
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Основной контент
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(bottom = adaptiveInsets.contentPadding) // Адаптивный отступ для NavigationBar
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onExerciseClick = onExerciseClick)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel ?: hiltViewModel(),
                    onLogoutClick = {
                        profileViewModel?.logout()
                        mainNavController.navigate("login_screen") { popUpTo("welcome") { inclusive = true } }
                    }
                )
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }
            composable(Screen.CycleHistory.route) {
                CycleHistoryScreen(navController = mainNavController)
            }
        }

        // NavigationBar внизу экрана с отступом от системных кнопок
        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(bottom = adaptiveInsets.navBarPadding) // Адаптивный отступ
        ) {
            items.forEach { screen ->
                NavigationBarItem(
                    icon = { Icon(screen.icon, contentDescription = null) },
                    label = { Text(screen.label) },
                    selected = currentDestination?.hierarchy?.any { navDestination -> navDestination.route == screen.route } == true,
                    onClick = {
                        bottomNavController.navigate(screen.route) {
                            popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
