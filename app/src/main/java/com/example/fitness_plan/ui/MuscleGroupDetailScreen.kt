package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.MuscleGroupDetail
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleGroupDetailScreen(
    muscleGroup: com.example.fitness_plan.domain.model.MuscleGroup,
    detail: MuscleGroupDetail?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(muscleGroup.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (detail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "Нет данных для этой группы мышц",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                DetailMetricsCard(detail = detail)
                Spacer(modifier = Modifier.height(16.dp))
                RecentExercisesCard(detail = detail)
            }
        }
    }
}

@Composable
private fun DetailMetricsCard(detail: MuscleGroupDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Основные показатели",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailMetricItem(
                    label = "Общий объём",
                    value = formatVolume(detail.totalVolume)
                )

                DetailMetricItem(
                    label = "Подходы",
                    value = detail.totalSets.toString()
                )

                DetailMetricItem(
                    label = "Упражнения",
                    value = detail.totalExercises.toString()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailMetricItem(
                    label = "Макс. вес",
                    value = "${formatWeight(detail.maxWeight)} кг"
                )

                DetailMetricItem(
                    label = "Частота",
                    value = "${String.format("%.1f", detail.averageWeeklyFrequency)} р/нед"
                )
            }

            val lastWorkoutText = if (detail.lastWorkoutDate > 0) {
                val daysSinceLastWorkout = ((System.currentTimeMillis() - detail.lastWorkoutDate) / (24 * 60 * 60 * 1000L)).toInt()
                when {
                    daysSinceLastWorkout == 0 -> "Сегодня"
                    daysSinceLastWorkout == 1 -> "Вчера"
                    daysSinceLastWorkout < 7 -> "$daysSinceLastWorkout дн. назад"
                    else -> SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date(detail.lastWorkoutDate))
                }
            } else {
                "Нет данных"
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Последняя тренировка: $lastWorkoutText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentExercisesCard(detail: MuscleGroupDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Последние упражнения",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (detail.recentExercises.isEmpty()) {
                Text(
                    text = "Нет записей о выполненных упражнениях",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detail.recentExercises) { exercise ->
                        ExerciseInfoItem(exercise = exercise)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseInfoItem(exercise: com.example.fitness_plan.domain.model.MuscleGroupExerciseInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = exercise.formatDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    text = "${exercise.weight} кг × ${exercise.reps}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatVolume(exercise.volume),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailMetricItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatVolume(volume: Long): String {
    return when {
        volume >= 1_000_000 -> String.format("%.1fM", volume / 1_000_000.0)
        volume >= 1_000 -> String.format("%.1fK", volume / 1_000.0)
        else -> volume.toString()
    }
}

private fun formatWeight(weight: Double): String {
    return if (weight == weight.toLong().toDouble()) {
        weight.toLong().toString()
    } else {
        String.format("%.1f", weight)
    }
}