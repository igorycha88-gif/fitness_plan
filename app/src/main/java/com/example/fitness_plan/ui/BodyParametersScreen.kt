package com.example.fitness_plan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.presentation.viewmodel.BodyParametersStatsViewModel
import com.example.fitness_plan.presentation.viewmodel.TimeFilter
import com.example.fitness_plan.presentation.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyParametersScreen(
    viewModel: BodyParametersStatsViewModel = hiltViewModel()
) {
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsState()
    val selectedTypes by viewModel.selectedTypes.collectAsState()
    val filteredChartSeries by viewModel.filteredChartSeries.collectAsState()
    val availableParameters by viewModel.availableForChart.collectAsState()
    val showParameterSelector by viewModel.showParameterSelector.collectAsState()

    val hasSelectedParameters = selectedTypes.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика: Замеры") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TimeFilterRow(
                selectedFilter = selectedTimeFilter,
                onFilterSelected = { viewModel.setTimeFilter(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ViewModeToggle(
                viewMode = viewMode,
                onViewModeChanged = { viewModel.setViewMode(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SelectedParametersCard(
                selectedTypes = selectedTypes,
                availableParameters = availableParameters,
                filteredChartSeries = filteredChartSeries,
                onRemoveParameter = { type ->
                    val newTypes = selectedTypes - type
                    viewModel.setSelectedTypes(newTypes)
                },
                onToggleVisibility = { type ->
                    viewModel.toggleParameterVisibility(type)
                },
                onAddParameter = { viewModel.setShowParameterSelector(true) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (viewMode) {
                ViewMode.CHART -> {
                    if (hasSelectedParameters && filteredChartSeries.any { it.isVisible }) {
                        BodyParametersChartCard(
                            seriesList = filteredChartSeries.filter { it.isVisible },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    } else {
                        PlaceholderCard(
                            message = if (!hasSelectedParameters) {
                                "Выберите параметры для отображения графика"
                            } else {
                                "Нет видимых параметров для отображения"
                            }
                        )
                    }
                }
                ViewMode.TABLE -> {
                    if (hasSelectedParameters) {
                        BodyParametersTableCard(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        PlaceholderCard(
                            message = "Выберите параметры для отображения таблицы"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showParameterSelector) {
        ParameterSelectorDialog(
            availableParameters = availableParameters,
            selectedTypes = selectedTypes,
            onDismiss = { viewModel.setShowParameterSelector(false) },
            onConfirm = { types ->
                viewModel.setSelectedTypes(types)
                viewModel.setShowParameterSelector(false)
            }
        )
    }
}

@Composable
fun TimeFilterRow(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(TimeFilter.WEEK, TimeFilter.MONTH, TimeFilter.YEAR).forEach { filter ->
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
fun ViewModeToggle(
    viewMode: ViewMode,
    onViewModeChanged: (ViewMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = viewMode == ViewMode.CHART,
            onClick = { onViewModeChanged(ViewMode.CHART) },
            label = { Text("📊 График") },
            modifier = Modifier.weight(1f),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        FilterChip(
            selected = viewMode == ViewMode.TABLE,
            onClick = { onViewModeChanged(ViewMode.TABLE) },
            label = { Text("📋 Таблица") },
            modifier = Modifier.weight(1f),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

@Composable
fun SelectedParametersCard(
    selectedTypes: Set<com.example.fitness_plan.domain.model.BodyParameterType>,
    availableParameters: List<com.example.fitness_plan.presentation.viewmodel.ParameterOption>,
    filteredChartSeries: List<com.example.fitness_plan.presentation.viewmodel.ChartSeries>,
    onRemoveParameter: (com.example.fitness_plan.domain.model.BodyParameterType) -> Unit,
    onToggleVisibility: (com.example.fitness_plan.domain.model.BodyParameterType) -> Unit,
    onAddParameter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = "Выбранные параметры (${selectedTypes.size}/5)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTypes.isEmpty()) {
                Text(
                    text = "Нет выбранных параметров",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                selectedTypes.forEach { type ->
                    val option = availableParameters.find { it.type == type }
                    val series = filteredChartSeries.find { it.parameterType == type }
                    
                    SelectedParameterItem(
                        type = type,
                        lastValue = option?.lastValue,
                        lastDate = option?.lastDate,
                        unit = type.unit,
                        color = series?.color ?: MaterialTheme.colorScheme.primary,
                        isVisible = series?.isVisible ?: true,
                        onRemove = { onRemoveParameter(type) },
                        onToggleVisibility = { onToggleVisibility(type) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AssistChip(
                onClick = onAddParameter,
                label = { 
                    Text(
                        text = if (selectedTypes.size >= 5) {
                            "Максимум 5 параметров"
                        } else {
                            "+ Добавить параметр"
                        },
                        color = if (selectedTypes.size >= 5) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                },
                enabled = selectedTypes.size < 5,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = if (selectedTypes.size >= 5) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                },
                border = if (selectedTypes.size >= 5) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                },
                colors = AssistChipDefaults.assistChipColors(
                    leadingIconContentColor = if (selectedTypes.size >= 5) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    labelColor = if (selectedTypes.size >= 5) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            )
        }
    }
}

@Composable
fun SelectedParameterItem(
    type: com.example.fitness_plan.domain.model.BodyParameterType,
    lastValue: Double?,
    lastDate: Long?,
    unit: String,
    color: androidx.compose.ui.graphics.Color,
    isVisible: Boolean,
    onRemove: () -> Unit,
    onToggleVisibility: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = color,
                        radius = size.minDimension / 2
                    )
                }
            }

            Column {
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (lastValue != null) {
                        "%.1f $unit".format(lastValue)
                    } else {
                        "--"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (isVisible) {
                        Icons.Default.Check
                    } else {
                        Icons.Default.Close
                    },
                    contentDescription = if (isVisible) "Скрыть" else "Показать",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PlaceholderCard(
    message: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
