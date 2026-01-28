package com.example.fitness_plan.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.MuscleGroupSummary
import java.text.DecimalFormat

@Composable
fun MuscleGroupList(
    summaries: List<MuscleGroupSummary>,
    onMuscleGroupClick: (com.example.fitness_plan.domain.model.MuscleGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(summaries) { summary ->
            MuscleGroupItem(
                summary = summary,
                onClick = { onMuscleGroupClick(summary.muscleGroup) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MuscleGroupItem(
    summary: MuscleGroupSummary,
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