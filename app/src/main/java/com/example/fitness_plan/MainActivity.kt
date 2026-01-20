package com.example.fitness_plan

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.fitness_plan.ui.ExerciseDetailScreen
import com.example.fitness_plan.ui.LoginScreen
import com.example.fitness_plan.ui.MainScreen
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel
import com.example.fitness_plan.ui.RegisterScreen
import com.example.fitness_plan.ui.UserProfileForm
import com.example.fitness_plan.ui.WelcomeScreen
import com.example.fitness_plan.ui.theme.Fitness_planTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Fitness_planTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                                Log.d(TAG, "No user logged in, showing welcome screen")
                                startDestination = "welcome"
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error checking credentials", e)
                            startDestination = "welcome"
                        }
                    }

                    startDestination?.let { start ->
                        NavHost(
                            navController = navController,
                            startDestination = start
                        ) {
                            composable("welcome") {
                                WelcomeScreen(
                                    onGetStartedClick = { navController.navigate("login_screen") }
                                )
                            }

                            composable("login_screen") {
                                LoginScreen(
                                    onLoginSuccess = {
                                        navController.navigate("main_tabs") {
                                            popUpTo("welcome") { inclusive = true }
                                        }
                                    },
                                    onRegisterClick = {
                                        navController.navigate("register_screen")
                                    }
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

                            composable("exercise_detail/{exerciseName}") { backStackEntry ->
                                val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: ""
                                ExerciseDetailScreen(
                                    exerciseName = exerciseName,
                                    onBackClick = { navController.popBackStack() },
                                    workoutViewModel = workoutViewModel
                                )
                            }

                            composable("main_tabs") {
                                MainScreen(
                                    mainNavController = navController,
                                    profileViewModel = profileViewModel,
                                    workoutViewModel = workoutViewModel,
                                    onExerciseClick = { exercise ->
                                        val encodedName = URLEncoder.encode(exercise.name, "UTF-8")
                                        navController.navigate("exercise_detail/$encodedName")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
