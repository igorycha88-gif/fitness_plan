
package com.example.fitness_plan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// Импорты UI пакета
import com.example.fitness_plan.ui.MainScreen
import com.example.fitness_plan.ui.UserProfileForm
import com.example.fitness_plan.ui.UserProfileViewModel
import com.example.fitness_plan.ui.WelcomeScreen
import com.example.fitness_plan.ui.theme.Fitness_planTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitnessApp()
        }
    }
}

@Composable
fun FitnessApp() {
    Fitness_planTheme {
        val userProfileViewModel: UserProfileViewModel = hiltViewModel()

        // Используем State для отслеживания готовности данных
        val isProfileChecked by userProfileViewModel.isProfileChecked.collectAsState()
        val currentPlan by userProfileViewModel.currentWorkoutPlan.collectAsState()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Ждем, пока ViewModel проверит состояние профиля
            if (!isProfileChecked) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val mainNavController = rememberNavController()
                val startDest = if (currentPlan != null) "main_tabs" else "welcome"

                NavHost(
                    navController = mainNavController,
                    startDestination = startDest
                ) {
                    // ИСПРАВЛЕНИЕ ЗДЕСЬ (Строка 64 находилась в этом блоке в старом коде)
                    composable(route = "welcome") {
                        WelcomeScreen(
                            onGetStartedClick = {
                                // Переход к форме профиля при нажатии "Начать"
                                mainNavController.navigate(route = "profile_form")
                            },
                            onLoginClick = {
                                // Здесь должна быть логика перехода на экран входа (login_screen), если он есть
                                // mainNavController.navigate(route = "login_screen")
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
                    composable("main_tabs") {
                        MainScreen(mainNavController)
                    }
                }
            }
        }
    }
}
