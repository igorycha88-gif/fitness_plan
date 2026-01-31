package com.example.fitness_plan.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleHistoryEntry
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.ProgressTimeFilter
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WeightEntry
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "StatisticsViewModel"

enum class TimeFilter(val days: Int, val label: String) {
    WEEK(7, "Неделя"),
    MONTH(30, "Месяц"),
    YEAR(365, "Год"),
    ALL(0, "Всё время")
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

    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.MONTH)
    val selectedTimeFilter: StateFlow<TimeFilter> = _selectedTimeFilter.asStateFlow()

    private val _showWeightDialog = MutableStateFlow(false)
    val showWeightDialog: StateFlow<Boolean> = _showWeightDialog.asStateFlow()

    private val _selectedProgressTimeFilter = MutableStateFlow(ProgressTimeFilter.MONTH)
    val selectedProgressTimeFilter: StateFlow<ProgressTimeFilter> = _selectedProgressTimeFilter.asStateFlow()

    private val _selectedProgressExercise = MutableStateFlow<String?>(null)
    val selectedProgressExercise: StateFlow<String?> = _selectedProgressExercise.asStateFlow()

    private val _progressChartData = MutableStateFlow<List<com.example.fitness_plan.domain.model.ProgressChartData>>(emptyList())
    val progressChartData: StateFlow<List<com.example.fitness_plan.domain.model.ProgressChartData>> = _progressChartData.asStateFlow()

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
    }

    fun setTimeFilter(filter: TimeFilter) {
        _selectedTimeFilter.value = filter
    }

    fun setShowWeightDialog(show: Boolean) {
        _showWeightDialog.value = show
    }

    fun setProgressTimeFilter(filter: ProgressTimeFilter) {
        _selectedProgressTimeFilter.value = filter
        updateProgressChartData()
    }

    fun setProgressExercise(exerciseName: String?) {
        _selectedProgressExercise.value = exerciseName
        updateProgressChartData()
    }

    fun saveWeight(weight: Double, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val username = _currentUsername.value
            if (username.isNotEmpty()) {
                weightRepository.saveWeight(username, weight, date)
            }
        }
    }

    private fun loadWeightHistory() {
        viewModelScope.launch {
            weightRepository.getWeightHistory(_currentUsername.value).collect { entries ->
                _weightHistory.value = entries.sortedBy { it.date }
            }
        }
    }

    private fun loadExerciseStats() {
        viewModelScope.launch {
            exerciseStatsRepository.getExerciseStats(_currentUsername.value).collect { stats ->
                Log.d(TAG, "Loaded ${stats.size} exercise stats for user ${_currentUsername.value}")
                val withVolume = stats.count { it.volume > 0L }
                val withoutVolume = stats.count { it.volume == 0L }
                Log.d(TAG, "Stats breakdown: $withVolume with volume > 0, $withoutVolume with volume = 0")
                _exerciseStats.value = stats.sortedBy { it.date }
            }
        }
    }

    private suspend fun loadCycleData() {
        val profile = userRepository.getUserProfile().first()
        if (profile != null) {
            cycleRepository.getCurrentCycle(profile.username).collect { cycle ->
                _currentCycle.value = cycle
            }
        }

        if (_currentUsername.value.isNotEmpty()) {
            cycleRepository.getCycleHistory(_currentUsername.value).collect { history ->
                _cycleHistory.value = history
            }
        }
    }





    fun getFilteredWeightHistory(): List<WeightEntry> {
        val days = _selectedTimeFilter.value.days
        if (days == 0) {
            return _weightHistory.value.sortedBy { it.date }
        }
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return _weightHistory.value.filter { it.date >= cutoffTime }.sortedBy { it.date }
    }

    fun getCurrentWeight(): Double {
        val filtered = getFilteredWeightHistory()
        return if (filtered.isNotEmpty()) filtered.last().weight else userProfile.value?.weight ?: 0.0
    }

    fun getStartWeight(): Double {
        val filtered = getFilteredWeightHistory()
        return if (filtered.isNotEmpty()) filtered.first().weight else userProfile.value?.weight ?: 0.0
    }

    fun getDaysFromStart(): Int {
        val filtered = getFilteredWeightHistory()
        if (filtered.isEmpty()) return 0
        val firstEntry = filtered.first()
        val days = ((System.currentTimeMillis() - firstEntry.date) / (24 * 60 * 60 * 1000L)).toInt()
        return maxOf(1, days)
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

    fun getExerciseStatsSummary(): ExerciseStatsSummary {
        val stats = _exerciseStats.value
        return if (stats.isEmpty()) {
            ExerciseStatsSummary(0, 0, emptyList(), emptyList(), 0L, "Нет данных")
        } else {
            val uniqueExercises = stats.map { it.exerciseName }.distinct()
            val oldestDate = stats.minOfOrNull { it.date } ?: 0L
            val newestDate = stats.maxOfOrNull { it.date } ?: 0L
            val totalVolume = stats.sumOf { it.volume }
            val dateRangeDays = if (oldestDate > 0 && newestDate > 0) {
                (newestDate - oldestDate) / (24 * 60 * 60 * 1000L)
            } else {
                0L
            }
            ExerciseStatsSummary(
                totalCount = stats.size,
                uniqueCount = uniqueExercises.size,
                exerciseNames = uniqueExercises,
                sampleDates = stats.take(5).map { it.date },
                totalVolume = totalVolume,
                dateRange = "$dateRangeDays дней"
            )
        }
    }

    fun getAvailableExercises(): List<String> {
        return _exerciseStats.value.map { it.exerciseName }.distinct().sorted()
    }

    private fun updateProgressChartData() {
        viewModelScope.launch {
            val exerciseName = _selectedProgressExercise.value
            val days = _selectedProgressTimeFilter.value.days

            if (exerciseName == null) {
                _progressChartData.value = emptyList()
                return@launch
            }

            val stats = if (days == 0) {
                _exerciseStats.value
            } else {
                val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
                _exerciseStats.value.filter { it.date >= cutoffTime }
            }.filter { it.exerciseName == exerciseName }

            if (stats.isEmpty()) {
                _progressChartData.value = emptyList()
                return@launch
            }

            val dailyStats = aggregateByDay(stats)
            val chartData = dailyStats.map { daily ->
                com.example.fitness_plan.domain.model.ProgressChartData(
                    date = daily.date,
                    xValue = if (daily.exerciseType == com.example.fitness_plan.domain.model.ExerciseType.STRENGTH) {
                        daily.averageWeight
                    } else {
                        daily.duration.toDouble()
                    },
                    xLabel = if (daily.exerciseType == com.example.fitness_plan.domain.model.ExerciseType.STRENGTH) {
                        "%.1f кг".format(daily.averageWeight)
                    } else {
                        "%d мин".format(daily.duration)
                    },
                    yValue = if (daily.exerciseType == com.example.fitness_plan.domain.model.ExerciseType.STRENGTH) {
                        daily.averageReps.toDouble()
                    } else {
                        daily.duration.toDouble()
                    },
                    exerciseName = daily.exerciseName
                )
            }.sortedBy { it.date }

            _progressChartData.value = chartData
        }
    }

    private fun aggregateByDay(stats: List<ExerciseStats>): List<com.example.fitness_plan.domain.model.DailyExerciseStats> {
        return stats.groupBy { stat ->
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = stat.date
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.map { (date, dayStats) ->
            val exerciseType = determineExerciseType(dayStats.first().exerciseName)
            com.example.fitness_plan.domain.model.DailyExerciseStats(
                exerciseName = dayStats.first().exerciseName,
                date = date,
                averageWeight = if (exerciseType == com.example.fitness_plan.domain.model.ExerciseType.STRENGTH) {
                    dayStats.filter { it.weight > 0 }.map { it.weight }.average()
                } else {
                    0.0
                },
                averageReps = if (exerciseType == com.example.fitness_plan.domain.model.ExerciseType.STRENGTH) {
                    dayStats.filter { it.reps > 0 }.map { it.reps }.average().toInt()
                } else {
                    0
                },
                totalSets = dayStats.size,
                exerciseType = exerciseType,
                duration = dayStats.sumOf { it.duration }
            )
        }.sortedBy { it.date }
    }

    private fun determineExerciseType(exerciseName: String): com.example.fitness_plan.domain.model.ExerciseType {
        return com.example.fitness_plan.domain.model.ExerciseType.STRENGTH
    }
}

data class ExerciseStatsSummary(
    val totalCount: Int,
    val uniqueCount: Int,
    val exerciseNames: List<String>,
    val sampleDates: List<Long>,
    val totalVolume: Long,
    val dateRange: String
)
