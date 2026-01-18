package com.example.fitness_plan.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Показываем эмодзи бодибилдера как placeholder
        // Добавьте файл bodybuilder.gif в res/drawable для отображения GIF
        Text(
            text = "💪",
            fontSize = 80.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

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
    }
}
