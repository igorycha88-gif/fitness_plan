package com.example.fitness_plan.ui

import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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

enum class NavigationMode {
    BUTTONS_3,    // 3 кнопки (back, home, recent)
    BUTTONS_2,    // 2 кнопки (back, recent)
    GESTURES      // Жестовая навигация
}

// Расчет адаптивных отступов
@Composable
private fun calculateAdaptiveInsets(
    isFoldDevice: Boolean,
    isFoldedMode: Boolean,
    navigationMode: NavigationMode,
    configuration: Configuration
): AdaptiveInsets {
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Базовые отступы в зависимости от режима навигации
    val baseNavBarHeight = when (navigationMode) {
        NavigationMode.GESTURES -> 0.dp  // Жесты не занимают место
        NavigationMode.BUTTONS_2 -> 48.dp
        NavigationMode.BUTTONS_3 -> 56.dp
    }

    // Дополнительные отступы для Samsung Fold
    val foldMultiplier = if (isFoldDevice) {
        if (isFoldedMode) 1.2f else 1.0f
    } else {
        1.0f
    }

    // Адаптация под размер экрана
    val screenMultiplier = when {
        screenHeight < 600.dp -> 0.8f  // Маленькие экраны
        screenHeight > 1000.dp -> 1.2f  // Большие экраны
        else -> 1.0f
    }

    val navBarPadding = (baseNavBarHeight * foldMultiplier * screenMultiplier).coerceIn(0.dp, 120.dp)
    val contentPadding = (navBarPadding + 24.dp).coerceIn(24.dp, 144.dp)

    return AdaptiveInsets(navBarPadding, contentPadding)
}

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

    // Расчет адаптивных отступов
    val adaptiveInsets = calculateAdaptiveInsets(
        isFoldDevice = isFoldDevice,
        isFoldedMode = isFoldedMode,
        navigationMode = navigationMode,
        configuration = configuration
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
