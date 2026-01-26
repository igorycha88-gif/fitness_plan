package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitness_plan.presentation.viewmodel.StatisticsViewModel
import com.example.fitness_plan.presentation.viewmodel.TimeFilter
import com.example.fitness_plan.ui.charts.WeightChart
import com.example.fitness_plan.ui.charts.VolumeChart
import com.example.fitness_plan.ui.charts.FrequencyChart
import com.example.fitness_plan.ui.components.OverallStatsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val overallStats by viewModel.overallStats.collectAsStateWithLifecycle()
    val weightChartData by viewModel.weightChartData.collectAsStateWithLifecycle()
    val volumeChartData by viewModel.volumeChartData.collectAsStateWithLifecycle()
    val frequencyChartData by viewModel.frequencyChartData.collectAsStateWithLifecycle()
    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (overallStats != null) {
                OverallStatsCard(
                    stats = overallStats!!,
                    modifier = Modifier.padding(16.dp)
                )
            }

            TimeFilterSection(
                selectedFilter = selectedTimeFilter,
                onFilterSelected = { filter ->
                    viewModel.updateTimeFilter(filter)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            weightChartData?.let { chartData ->
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Динамика веса",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        WeightChart(
                            chartData = chartData,
                            modifier = Modifier.fillMaxWidth(),
                            primaryColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            volumeChartData?.let { chartData ->
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Объём тренировок",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        VolumeChart(
                            chartData = chartData,
                            modifier = Modifier.fillMaxWidth(),
                            secondaryColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            frequencyChartData?.let { chartData ->
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Частота тренировок",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        FrequencyChart(
                            chartData = chartData,
                            modifier = Modifier.fillMaxWidth(),
                            tertiaryColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimeFilterSection(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}