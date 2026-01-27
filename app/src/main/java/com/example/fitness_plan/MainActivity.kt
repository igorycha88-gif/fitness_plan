package com.example.fitness_plan

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitness_plan.ui.AdminMainScreen
import com.example.fitness_plan.ui.ExerciseDetailScreen
import com.example.fitness_plan.ui.ExerciseGuideScreen
import com.example.fitness_plan.ui.LoginScreen
import com.example.fitness_plan.ui.MainScreen
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel
import com.example.fitness_plan.ui.RegisterScreen
import com.example.fitness_plan.ui.UserProfileForm
import com.example.fitness_plan.ui.theme.Fitness_planTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Fitness_planTheme {
                val navController = rememberNavController()
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val workoutViewModel: WorkoutViewModel = hiltViewModel()
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    try {
                        val credentials = profileViewModel.getCredentials()
                        if (credentials != null) {
                            Log.d(TAG, "User is logged in: ${credentials.username}")
                            startDestination = "main_tabs"
                        } else {
                            Log.d(TAG, "No user logged in, showing login screen")
                            startDestination = "login_screen"
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error checking credentials", e)
                        startDestination = "login_screen"
                    }
                }

                startDestination?.let { start ->
                    NavHost(
                        navController = navController,
                        startDestination = start
                    ) {
                        composable("login_screen") {
                            LoginScreen(
                                onLoginSuccess = {
                                    // Check if user is admin
                                    val isAdmin = profileViewModel.isAdmin.value
                                    val destination = if (isAdmin) "admin_main" else "main_tabs"
                                    navController.navigate(destination) {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onRegisterClick = {
                                    navController.navigate("register_screen")
                                },
                                onAdminLoginClick = {
                                    navController.navigate("admin_login")
                                },
                                viewModel = profileViewModel
                            )
                        }

                        composable("admin_login") {
                            com.example.fitness_plan.ui.AdminLoginScreen(
                                navController = navController
                            )
                        }

                        composable("register_screen") {
                            RegisterScreen(
                                onRegisterSuccess = { username ->
                                    navController.navigate("profile_form/$username") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onBackClick = {
                                    navController.navigate("login_screen")
                                }
                            )
                        }

                        composable("profile_form/{username}") { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: ""
                            UserProfileForm(
                                viewModel = profileViewModel,
                                username = username,
                                onProfileSaved = {
                                    navController.navigate("main_tabs") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "exercise_detail/{exerciseName}/{dayIndex}",
                            arguments = listOf(
                                androidx.navigation.navArgument("exerciseName") { type = androidx.navigation.NavType.StringType },
                                androidx.navigation.navArgument("dayIndex") { type = androidx.navigation.NavType.IntType; defaultValue = -1 }
                            )
                        ) { backStackEntry ->
                            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: ""
                            val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: -1
                            val isAdmin = navController.previousBackStackEntry?.destination?.route == "admin_main"
                            ExerciseDetailScreen(
                                exerciseName = exerciseName,
                                dayIndex = dayIndex,
                                onBackClick = { navController.popBackStack() },
                                workoutViewModel = workoutViewModel,
                                isAdmin = isAdmin
                            )
                        }

                        composable("admin_main") {
                            AdminMainScreen(
                                mainNavController = navController,
                                profileViewModel = profileViewModel,
                                workoutViewModel = workoutViewModel,
                                onExerciseClick = { exercise, dayIndex ->
                                    val encodedName = URLEncoder.encode(exercise.name, "UTF-8")
                                    navController.navigate("exercise_detail/$encodedName/$dayIndex")
                                }
                            )
                        }

                        composable("main_tabs") {
                            MainScreen(
                                mainNavController = navController,
                                profileViewModel = profileViewModel,
                                workoutViewModel = workoutViewModel,
                                onExerciseClick = { exercise, dayIndex ->
                                    val encodedName = URLEncoder.encode(exercise.name, "UTF-8")
                                    navController.navigate("exercise_detail/$encodedName/$dayIndex")
                                },
                                onExerciseLibraryClick = { exercise ->
                                    navController.navigate("exercise_guide/${exercise.id}")
                                }
                            )
                        }

                        composable(
                            route = "exercise_guide/{exerciseId}",
                            arguments = listOf(
                                androidx.navigation.navArgument("exerciseId") { type = androidx.navigation.NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                            ExerciseGuideScreen(
                                exerciseId = exerciseId,
                                onBackClick = { navController.popBackStack() },
                                profileViewModel = profileViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}