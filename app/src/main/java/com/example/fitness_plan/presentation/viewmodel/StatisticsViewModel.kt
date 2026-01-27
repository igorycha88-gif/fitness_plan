package com.example.fitness_plan.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleHistoryEntry
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.VolumeEntry
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

enum class VolumeTimeFilter(val days: Int, val label: String) {
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

    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.MONTH)
    val selectedTimeFilter: StateFlow<TimeFilter> = _selectedTimeFilter.asStateFlow()

    private val _showWeightDialog = MutableStateFlow(false)
    val showWeightDialog: StateFlow<Boolean> = _showWeightDialog.asStateFlow()

    private val _selectedVolumeFilter = MutableStateFlow(VolumeTimeFilter.MONTH)
    val selectedVolumeFilter: StateFlow<VolumeTimeFilter> = _selectedVolumeFilter.asStateFlow()

    private val _volumeData = MutableStateFlow<List<VolumeEntry>>(emptyList())
    val volumeData: StateFlow<List<VolumeEntry>> = _volumeData.asStateFlow()





    private val _availableExercises = MutableStateFlow<List<String>>(emptyList())
    val availableExercises: StateFlow<List<String>> = _availableExercises.asStateFlow()

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
        loadVolumeData()
    }

    fun setTimeFilter(filter: TimeFilter) {
        _selectedTimeFilter.value = filter
    }

    fun setShowWeightDialog(show: Boolean) {
        _showWeightDialog.value = show
    }

    fun setVolumeFilter(filter: VolumeTimeFilter) {
        _selectedVolumeFilter.value = filter
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
                _exerciseStats.value = stats.sortedBy { it.date }
                updateAvailableExercises(stats)
            }
        }
    }

    private fun updateAvailableExercises(stats: List<ExerciseStats>) {
        val exercises = stats.map { it.exerciseName }.distinct().sorted()
        _availableExercises.value = exercises
    }

    private fun loadVolumeData() {
        viewModelScope.launch {
            _exerciseStats.collect { stats ->
                updateVolumeData(stats)
            }
        }
    }

    private fun updateVolumeData(stats: List<ExerciseStats>) {
        val filter = _selectedVolumeFilter.value
        val cutoffTime = System.currentTimeMillis() - (filter.days.toLong() * 24 * 60 * 60 * 1000)

        val filteredStats = stats.filter { it.date >= cutoffTime }.sortedBy { it.date }
        val volumeEntries = mutableMapOf<Long, VolumeEntry>()

        for (stat in filteredStats) {
            val volume = stat.volume
            val existing = volumeEntries[stat.date]

            if (existing != null) {
                volumeEntries[stat.date] = existing.copy(
                    volume = existing.volume + volume,
                    exerciseCount = existing.exerciseCount + 1,
                    stats = existing.stats + stat
                )
            } else {
                volumeEntries[stat.date] = VolumeEntry(
                    date = stat.date,
                    volume = volume,
                    exerciseCount = 1,
                    stats = listOf(stat)
                )
            }
        }

        _volumeData.value = volumeEntries.values.toList().sortedBy { it.date }
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
        return if (filtered.isNotEmpty()) filtered.last().weight else 0.0
    }

    fun getStartWeight(): Double {
        val filtered = getFilteredWeightHistory()
        return if (filtered.isNotEmpty()) filtered.first().weight else 0.0
    }

    fun getDaysFromStart(): Int {
        val filtered = getFilteredWeightHistory()
        if (filtered.isEmpty()) return 0
        val firstEntry = filtered.first()
        val days = ((System.currentTimeMillis() - firstEntry.date) / (24 * 60 * 60 * 1000L)).toInt()
        return maxOf(1, days)
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

    fun getFilteredVolumeData(): List<VolumeEntry> {
        val filter = _selectedVolumeFilter.value
        val volumeEntries = _volumeData.value

        return when (filter) {
            VolumeTimeFilter.WEEK -> {
                volumeEntries.groupBy { getStartOfDay(it.date) }
                    .map { (day, entries) ->
                        VolumeEntry(
                            date = day,
                            volume = entries.sumOf { it.volume },
                            exerciseCount = entries.sumOf { it.exerciseCount },
                            stats = entries.flatMap { it.stats }
                        )
                    }
                    .sortedBy { it.date }
            }
            VolumeTimeFilter.MONTH, VolumeTimeFilter.YEAR -> {
                volumeEntries.groupBy { getStartOfWeek(it.date) }
                    .map { (week, entries) ->
                        VolumeEntry(
                            date = week,
                            volume = entries.sumOf { it.volume },
                            exerciseCount = entries.sumOf { it.exerciseCount },
                            stats = entries.flatMap { it.stats }
                        )
                    }
                    .sortedBy { it.date }
            }
        }
    }

    fun getTotalVolume(): Long {
        return getFilteredVolumeData().sumOf { it.volume }
    }

    fun getAverageVolume(): Long {
        val data = getFilteredVolumeData()
        return if (data.isNotEmpty()) data.sumOf { it.volume } / data.size else 0L
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfWeek(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
