package com.example.fitness_plan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fitness_plan.ui.WelcomeScreen
import com.example.fitness_plan.ui.theme.Fitness_planTheme

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
            WelcomeScreen(
                onGetStartedClick = { /* Перейти к главному экрану */ },
                onLoginClick = { /* Перейти к входу */ }
            )
        }
    }
}