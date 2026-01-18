
package com.example.fitness_plan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitness_plan.notification.NotificationHelper
import com.example.fitness_plan.notification.ScheduleCheckWorker
import com.example.fitness_plan.ui.MainScreen
import com.example.fitness_plan.ui.RegisterScreen
import com.example.fitness_plan.ui.UserProfileForm
import com.example.fitness_plan.ui.UserProfileViewModel
import com.example.fitness_plan.ui.WelcomeScreen
import com.example.fitness_plan.ui.LoginScreen
import com.example.fitness_plan.ui.ExerciseDetailScreen
import com.example.fitness_plan.ui.theme.Fitness_planTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        // Schedule periodic schedule check
        ScheduleCheckWorker.schedulePeriodicWork(this)

        setContent {
            FitnessApp()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@Composable
fun FitnessApp() {
    Fitness_planTheme {
        val userProfileViewModel: UserProfileViewModel = hiltViewModel()

        val isProfileChecked by userProfileViewModel.isProfileChecked.collectAsState()
        val currentPlan by userProfileViewModel.currentWorkoutPlan.collectAsState()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!isProfileChecked) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val mainNavController = rememberNavController()

                NavHost(
                    navController = mainNavController,
                    startDestination = "login_screen"
                ) {
                    composable(route = "welcome") {
                        WelcomeScreen(
                            onGetStartedClick = {
                                mainNavController.navigate(route = "profile_form")
                            }
                        )
                    }

                    composable("profile_form") {
                        UserProfileForm(
                            viewModel = userProfileViewModel,
                            onProfileSaved = {
                                mainNavController.navigate("main_tabs") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("login_screen") {
                        LoginScreen(
                            onLoginSuccess = {
                                mainNavController.navigate("welcome") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            },
                            onBackClick = {
                                mainNavController.popBackStack()
                            },
                            onRegisterClick = {
                                mainNavController.navigate("register_screen")
                            }
                        )
                    }
                    composable("exercise_detail/{exerciseName}") { backStackEntry ->
                        val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: ""
                        ExerciseDetailScreen(
                            exerciseName = exerciseName,
                            onBackClick = { mainNavController.popBackStack() }
                        )
                    }
                    composable("register_screen") {
                        RegisterScreen(
                            onRegisterSuccess = {
                                mainNavController.navigate("profile_form")
                            },
                            onBackClick = {
                                mainNavController.popBackStack()
                            }
                        )
                    }
                    composable("main_tabs") {
                        MainScreen(
                            mainNavController = mainNavController,
                            onExerciseClick = { exercise ->
                                mainNavController.navigate("exercise_detail/${exercise.name}")
                            }
                        )
                    }
                }
            }
        }
    }
}
