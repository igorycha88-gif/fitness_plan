package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile content
        Text("Профиль", style = MaterialTheme.typography.headlineMedium)
        userProfile?.let {
            Text("Имя: ${it.username}")
            Text("Цель: ${it.goal}")
            Text("Вес: ${it.weight} кг")
        } ?: Text("Профиль не заполнен")

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                viewModel.logout()
                onLogoutClick()
            },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выйти из аккаунта")
        }
    }
}
