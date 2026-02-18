package com.example.fitness_plan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private const val TAG = "MainActivity"

private val HEALTH_CONNECT_PERMISSIONS = setOf(
    HealthPermission.getReadPermission(HeartRateRecord::class),
    HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getReadPermission(DistanceRecord::class)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var healthConnectClient: HealthConnectClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectClient = HealthConnectClient.getOrCreate(this)
        
        setContent {
            Fitness_planTheme {
                val navController = rememberNavController()
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val workoutViewModel: WorkoutViewModel = hiltViewModel()
                var startDestination by remember { mutableStateOf<String?>(null) }

                val healthSettingsLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { _ ->
                    lifecycleScope.launch {
                        // Даём время Health Connect обновить состояние
                        delay(1000)
                        try {
                            val granted = healthConnectClient.permissionController.getGrantedPermissions()
                            val allGranted = HEALTH_CONNECT_PERMISSIONS.all { it in granted }
                            Log.d(TAG, "Health permissions result after settings: allGranted=$allGranted, granted=$granted")
                            
                            if (allGranted) {
                                workoutViewModel.onPermissionsResult(true)
                                // Запускаем мониторинг если разрешения получены
                                workoutViewModel.checkHealthConnectAvailability()
                            } else {
                                workoutViewModel.onPermissionsResult(false)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error checking permissions", e)
                            // Пробуем ещё раз через 2 секунды
                            delay(2000)
                            try {
                                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                                val allGranted = HEALTH_CONNECT_PERMISSIONS.all { it in granted }
                                Log.d(TAG, "Health permissions result (retry): allGranted=$allGranted, granted=$granted")
                                workoutViewModel.onPermissionsResult(allGranted)
                                if (allGranted) {
                                    workoutViewModel.checkHealthConnectAvailability()
                                }
                            } catch (e2: Exception) {
                                Log.e(TAG, "Error checking permissions (retry)", e2)
                                workoutViewModel.onPermissionsResult(false)
                            }
                        }
                    }
                }

                val requestHealthPermissions by workoutViewModel.requestHealthPermissions.collectAsState()

                LaunchedEffect(requestHealthPermissions) {
                    if (requestHealthPermissions) {
                        Log.d(TAG, "Launching Health Connect permissions request")
                        
                        var intentOpened = false
                        
                        // Способ 1: Открыть Health Connect напрямую
                        try {
                            val intent = Intent().apply {
                                action = "androidx.health.ACTION_REQUEST_PERMISSIONS"
                                putExtra("androidx.health.extra.PERMISSIONS", HEALTH_CONNECT_PERMISSIONS.toTypedArray())
                            }
                            healthSettingsLauncher.launch(intent)
                            intentOpened = true
                            Log.d(TAG, "Opened Health Connect directly")
                        } catch (e: Exception) {
                            Log.d(TAG, "Health Connect direct intent failed: ${e.message}")
                        }
                        
                        // Способ 2: Открыть Samsung Health → Health Connect
                        if (!intentOpened) {
                            try {
                                val intent = Intent().apply {
                                    action = Intent.ACTION_VIEW
                                    data = Uri.parse("shealth://healthconnect")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                healthSettingsLauncher.launch(intent)
                                intentOpened = true
                                Log.d(TAG, "Opened Samsung Health Health Connect")
                            } catch (e: Exception) {
                                Log.d(TAG, "Samsung Health Health Connect intent failed: ${e.message}")
                            }
                        }
                        
                        // Способ 3: Открыть Samsung Health (главный экран)
                        if (!intentOpened) {
                            val samsungHealthIntent = packageManager.getLaunchIntentForPackage("com.sec.android.app.shealth")
                            if (samsungHealthIntent != null) {
                                try {
                                    samsungHealthIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    healthSettingsLauncher.launch(samsungHealthIntent)
                                    intentOpened = true
                                    Log.d(TAG, "Opened Samsung Health main screen")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error opening Samsung Health", e)
                                }
                            }
                        }
                        
                        // Способ 4: Открыть настройки приложений
                        if (!intentOpened) {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:$packageName")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                healthSettingsLauncher.launch(intent)
                                Log.d(TAG, "Opened app settings")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error opening app settings", e)
                            }
                        }
                        
                        workoutViewModel.onPermissionRequestHandled()
                    }
                }

                LaunchedEffect(Unit) {
                    try {
                        val packageInfo = packageManager.getPackageInfo(packageName, 0)
                        val currentVersion = packageInfo.versionName
                        Log.d(TAG, "Current app version: $currentVersion")

                        val hasValidSession = profileViewModel.checkSession(currentVersion)

                        if (hasValidSession) {
                            val credentials = profileViewModel.getCredentials()
                            val profile = profileViewModel.userProfile.first()

                            if (credentials != null && profile != null) {
                                Log.d(TAG, "User is logged in: ${credentials.username}, profile: ${profile.username}")
                                startDestination = "main_tabs"
                            } else {
                                Log.d(TAG, "Invalid state - credentials=$credentials, profile=$profile, showing login")
                                profileViewModel.logout()
                                startDestination = "login_screen"
                            }
                        } else {
                            Log.d(TAG, "No valid session, showing login screen")
                            startDestination = "login_screen"
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error checking session", e)
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