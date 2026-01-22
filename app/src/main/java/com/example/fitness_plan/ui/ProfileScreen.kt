package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Профиль", "Статистика", "Аккаунт")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Профиль"
                            1 -> "Статистика"
                            else -> "Аккаунт"
                        }
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Filled.Person
                                    1 -> Icons.AutoMirrored.Filled.List
                                    else -> Icons.Filled.Settings
                                },
                                contentDescription = null
                            )
                        },
                        label = { Text(text = title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (selectedTab) {
                0 -> {
                    // Simple profile content
                    Text("Профиль", style = MaterialTheme.typography.headlineMedium)
                    userProfile?.let {
                        Text("Имя: ${it.username}")
                        Text("Цель: ${it.goal}")
                        Text("Вес: ${it.weight} кг")
                    } ?: Text("Профиль не заполнен")
                }
                1 -> {
                    // Simple stats content
                    Text("Статистика", style = MaterialTheme.typography.headlineMedium)
                    userProfile?.let {
                        Text("Рост: ${it.height} см")
                        Text("Пол: ${it.gender}")
                    } ?: Text("Заполните профиль")
                }
                2 -> {
                    // Simple account content
                    Text("Аккаунт", style = MaterialTheme.typography.headlineMedium)
                    Text("Настройки аккаунта")
                }
            }

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
}
