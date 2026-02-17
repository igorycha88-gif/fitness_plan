package com.example.fitness_plan.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.BodyParameter
import com.example.fitness_plan.domain.model.BodyParameterType
import com.example.fitness_plan.domain.repository.BodyParametersRepository
import com.example.fitness_plan.domain.usecase.BodyParametersUseCase
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

private const val TAG = "BodyParametersStatsViewModel"

@HiltViewModel
class BodyParametersStatsViewModel @Inject constructor(
    private val useCase: BodyParametersUseCase,
    private val credentialsRepository: ICredentialsRepository
) : ViewModel() {

    private val _currentUsername = MutableStateFlow("")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    private val _allMeasurements = MutableStateFlow<List<BodyParameter>>(emptyList())
    val allMeasurements: StateFlow<List<BodyParameter>> = _allMeasurements.asStateFlow()

    private val _selectedTypes = MutableStateFlow<Set<BodyParameterType>>(emptySet())
    val selectedTypes: StateFlow<Set<BodyParameterType>> = _selectedTypes.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.ALL)
    val selectedTimeFilter: StateFlow<TimeFilter> = _selectedTimeFilter.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.CHART)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _showParameterSelector = MutableStateFlow(false)
    val showParameterSelector: StateFlow<Boolean> = _showParameterSelector.asStateFlow()

    private val chartColors = listOf(
        androidx.compose.ui.graphics.Color(0xFF2DD4BF),
        androidx.compose.ui.graphics.Color(0xFFE07A5F),
        androidx.compose.ui.graphics.Color(0xFF6B4C9A),
        androidx.compose.ui.graphics.Color(0xFFF5B041),
        androidx.compose.ui.graphics.Color(0xFF5C9CE6)
    )

    val filteredChartSeries: StateFlow<List<ChartSeries>> = combine(
        allMeasurements,
        selectedTypes,
        selectedTimeFilter
    ) { measurements, types, timeFilter ->
        calculateFilteredChartSeries(measurements, types, timeFilter)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val availableForChart: StateFlow<List<ParameterOption>> = allMeasurements
        .map { measurements -> calculateAvailableParameters(measurements) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    init {
        viewModelScope.launch {
            val username = credentialsRepository.getUsername() ?: ""
            _currentUsername.value = username

            useCase.getMeasurements(username).collect { measurements ->
                _allMeasurements.value = measurements
            }
        }
    }

    fun setSelectedTypes(types: Set<BodyParameterType>) {
        _selectedTypes.value = types
    }

    fun setTimeFilter(filter: TimeFilter) {
        _selectedTimeFilter.value = filter
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun setShowParameterSelector(show: Boolean) {
        _showParameterSelector.value = show
    }

    fun getFilteredMeasurements(): List<BodyParameter> {
        val measurements = allMeasurements.value
        val types = _selectedTypes.value
        val timeFilter = _selectedTimeFilter.value

        val typesFiltered = if (types.isEmpty()) {
            measurements
        } else {
            measurements.filter { it.parameterType in types }
        }

        val timeFiltered = if (timeFilter.days == 0) {
            typesFiltered
        } else {
            val cutoffTime = System.currentTimeMillis() - (timeFilter.days * 24 * 60 * 60 * 1000L)
            typesFiltered.filter { it.date >= cutoffTime }
        }

        return timeFiltered.sortedBy { it.date }
    }

    fun getAvailableParameters(): List<ParameterOption> {
        return availableForChart.value
    }

    private fun calculateFilteredChartSeries(
        measurements: List<BodyParameter>,
        types: Set<BodyParameterType> = _selectedTypes.value,
        timeFilter: TimeFilter = _selectedTimeFilter.value
    ): List<ChartSeries> {
        if (types.isEmpty()) return emptyList()

        val sdf = SimpleDateFormat("d MMM", Locale("ru"))

        return types.mapIndexed { index, type ->
            val typeMeasurements = measurements.filter { it.parameterType == type }
            val timeFiltered = if (timeFilter.days == 0) {
                typeMeasurements
            } else {
                val cutoffTime = System.currentTimeMillis() - (timeFilter.days * 24 * 60 * 60 * 1000L)
                typeMeasurements.filter { it.date >= cutoffTime }
            }.sortedBy { it.date }

            val chartData = timeFiltered.map { measurement ->
                ChartDataPoint(
                    date = measurement.date,
                    value = measurement.value,
                    label = sdf.format(measurement.date)
                )
            }

            ChartSeries(
                parameterType = type,
                data = chartData,
                color = chartColors[index % chartColors.size],
                isVisible = true
            )
        }
    }

    private fun calculateAvailableParameters(measurements: List<BodyParameter>): List<ParameterOption> {
        return BodyParameterType.values().map { type ->
            val typeMeasurements = measurements.filter { it.parameterType == type }
            val last = typeMeasurements.maxByOrNull { it.date }

            ParameterOption(
                type = type,
                lastValue = last?.value,
                lastDate = last?.date,
                hasData = typeMeasurements.isNotEmpty()
            )
        }.filter { it.hasData }
    }

    fun toggleParameterVisibility(type: BodyParameterType) {
        val currentSeries = filteredChartSeries.value.toMutableList()
        val index = currentSeries.indexOfFirst { it.parameterType == type }
        if (index != -1) {
            currentSeries[index] = currentSeries[index].copy(isVisible = !currentSeries[index].isVisible)
        }
    }
}
