package com.example.fitness_plan.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.domain.model.MuscleGroupStatsFilter
import com.example.fitness_plan.presentation.viewmodel.MuscleGroupStatsViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleGroupStatsScreen(
    onMuscleGroupDetail: (com.example.fitness_plan.domain.model.MuscleGroup) -> Unit,
    viewModel: MuscleGroupStatsViewModel = hiltViewModel()
) {
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val summaries by viewModel.summaries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика по группам мышц") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { viewModel.setFilter(it) }
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (summaries.isEmpty()) {
                item {
                    EmptyStateMessage()
                }
            } else {
                items(summaries) { summary ->
                    MuscleGroupItem(
                        summary = summary,
                        onClick = { onMuscleGroupDetail(summary.muscleGroup) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: MuscleGroupStatsFilter,
    onFilterSelected: (MuscleGroupStatsFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MuscleGroupStatsFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                modifier = Modifier.height(40.dp)
            )
        }
    }
}

@Composable
private fun EmptyStateMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Нет данных для отображения",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Выполняйте тренировки, чтобы увидеть статистику по группам мышц",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MuscleGroupItem(
    summary: com.example.fitness_plan.domain.model.MuscleGroupSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.muscleGroup.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                StatusIndicator(daysSinceLastWorkout = summary.daysSinceLastWorkout)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Объём",
                    value = formatVolume(summary.totalVolume)
                )

                MetricItem(
                    label = "Подходы",
                    value = summary.totalSets.toString()
                )

                MetricItem(
                    label = "Упражнения",
                    value = summary.exerciseCount.toString()
                )

                MetricItem(
                    label = "Макс. вес",
                    value = "${formatWeight(summary.maxWeight)} кг"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = summary.percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusIndicator(daysSinceLastWorkout: Int) {
    val (text, color) = when {
        daysSinceLastWorkout <= 3 -> "Свежая" to MaterialTheme.colorScheme.primary
        daysSinceLastWorkout <= 7 -> "Норма" to MaterialTheme.colorScheme.tertiary
        daysSinceLastWorkout <= 14 -> "Внимание" to MaterialTheme.colorScheme.secondary
        else -> "Давно" to MaterialTheme.colorScheme.error
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = if (daysSinceLastWorkout == Int.MAX_VALUE) "Никогда" else "$daysSinceLastWorkout дн.",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
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
    return DecimalFormat("#.#").format(weight)
}