package com.example.fitness_plan.ui

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Готов стать сильнее?",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Тренируйся умно. Следи за прогрессом. Достигай целей.",
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGetStartedClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Начать")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Уже есть аккаунт? Войти")
        }
    }
}