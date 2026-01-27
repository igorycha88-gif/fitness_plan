package com.example.fitness_plan.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness_plan.presentation.viewmodel.StatisticsViewModel
import com.example.fitness_plan.presentation.viewmodel.TimeFilter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(viewModel: StatisticsViewModel) {
    val weightHistory by viewModel.weightHistory.collectAsState()
    val selectedFilter by viewModel.selectedTimeFilter.collectAsState()
    val showWeightDialog by viewModel.showWeightDialog.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val filteredHistory = remember(selectedFilter, weightHistory) {
        viewModel.getFilteredWeightHistory()
    }

    val currentWeight = remember(filteredHistory) {
        viewModel.getCurrentWeight()
    }

    val startWeight = remember(filteredHistory) {
        viewModel.getStartWeight()
    }

    val daysFromStart = remember(filteredHistory) {
        viewModel.getDaysFromStart()
    }

    val weightChange = remember(currentWeight, startWeight) {
        currentWeight - startWeight
    }

    val weightChangeText = remember(weightChange) {
        if (weightChange > 0) "+%.1f кг".format(weightChange) else "%.1f кг".format(weightChange)
    }

    val weightChangeColor = remember(weightChange) {
        if (weightChange < 0) Color(0xFF2DD4BF) else if (weightChange > 0) Color(0xFFE55C5C) else Color(0xFF6B7280)
    }

    val latestDate = remember(filteredHistory) {
        if (filteredHistory.isNotEmpty()) {
            filteredHistory.last().date
        } else {
            System.currentTimeMillis()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ваш вес",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (currentWeight > 0) "%.1f кг".format(currentWeight) else "--",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatWeightDate(latestDate),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (filteredHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$daysFromStart дн.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        WeightFilterRow(
            selectedFilter = selectedFilter,
            onFilterSelected = { viewModel.setTimeFilter(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredHistory.isNotEmpty()) {
            WeightChartCard(
                weightHistory = filteredHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет данных за выбранный период",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeightProgressItem(
                label = "Начало",
                value = if (startWeight > 0) "%.1f кг".format(startWeight) else "--",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            WeightProgressItem(
                label = "Изменение",
                value = weightChangeText,
                color = weightChangeColor
            )

            WeightProgressItem(
                label = "Текущий",
                value = if (currentWeight > 0) "%.1f кг".format(currentWeight) else "--",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.setShowWeightDialog(true) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Изменить вес",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showWeightDialog) {
        WeightInputDialog(
            currentWeight = currentWeight,
            onDismiss = { viewModel.setShowWeightDialog(false) },
            onConfirm = { weight, date ->
                viewModel.saveWeight(weight, date)
                viewModel.setShowWeightDialog(false)
            }
        )
    }
}

@Composable
fun WeightFilterRow(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeFilter.values().forEach { filter ->
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

@Composable
fun WeightProgressItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
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

@Composable
fun WeightChartCard(
    weightHistory: List<com.example.fitness_plan.domain.model.WeightEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        WeightChart(
            weightHistory = weightHistory,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun WeightChart(
    weightHistory: List<com.example.fitness_plan.domain.model.WeightEntry>,
    modifier: Modifier = Modifier
) {
    if (weightHistory.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
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
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val weights = weightHistory.map { it.weight.toFloat() }
    val minWeight = weights.minOrNull() ?: 0f
    val maxWeight = weights.maxOrNull() ?: 0f
    val weightRange = maxWeight - minWeight

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "chart_animation"
    )

    Canvas(
        modifier = modifier.padding(16.dp)
    ) {
        val width = size.width
        val height = size.height

        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val stepX = if (weightHistory.size > 1) {
            chartWidth / (weightHistory.size - 1)
        } else {
            chartWidth / 2
        }

        val path = Path()

        weightHistory.forEachIndexed { index, entry ->
            val x = padding + index * stepX
            val normalizedWeight = if (weightRange > 0) {
                (entry.weight.toFloat() - minWeight) / weightRange
            } else {
                0.5f
            }
            val animatedY = (chartHeight * normalizedWeight * animatedProgress)
            val y = height - padding - animatedY

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

        weightHistory.forEachIndexed { index, entry ->
            val x = padding + index * stepX
            val normalizedWeight = if (weightRange > 0) {
                (entry.weight.toFloat() - minWeight) / weightRange
            } else {
                0.5f
            }
            val animatedY = (chartHeight * normalizedWeight * animatedProgress)
            val y = height - padding - animatedY

            drawCircle(
                color = surfaceColor,
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
                formatWeightDateShort(entry.date),
                x,
                height - 5.dp.toPx(),
                paint
            )

            val weightText = "%.1f".format(entry.weight)
            drawContext.canvas.nativeCanvas.drawText(
                weightText,
                x,
                padding - 10.dp.toPx(),
                paint
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightInputDialog(
    currentWeight: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Long) -> Unit
) {
    var weight by remember { mutableStateOf(if (currentWeight > 0) currentWeight.toString() else "") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val weightValue = weight.toDoubleOrNull()
    val isValid = weightValue != null && weightValue > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Изменить вес",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Вес (кг)") },
                    isError = weight.isNotEmpty() && !isValid,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedDate != null) formatWeightDate(selectedDate!!) else "Выберите дату",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValid) {
                        val date = selectedDate ?: System.currentTimeMillis()
                        onConfirm(weightValue!!, date)
                    }
                },
                enabled = isValid
            ) {
                Text(
                    text = "Сохранить",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { date ->
                            selectedDate = date
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun formatWeightDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("d MMMM", Locale("ru"))
    return sdf.format(Date(timestamp))
}

fun formatWeightDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("d MMM", Locale("ru"))
    return sdf.format(Date(timestamp))
}