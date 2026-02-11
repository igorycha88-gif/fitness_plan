package com.example.fitness_plan.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.domain.model.BodyParameter
import com.example.fitness_plan.presentation.viewmodel.BodyParametersStatsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

@Composable
fun BodyParametersTableCard(
    viewModel: BodyParametersStatsViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTypes by viewModel.selectedTypes.collectAsState()
    val filteredMeasurements by remember(selectedTypes) {
        derivedStateOf { viewModel.getFilteredMeasurements() }
    }

    Card(
        modifier = modifier.padding(horizontal = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Параметры тела: Таблица",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTypes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет выбранных параметров",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                BodyParametersTable(
                    measurements = filteredMeasurements,
                    selectedTypes = selectedTypes,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun BodyParametersTable(
    measurements: List<BodyParameter>,
    selectedTypes: Set<com.example.fitness_plan.domain.model.BodyParameterType>,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()
    val sdf = SimpleDateFormat("d MMM yyyy", Locale("ru"))

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScrollState)
                .fillMaxWidth()
        ) {
            TableCell(
                text = "Дата",
                isHeader = true,
                modifier = Modifier.width(100.dp)
            )

            selectedTypes.forEach { type ->
                TableCell(
                    text = "${type.displayName} (${type.unit})",
                    isHeader = true,
                    modifier = Modifier.width(100.dp)
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        if (measurements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет данных за выбранный период",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val groupedByDate = measurements.groupBy { measurement ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = measurement.date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }.toSortedMap()
            val sortedDates = groupedByDate.keys.sortedDescending()

            sortedDates.forEach { date ->
                val dayMeasurements = groupedByDate[date] ?: emptyList()
                val typeToValue = dayMeasurements.associateBy { it.parameterType }

                Row(
                    modifier = Modifier
                        .horizontalScroll(horizontalScrollState)
                        .fillMaxWidth()
                ) {
                    TableCell(
                        text = sdf.format(date),
                        isHeader = false,
                        modifier = Modifier.width(100.dp)
                    )

                    selectedTypes.forEach { type ->
                        val measurement = typeToValue[type]
                        TableCell(
                            text = if (measurement != null) {
                                "%.1f".format(measurement.value)
                            } else {
                                "--"
                            },
                            isHeader = false,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    isHeader: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(vertical = 8.dp, horizontal = 4.dp),
        color = if (isHeader) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Text(
            text = text,
            style = if (isHeader) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                if (text == "--") {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            },
            modifier = Modifier.padding(4.dp)
        )
    }
}
