package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Главная", Icons.Default.Home)
    object Profile : Screen("profile", "Профиль", Icons.Default.AccountCircle)
    object Statistics : Screen("statistics", "Статистика", Icons.Default.List)
    object CycleHistory : Screen("cycle_history", "История циклов", Icons.Default.Home)
}

private val items = listOf(Screen.Home, Screen.Profile, Screen.Statistics)



@Composable
fun MainScreen(
    mainNavController: NavHostController,
    profileViewModel: ProfileViewModel? = null,
    workoutViewModel: WorkoutViewModel? = null,
    onExerciseClick: (Exercise) -> Unit = {}
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry = bottomNavController.currentBackStackEntryAsState().value
                val currentDestination = navBackStackEntry?.destination

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
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
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
    }
}
