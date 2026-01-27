package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.VolumeEntry
import com.example.fitness_plan.presentation.viewmodel.VolumeTimeFilter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeDetailDialog(
    volumeEntry: VolumeEntry,
    selectedFilter: com.example.fitness_plan.presentation.viewmodel.VolumeTimeFilter,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = getVolumeDetailTitle(volumeEntry, selectedFilter),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VolumeDetailSummary(volumeEntry)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Тренировки:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(volumeEntry.stats.groupBy { it.date }.entries.toList()) { (date, stats) ->
                        DateExerciseCard(date, stats)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Закрыть",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun VolumeDetailSummary(volumeEntry: VolumeEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryItem(
                label = "Объём",
                value = formatVolumeDetail(volumeEntry.volume),
                color = MaterialTheme.colorScheme.primary
            )
            SummaryItem(
                label = "Упражнений",
                value = volumeEntry.exerciseCount.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun DateExerciseCard(date: Long, stats: List<ExerciseStats>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formatExerciseDate(date),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            stats.forEach { stat ->
                ExerciseStatRow(stat)
            }

            val totalVolume = stats.sumOf { it.volume }
            Text(
                text = "Итого: ${formatVolumeDetail(totalVolume)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ExerciseStatRow(stat: ExerciseStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stat.exerciseName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${stat.weight} × ${stat.reps}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatVolumeDetail(stat.volume),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

fun getVolumeDetailTitle(
    volumeEntry: VolumeEntry,
    selectedFilter: VolumeTimeFilter
): String {
    return when (selectedFilter) {
        VolumeTimeFilter.DAY -> {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = volumeEntry.date
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            "$hour:${minute.toString().padStart(2, '0')}"
        }
        VolumeTimeFilter.WEEK -> {
            val sdf = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            sdf.format(Date(volumeEntry.date))
        }
        VolumeTimeFilter.MONTH,
        VolumeTimeFilter.YEAR,
        VolumeTimeFilter.ALL -> {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = volumeEntry.date
            val endDate = calendar.clone() as Calendar
            endDate.add(Calendar.DAY_OF_MONTH, 6)

            val startSdf = SimpleDateFormat("d MMM", Locale("ru"))
            val endSdf = SimpleDateFormat("d MMM", Locale("ru"))

            "${startSdf.format(calendar.time)} - ${endSdf.format(endDate.time)}"
        }
    }
}

fun formatVolumeDetail(volume: Long): String {
    return if (volume >= 1000000) {
        "%.2fM".format(volume / 1000000.0)
    } else if (volume >= 1000) {
        "%.1fK".format(volume / 1000.0)
    } else {
        "$volume"
    }
}

fun formatExerciseDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
    return sdf.format(Date(timestamp))
}
