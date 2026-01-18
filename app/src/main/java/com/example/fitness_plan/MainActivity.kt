package com.example.fitness_plan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitness_plan.data.UserRepository
import com.example.fitness_plan.ui.HomeScreen
// import com.example.fitness_plan.ui.UserProfile // Эту строку удаляем
import com.example.fitness_plan.ui.UserProfileForm // <-- ЭТУ СТРОКУ ДОБАВЛЯЕМ
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            val context = LocalContext.current
            val userRepository = remember { UserRepository(context) }
            val userProfileViewModel = remember { UserProfileViewModel(userRepository) }

            NavHost(
                navController = navController,
                startDestination = "welcome"
            ) {
                composable("welcome") {
                    WelcomeScreen(
                        onGetStartedClick = { navController.navigate("profile_form") },
                        onLoginClick = { /* TODO */ }
                    )
                }
                composable("profile_form") {
                    UserProfileForm(
                        onProfileSaved = { profile ->
                            userProfileViewModel.saveUserProfile(profile)
                            navController.navigate("home") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                    )
                }
                composable("home") {
                    HomeScreen()
                }
            }
        }
    }
}
