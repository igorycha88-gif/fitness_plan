package com.example.fitness_plan.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WeightEntry
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

private const val TAG = "ProfileScreen"

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isEditing by viewModel.isEditingProfile.collectAsState()
    val profileChangeDetected by viewModel.profileChangeDetected.collectAsState()
    val weightHistory by viewModel.getWeightHistory().collectAsState(initial = emptyList())
    val workoutReminderEnabled by viewModel.workoutReminderEnabled.collectAsState()
    val context = LocalContext.current

    var editedProfile by remember { mutableStateOf<UserProfile?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingWorkoutReminderToggle by remember { mutableStateOf<Boolean?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingWorkoutReminderToggle?.let { enabled ->
                viewModel.toggleWorkoutReminder(enabled)
                pendingWorkoutReminderToggle = null
            }
        } else {
            pendingWorkoutReminderToggle = false
            Toast.makeText(context, "Уведомления отключены", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(userProfile) {
        editedProfile = userProfile
    }

    Scaffold(
        topBar = {
            ProfileTopBar(
                title = "Профиль",
                isEditing = isEditing,
                onBackClick = { },
                onEditClick = { viewModel.setIsEditingProfile(true) },
                onSaveClick = {
                    editedProfile?.let { profile ->
                        viewModel.saveProfileWithConfirmation(profile, requireConfirmation = true)
                    }
                },
                onCancelClick = {
                    viewModel.setIsEditingProfile(false)
                    editedProfile = userProfile
                },
                onNotificationClick = { showNotificationDialog = true }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
        ) {
            editedProfile?.let { profile ->
                ProfileCard(
                    profile = profile,
                    isEditing = isEditing,
                    onPhotoClick = { },
                    onUsernameChange = { username ->
                        editedProfile = profile.copy(username = username)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (profile.goal == UserProfile.GOAL_WEIGHT_LOSS) {
                    GoalProgressSection(
                        profile = profile,
                        currentWeight = weightHistory.firstOrNull()?.weight
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ProfileInfoSection(
                    profile = profile,
                    isEditing = isEditing,
                    onGoalChange = { goal ->
                        editedProfile = profile.copy(goal = goal)
                    },
                    onLevelChange = { level ->
                        editedProfile = profile.copy(level = level)
                    },
                    onFrequencyChange = { frequency ->
                        editedProfile = profile.copy(frequency = frequency)
                    },
                    onWeightChange = { weight ->
                        val weightValue = weight.toDoubleOrNull()
                        if (weightValue != null && weightValue > 0) {
                            editedProfile = profile.copy(weight = weightValue)
                        }
                    },
                    onHeightChange = { height ->
                        val heightValue = height.toDoubleOrNull()
                        if (heightValue != null && heightValue > 0) {
                            editedProfile = profile.copy(height = heightValue)
                        }
                    },
                    onGenderChange = { gender ->
                        editedProfile = profile.copy(gender = gender)
                    },
                    onTargetWeightChange = { targetWeight ->
                        val targetWeightValue = targetWeight.toDoubleOrNull()
                        editedProfile = profile.copy(targetWeight = targetWeightValue)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsSection(
                    onSettingsClick = { showNotificationDialog = true },
                    workoutReminderEnabled = workoutReminderEnabled,
                    onWorkoutReminderToggle = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                
                                if (hasPermission) {
                                    viewModel.toggleWorkoutReminder(true)
                                } else {
                                    pendingWorkoutReminderToggle = true
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                viewModel.toggleWorkoutReminder(true)
                            }
                        } else {
                            viewModel.toggleWorkoutReminder(false)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!isEditing) {
                    OutlinedButton(
                        onClick = {
                            viewModel.logout()
                            onLogoutClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Выйти из аккаунта")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (showNotificationDialog) {
        NotificationDialog(
            showNotifications = true,
            onDismiss = { showNotificationDialog = false }
        )
    }

    if (showConfirmationDialog) {
        ProfileChangeConfirmationDialog(
            onDismiss = {
                showConfirmationDialog = false
                viewModel.clearProfileChangeDetected()
            },
            onConfirm = {
                editedProfile?.let { profile ->
                    viewModel.confirmAndSaveProfile(profile)
                }
                showConfirmationDialog = false
            }
        )
    }

    LaunchedEffect(profileChangeDetected) {
        if (profileChangeDetected) {
            showConfirmationDialog = true
        }
    }
}
