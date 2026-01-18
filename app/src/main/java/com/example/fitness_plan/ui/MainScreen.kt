package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Определяем маршруты и иконки для наших вкладок
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Главная", Icons.Default.Home)
    object Profile : Screen("profile", "Профиль", Icons.Default.AccountCircle)
}

private val items = listOf(Screen.Home, Screen.Profile)

@Composable
fun MainScreen(
    mainNavController: NavHostController // Контроллер для глобальных переходов (например, выход из аккаунта)
) {
    val bottomNavController = rememberNavController() // Контроллер для навигации между вкладками

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                // Настройка поведения для предотвращения накопления стека
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Навигация внутри главного экрана
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Profile.route) {
                // Получаем ту же ViewModel, что и в остальном приложении
                val viewModel: UserProfileViewModel = hiltViewModel()

                UserProfileForm(
                    viewModel = viewModel,
                    onProfileSaved = {
                        // После сохранения переключаем вкладку на "Главную"
                        bottomNavController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
