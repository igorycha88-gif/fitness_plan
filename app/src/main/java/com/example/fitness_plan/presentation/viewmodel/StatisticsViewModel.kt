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
import kotlinx.coroutines.flow.combine
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
    DAY(1, "День"),
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

    private val _selectedVolumeFilter = MutableStateFlow(VolumeTimeFilter.WEEK)
    val selectedVolumeFilter: StateFlow<VolumeTimeFilter> = _selectedVolumeFilter.asStateFlow()

    private val _volumeData = MutableStateFlow<List<ExerciseStats>>(emptyList())
    val volumeData: StateFlow<List<ExerciseStats>> = _volumeData.asStateFlow()





    private val _availableExercises = MutableStateFlow<List<String>>(emptyList())
    val availableExercises: StateFlow<List<String>> = _availableExercises.asStateFlow()

    private val _selectedExercise = MutableStateFlow<String?>(null)
    val selectedExercise: StateFlow<String?> = _selectedExercise.asStateFlow()

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

    fun setSelectedExercise(exerciseName: String?) {
        _selectedExercise.value = exerciseName
    }

    fun getSelectedExercise(): String? {
        return _selectedExercise.value
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
            combine(
                _exerciseStats,
                _selectedExercise,
                _selectedVolumeFilter
            ) { stats, selectedExercise, filter ->
                Triple(stats, selectedExercise, filter)
            }.collect { (stats, selectedExercise, filter) ->
                updateVolumeData(stats, selectedExercise, filter)
            }
        }
    }

    private fun updateVolumeData(
        stats: List<ExerciseStats>,
        selectedExercise: String?,
        filter: VolumeTimeFilter
    ) {
        val cutoffTime = if (filter.days > 0) {
            System.currentTimeMillis() - (filter.days.toLong() * 24 * 60 * 60 * 1000)
        } else {
            0L
        }
        Log.d(TAG, "updateVolumeData: filter=${filter.label}, days=${filter.days}, selectedExercise=$selectedExercise, totalStats=${stats.size}")

        val beforeFilterCount = stats.size
        val filteredStats = stats
            .filter { it.date >= cutoffTime }
            .filter { selectedExercise == null || it.exerciseName == selectedExercise }
            .sortedBy { it.date }

        Log.d(TAG, "updateVolumeData: filtered from $beforeFilterCount to ${filteredStats.size} records")
        if (filteredStats.isNotEmpty()) {
            val totalVolume = filteredStats.sumOf { it.volume }
            val uniqueExercises = filteredStats.map { it.exerciseName }.distinct()
            Log.d(TAG, "updateVolumeData: totalVolume=$totalVolume, uniqueExercises=${uniqueExercises.size}, exercises=${uniqueExercises}")
            val oldestDate = filteredStats.first().date
            val newestDate = filteredStats.last().date
            val dateRange = (newestDate - oldestDate) / (24 * 60 * 60 * 1000L)
            Log.d(TAG, "updateVolumeData: dateRange=$dateRange days")
        }
        _volumeData.value = filteredStats
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

    fun getVolumeDataSummary(): VolumeDataSummary {
        val volumeData = _volumeData.value
        val filter = _selectedVolumeFilter.value
        val selectedExercise = _selectedExercise.value

        return if (volumeData.isEmpty()) {
            val allStats = _exerciseStats.value
            VolumeDataSummary(
                totalCount = 0,
                filterLabel = filter.label,
                selectedExercise = selectedExercise,
                reason = when {
                    allStats.isEmpty() -> "Нет выполненных упражнений"
                    selectedExercise != null && allStats.none { it.exerciseName == selectedExercise } -> "Нет данных для выбранного упражнения"
                    filter != VolumeTimeFilter.ALL && allStats.isNotEmpty() -> {
                        val cutoffTime = System.currentTimeMillis() - (filter.days.toLong() * 24 * 60 * 60 * 1000)
                        val filteredByDate = allStats.filter { it.date >= cutoffTime }
                        val filteredByExercise = if (selectedExercise != null) {
                            filteredByDate.filter { it.exerciseName == selectedExercise }
                        } else {
                            filteredByDate
                        }
                        "Данные отфильтрованы: ${allStats.size} → ${filteredByExercise.size}"
                    }
                    else -> "Нет данных за выбранный период"
                }
            )
        } else {
            VolumeDataSummary(
                totalCount = volumeData.size,
                filterLabel = filter.label,
                selectedExercise = selectedExercise,
                reason = "OK"
            )
        }
    }

    fun getFilteredVolumeData(): List<VolumeEntry> {
        val filter = _selectedVolumeFilter.value
        val exerciseStats = _volumeData.value

        return when (filter) {
            VolumeTimeFilter.DAY -> {
                exerciseStats.groupBy { getStartOfHour(it.date) }
                    .map { (hour, stats) ->
                        VolumeEntry(
                            date = hour,
                            volume = stats.sumOf { it.volume },
                            exerciseCount = stats.size,
                            stats = stats
                        )
                    }
                    .sortedBy { it.date }
            }
            VolumeTimeFilter.WEEK, VolumeTimeFilter.MONTH -> {
                exerciseStats.groupBy { getStartOfDay(it.date) }
                    .map { (day, stats) ->
                        VolumeEntry(
                            date = day,
                            volume = stats.sumOf { it.volume },
                            exerciseCount = stats.size,
                            stats = stats
                        )
                    }
                    .sortedBy { it.date }
            }
            VolumeTimeFilter.YEAR -> {
                exerciseStats.groupBy { getStartOfWeek(it.date) }
                    .map { (week, stats) ->
                        VolumeEntry(
                            date = week,
                            volume = stats.sumOf { it.volume },
                            exerciseCount = stats.size,
                            stats = stats
                        )
                    }
                    .sortedBy { it.date }
            }
            VolumeTimeFilter.ALL -> {
                exerciseStats.groupBy { getStartOfDay(it.date) }
                    .map { (day, stats) ->
                        VolumeEntry(
                            date = day,
                            volume = stats.sumOf { it.volume },
                            exerciseCount = stats.size,
                            stats = stats
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

    private fun getStartOfHour(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
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

data class ExerciseStatsSummary(
    val totalCount: Int,
    val uniqueCount: Int,
    val exerciseNames: List<String>,
    val sampleDates: List<Long>,
    val totalVolume: Long,
    val dateRange: String
)

data class VolumeDataSummary(
    val totalCount: Int,
    val filterLabel: String,
    val selectedExercise: String?,
    val reason: String
)
