package com.example.fitness_plan.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.domain.model.ProgressTimeFilter
import com.example.fitness_plan.presentation.viewmodel.StatisticsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val progressChartData by viewModel.progressChartData.collectAsState()
    val selectedTimeFilter by viewModel.selectedProgressTimeFilter.collectAsState()
    val selectedExercise by viewModel.selectedProgressExercise.collectAsState()
    val exerciseStats by viewModel.exerciseStats.collectAsState()
    val availableExercises by remember(exerciseStats) {
        derivedStateOf { viewModel.getAvailableExercises() }
    }

    LaunchedEffect(Unit) {
        if (availableExercises.isNotEmpty() && selectedExercise == null) {
            viewModel.setProgressExercise(availableExercises.first())
        }
    }

    LaunchedEffect(selectedTimeFilter, selectedExercise) {
        viewModel.setProgressTimeFilter(selectedTimeFilter)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Прогресс упражнений",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProgressTimeFilterRow(
            selectedFilter = selectedTimeFilter,
            onFilterSelected = { viewModel.setProgressTimeFilter(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProgressExerciseDropdown(
            exercises = availableExercises,
            selectedExercise = selectedExercise,
            onExerciseSelected = { viewModel.setProgressExercise(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedExercise != null) {
            if (progressChartData.isNotEmpty()) {
                ProgressChart(
                    chartData = progressChartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProgressStatsCard(
                    chartData = progressChartData,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "Нет данных за выбранный период",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "Выберите упражнение для просмотра прогресса",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressTimeFilterRow(
    selectedFilter: ProgressTimeFilter,
    onFilterSelected: (ProgressTimeFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProgressTimeFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressExerciseDropdown(
    exercises: List<String>,
    selectedExercise: String?,
    onExerciseSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedExercise ?: "Выберите упражнение",
            onValueChange = {},
            readOnly = true,
            label = { Text("Упражнение") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Все упражнения") },
                onClick = {
                    onExerciseSelected(null)
                    expanded = false
                }
            )

            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise) },
                    onClick = {
                        onExerciseSelected(exercise)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProgressChart(
    chartData: List<com.example.fitness_plan.domain.model.ProgressChartData>,
    modifier: Modifier = Modifier
) {
    if (chartData.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "Нет данных",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val xValues = chartData.map { it.xValue }
    val yValues = chartData.map { it.yValue }
    val minX = xValues.minOrNull() ?: 0.0
    val maxX = xValues.maxOrNull() ?: 0.0
    val minY = yValues.minOrNull() ?: 0.0
    val maxY = yValues.maxOrNull() ?: 0.0

    val xRange = maxX - minX
                val yRange = maxY - minY

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    modifier = modifier
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
            val width = size.width
            val height = size.height

            val padding = 40f
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2

            val stepX = if (chartData.size > 1) {
                chartWidth / (chartData.size - 1)
            } else {
                chartWidth / 2
            }

            val path = androidx.compose.ui.graphics.Path()

            chartData.forEachIndexed { index, data ->
                val normalizedX = if (xRange > 0) {
                    (data.xValue - minX) / xRange
                } else {
                    0.5f
                }
                val x = padding + index * stepX

                val normalizedY = if (yRange > 0) {
                    ((data.yValue - minY) / yRange).toFloat()
                } else {
                    0.5f
                }
                val chartYPixel = chartHeight * normalizedY
                val y = height - padding - chartYPixel

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 4.dp.toPx())
            )

            chartData.forEachIndexed { index, data ->
                val x = padding + index * stepX

                val normalizedY = if (yRange > 0) {
                    ((data.yValue - minY) / yRange).toFloat()
                } else {
                    0.5f
                }
                val chartYPixel = chartHeight * normalizedY
                val y = height - padding - chartYPixel

                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )

                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = Offset(x, y)
                )

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#6B7280")
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                drawContext.canvas.nativeCanvas.drawText(
                    data.xLabel,
                    x,
                    height - 5.dp.toPx(),
                    paint
                )

                val yLabel = "%.1f".format(data.yValue)
                drawContext.canvas.nativeCanvas.drawText(
                    yLabel,
                    x,
                    padding - 10.dp.toPx(),
                    paint
                )
            }
        }
    }
}

@Composable
fun ProgressStatsCard(
    chartData: List<com.example.fitness_plan.domain.model.ProgressChartData>,
    modifier: Modifier = Modifier
) {
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
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            val latestData = chartData.lastOrNull()
            val oldestData = chartData.firstOrNull()

            if (latestData != null && oldestData != null) {
                val change = latestData.yValue - oldestData.yValue
                val changeText = if (change > 0) "+%.1f".format(change) else "%.1f".format(change)
                val changeColor = if (change > 0) Color(0xFF2DD4BF) else if (change < 0) Color(0xFFE55C5C) else Color(0xFF6B7280)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProgressStatsItem(
                        label = "Начало",
                        value = "%.1f".format(oldestData.yValue),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ProgressStatsItem(
                        label = "Изменение",
                        value = changeText,
                        color = changeColor
                    )

                    ProgressStatsItem(
                        label = "Сейчас",
                        value = "%.1f".format(latestData.yValue),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProgressStatsItem(
                        label = "Записей",
                        value = "${chartData.size}",
                        color = MaterialTheme.colorScheme.secondary
                    )

                    val dateRangeDays = ((latestData.date - oldestData.date) / (24 * 60 * 60 * 1000L)).toInt()
                    ProgressStatsItem(
                        label = "Дней",
                        value = "$dateRangeDays",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressStatsItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatProgressDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("d MMM", Locale("ru"))
    return sdf.format(Date(timestamp))
}
