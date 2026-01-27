package com.example.fitness_plan.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.presentation.viewmodel.StatisticsViewModel
import com.example.fitness_plan.presentation.viewmodel.VolumeTimeFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val volumeData by viewModel.volumeData.collectAsState()
    val selectedFilter by viewModel.selectedVolumeFilter.collectAsState()
    val availableExercises by viewModel.availableExercises.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    val filteredVolumeData = remember(selectedFilter, selectedExercise, volumeData) {
        viewModel.getFilteredVolumeData()
    }

    val totalVolume = remember(filteredVolumeData) {
        viewModel.getTotalVolume()
    }

    val averageVolume = remember(filteredVolumeData) {
        viewModel.getAverageVolume()
    }

    var selectedVolumeEntry by remember { mutableStateOf<com.example.fitness_plan.domain.model.VolumeEntry?>(null) }

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
            text = "Объём тренировок",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (availableExercises.isNotEmpty()) {
            ExerciseFilterDropdown(
                availableExercises = availableExercises,
                selectedExercise = selectedExercise,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onExerciseSelected = { exerciseName ->
                    viewModel.setSelectedExercise(exerciseName)
                    expanded = false
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

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
                    text = formatVolumeLarge(totalVolume),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "кг",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        VolumeFilterRow(
            selectedFilter = selectedFilter,
            onFilterSelected = { viewModel.setVolumeFilter(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredVolumeData.isNotEmpty()) {
            VolumeChartCard(
                volumeData = filteredVolumeData,
                selectedFilter = selectedFilter,
                onBarClick = { selectedVolumeEntry = it },
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
            VolumeProgressItem(
                label = "Всего",
                value = formatVolumeCompact(totalVolume),
                color = MaterialTheme.colorScheme.primary
            )

            VolumeProgressItem(
                label = "Среднее",
                value = formatVolumeCompact(averageVolume),
                color = MaterialTheme.colorScheme.tertiary
            )

            VolumeProgressItem(
                label = "Записей",
                value = filteredVolumeData.size.toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    selectedVolumeEntry?.let { entry ->
        VolumeDetailDialog(
            volumeEntry = entry,
            selectedFilter = selectedFilter,
            onDismiss = { selectedVolumeEntry = null }
        )
    }
}

@Composable
fun VolumeFilterRow(
    selectedFilter: VolumeTimeFilter,
    onFilterSelected: (VolumeTimeFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VolumeTimeFilter.values().forEach { filter ->
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
fun VolumeProgressItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
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

fun formatVolumeLarge(volume: Long): String {
    return if (volume >= 1000000) {
        "%.2fM".format(volume / 1000000.0)
    } else if (volume >= 1000) {
        "%.1fK".format(volume / 1000.0)
    } else {
        volume.toString()
    }
}

fun formatVolumeCompact(volume: Long): String {
    return if (volume >= 1000000) {
        "%.1fM".format(volume / 1000000.0)
    } else if (volume >= 1000) {
        "%.0fK".format(volume / 1000.0)
    } else {
        "$volume"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseFilterDropdown(
    availableExercises: List<String>,
    selectedExercise: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onExerciseSelected: (String?) -> Unit
) {
    Box {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = selectedExercise ?: "Все упражнения",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Все упражнения") },
                    onClick = { onExerciseSelected(null) },
                    leadingIcon = if (selectedExercise == null) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )

                availableExercises.forEach { exercise ->
                    DropdownMenuItem(
                        text = { Text(exercise) },
                        onClick = { onExerciseSelected(exercise) },
                        leadingIcon = if (selectedExercise == exercise) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
        }
    }
}
