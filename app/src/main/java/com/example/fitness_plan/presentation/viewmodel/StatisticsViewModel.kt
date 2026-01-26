package com.example.fitness_plan.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleHistoryEntry
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WeightEntry
import com.example.fitness_plan.domain.model.WeightChartData
import com.example.fitness_plan.domain.model.VolumeChartData
import com.example.fitness_plan.domain.model.FrequencyChartData
import com.example.fitness_plan.domain.model.OverallStats
import com.example.fitness_plan.domain.model.WeightDataPoint
import com.example.fitness_plan.domain.model.VolumeDataPoint
import com.example.fitness_plan.domain.model.FrequencyDataPoint
import com.example.fitness_plan.domain.model.WeightStats
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "StatisticsViewModel"

enum class TimeFilter(val days: Int, val label: String) {
    DAYS_10(10, "10 дней"),
    WEEK(7, "Неделя"),
    MONTH(30, "Месяц"),
    YEAR(365, "Год")
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val credentialsRepository: ICredentialsRepository,
    private val cycleRepository: CycleRepository,
    private val weightRepository: WeightRepository,
    private val exerciseStatsRepository: ExerciseStatsRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = userRepository.getUserProfile()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    private val _weightHistory = MutableStateFlow<List<WeightEntry>>(emptyList())
    val weightHistory: StateFlow<List<WeightEntry>> = _weightHistory.asStateFlow()

    private val _exerciseStats = MutableStateFlow<List<ExerciseStats>>(emptyList())
    val exerciseStats: StateFlow<List<ExerciseStats>> = _exerciseStats.asStateFlow()

    private val _currentCycle = MutableStateFlow<Cycle?>(null)
    val currentCycle: StateFlow<Cycle?> = _currentCycle.asStateFlow()

    private val _cycleHistory = MutableStateFlow<List<CycleHistoryEntry>>(emptyList())
    val cycleHistory: StateFlow<List<CycleHistoryEntry>> = _cycleHistory.asStateFlow()

    private val _currentUsername = MutableStateFlow("")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()





    private val _availableExercises = MutableStateFlow<List<String>>(emptyList())
    val availableExercises: StateFlow<List<String>> = _availableExercises.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.DAYS_10)
    val selectedTimeFilter: StateFlow<TimeFilter> = _selectedTimeFilter.asStateFlow()

    private val _weightChartData = MutableStateFlow<WeightChartData?>(null)
    val weightChartData: StateFlow<WeightChartData?> = _weightChartData.asStateFlow()

    private val _volumeChartData = MutableStateFlow<VolumeChartData?>(null)
    val volumeChartData: StateFlow<VolumeChartData?> = _volumeChartData.asStateFlow()

    private val _frequencyChartData = MutableStateFlow<FrequencyChartData?>(null)
    val frequencyChartData: StateFlow<FrequencyChartData?> = _frequencyChartData.asStateFlow()

    private val _overallStats = MutableStateFlow<OverallStats?>(null)
    val overallStats: StateFlow<OverallStats?> = _overallStats.asStateFlow()

    init {
        viewModelScope.launch {
            val username = credentialsRepository.getUsername() ?: ""
            _currentUsername.value = username
            loadData()
        }
    }

    private suspend fun loadData() {
        loadWeightHistory()
        loadExerciseStats()
        loadCycleData()
        calculateAllChartData()
    }

    private suspend fun loadWeightHistory() {
        weightRepository.getWeightHistory(_currentUsername.value).collect { entries ->
            _weightHistory.value = entries.sortedBy { it.date }
            calculateAllChartData()
        }
    }

    private suspend fun loadExerciseStats() {
        exerciseStatsRepository.getExerciseStats(_currentUsername.value).collect { stats ->
            _exerciseStats.value = stats.sortedBy { it.date }
            updateAvailableExercises(stats)
            calculateAllChartData()
        }
    }

    private fun updateAvailableExercises(stats: List<ExerciseStats>) {
        val exercises = stats.map { it.exerciseName }.distinct().sorted()
        _availableExercises.value = exercises
    }

    private suspend fun loadCycleData() {
        val profile = userRepository.getUserProfile().first()
        if (profile != null) {
            cycleRepository.getCurrentCycle(profile.username).collect { cycle ->
                _currentCycle.value = cycle
                calculateAllChartData()
            }
        }

        if (_currentUsername.value.isNotEmpty()) {
            cycleRepository.getCycleHistory(_currentUsername.value).collect { history ->
                _cycleHistory.value = history
                calculateAllChartData()
            }
        }
    }





    fun getFilteredWeightHistory(): List<WeightEntry> {
        val days = TimeFilter.MONTH.days // Default to month
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return _weightHistory.value.filter { it.date >= cutoffTime }
    }

    fun getFilteredExerciseStats(): List<ExerciseStats> {
        val days = TimeFilter.MONTH.days // Default to month
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val exerciseName = _availableExercises.value.firstOrNull() ?: ""

        return _exerciseStats.value
            .filter { it.date >= cutoffTime && it.exerciseName == exerciseName }
            .sortedBy { it.date }
    }

    fun getCycleProgress(): Float {
        return _currentCycle.value?.progress ?: 0f
    }

    fun getCycleProgressText(): String {
        val cycle = _currentCycle.value ?: return "0/30"
        return "${cycle.daysCompleted}/${cycle.totalDays}"
    }

    fun getWeightChange(): Double {
        val history = getFilteredWeightHistory()
        if (history.size < 2) return 0.0
        val first = history.first().weight
        val last = history.last().weight
        return last - first
    }

    fun getWeightChangeText(): String {
        val change = getWeightChange()
        return if (change > 0) "+%.1f кг".format(change) else "%.1f кг".format(change)
    }

    fun getStrengthProgress(): Double {
        val stats = getFilteredExerciseStats()
        if (stats.size < 2) return 0.0

        val firstMaxWeight = stats.first().weight
        val lastMaxWeight = stats.last().weight

        return if (firstMaxWeight > 0) {
            ((lastMaxWeight - firstMaxWeight) / firstMaxWeight) * 100
        } else 0.0
    }

    fun getStrengthProgressText(): String {
        val progress = getStrengthProgress()
        return if (progress > 0) "+%.1f%%".format(progress) else "%.1f%%".format(progress)
    }

    fun updateTimeFilter(filter: TimeFilter) {
        _selectedTimeFilter.value = filter
        viewModelScope.launch {
            calculateAllChartData()
        }
    }

    private fun calculateAllChartData() {
        _weightChartData.value = calculateWeightChartData()
        _volumeChartData.value = calculateVolumeChartData()
        _frequencyChartData.value = calculateFrequencyChartData()
        _overallStats.value = calculateOverallStats()
    }

    private fun calculateWeightChartData(): WeightChartData? {
        val days = _selectedTimeFilter.value.days
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)

        val filteredEntries = _weightHistory.value
            .filter { it.date >= cutoffTime }
            .sortedBy { it.date }

        if (filteredEntries.isEmpty()) {
            return WeightChartData(emptyList(), WeightStats(0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
        }

        val dataPoints = filteredEntries.map { WeightDataPoint(it.date, it.weight) }
        val overallStats = WeightStats(
            startWeight = filteredEntries.first().weight,
            currentWeight = filteredEntries.last().weight,
            minWeight = filteredEntries.minOf { it.weight },
            maxWeight = filteredEntries.maxOf { it.weight },
            averageWeight = filteredEntries.map { it.weight }.average(),
            totalChange = filteredEntries.last().weight - filteredEntries.first().weight
        )

        return WeightChartData(dataPoints, overallStats)
    }

    private fun calculateVolumeChartData(): VolumeChartData? {
        val days = _selectedTimeFilter.value.days
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)

        val filteredStats = _exerciseStats.value
            .filter { it.date >= cutoffTime }
            .sortedBy { it.date }

        if (filteredStats.isEmpty()) {
            return VolumeChartData(emptyList(), 0)
        }

        val groupedByDate = filteredStats
            .groupBy { it.date }
            .map { (date, stats) ->
                val totalVolume = stats.sumOf { it.volume }
                val workoutsCount = stats.size
                VolumeDataPoint(date, totalVolume, workoutsCount)
            }
            .sortedBy { it.date }

        val totalVolume = groupedByDate.sumOf { it.volume }

        return VolumeChartData(groupedByDate, totalVolume)
    }

    private fun calculateFrequencyChartData(): FrequencyChartData? {
        val days = _selectedTimeFilter.value.days
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)

        val filteredStats = _exerciseStats.value
            .filter { it.date >= cutoffTime }
            .sortedBy { it.date }

        if (filteredStats.isEmpty()) {
            return FrequencyChartData(emptyList(), 0)
        }

        val groupedByDate = filteredStats
            .groupBy { it.date }
            .map { (date, stats) ->
                FrequencyDataPoint(date, stats.size)
            }
            .sortedBy { it.date }

        val totalWorkouts = groupedByDate.size

        return FrequencyChartData(groupedByDate, totalWorkouts)
    }

    private fun calculateOverallStats(): OverallStats? {
        val weightChartData = _weightChartData.value
        val volumeChartData = _volumeChartData.value
        val cycle = _currentCycle.value
        val userProfile = userProfile.value

        if (weightChartData == null || volumeChartData == null || userProfile == null) {
            return null
        }

        val currentWeight = if (weightChartData.dataPoints.isNotEmpty()) {
            weightChartData.dataPoints.last().weight
        } else {
            userProfile.weight
        }

        val weightChange = weightChartData.overallStats.totalChange
        val weightChangePercentage = weightChartData.overallStats.changePercentage

        val totalVolume = volumeChartData.totalVolume
        val totalWorkouts = volumeChartData.dataPoints.size

        val cycleProgress = cycle?.progress ?: 0f
        val cycleDaysCompleted = cycle?.daysCompleted ?: 0
        val cycleTotalDays = cycle?.totalDays ?: 30

        val periodDays = _selectedTimeFilter.value.days

        return OverallStats(
            currentWeight = currentWeight,
            weightChange = weightChange,
            weightChangePercentage = weightChangePercentage,
            totalVolume = totalVolume,
            totalWorkouts = totalWorkouts,
            cycleProgress = cycleProgress,
            cycleDaysCompleted = cycleDaysCompleted,
            cycleTotalDays = cycleTotalDays,
            periodDays = periodDays
        )
    }
}
