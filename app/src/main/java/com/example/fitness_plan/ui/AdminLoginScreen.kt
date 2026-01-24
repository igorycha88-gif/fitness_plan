package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.presentation.viewmodel.AdminLoginViewModel
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    navController: NavHostController,
    viewModel: AdminLoginViewModel = hiltViewModel()
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val loginResult by viewModel.loginResult.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Админ вход") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = { Text("Логин") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { viewModel.login() }, modifier = Modifier.fillMaxWidth()) {
                Text("Войти как Админ")
            }
            loginResult?.let { ok ->
                if (ok) {
                    LaunchedEffect(Unit) {
                        profileViewModel.setCurrentUsername(username)
                        profileViewModel.setIsAdmin(true)
                        navController.navigate("admin_main") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    }
                } else {
                    Text("Неверные учетные данные", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
