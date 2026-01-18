package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.data.Exercise
import com.example.fitness_plan.data.WorkoutDay
import com.example.fitness_plan.data.WorkoutPlan

@OptIn(ExperimentalMaterial3Api::class) // <-- Добавлена аннотация OptIn
@Composable
fun HomeScreen(
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    // Собираем состояние плана тренировок из ViewModel
    val workoutPlan by viewModel.currentWorkoutPlan.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Мой план тренировок") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)) {

            if (workoutPlan == null) {
                // Состояние загрузки или отсутствия плана
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                    Text("Загрузка плана...", modifier = Modifier.padding(top = 40.dp))
                }
            } else {
                // Отображение плана, если он доступен
                PlanDetailsScreen(plan = workoutPlan!!)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // <-- Добавлена аннотация OptIn
@Composable
fun PlanDetailsScreen(plan: WorkoutPlan) {
    Column {
        Text(text = plan.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = plan.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Список тренировочных дней
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(plan.days) { day ->
                WorkoutDayCard(day = day)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // <-- Добавлена аннотация OptIn
@Composable
fun WorkoutDayCard(day: WorkoutDay) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = day.dayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            // Список упражнений на день
            day.exercises.forEachIndexed { index, exercise ->
                ExerciseRow(exercise = exercise, isLast = index == day.exercises.lastIndex)
            }
        }
    }
}

@Composable
fun ExerciseRow(exercise: Exercise, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = exercise.name, style = MaterialTheme.typography.bodyLarge)
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "${exercise.sets} сетов", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = exercise.reps, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (!isLast) {
        Divider(modifier = Modifier.padding(top = 4.dp))
    }
}
