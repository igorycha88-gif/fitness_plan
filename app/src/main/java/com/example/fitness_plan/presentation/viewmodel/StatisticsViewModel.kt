package com.example.fitness_plan.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleHistoryEntry
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WeightEntry
import com.example.fitness_plan.domain.repository.CredentialsRepository
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
    YEAR(365, "Год")
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val credentialsRepository: CredentialsRepository,
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

    private suspend fun loadWeightHistory() {
        weightRepository.getWeightHistory(_currentUsername.value).collect { entries ->
            _weightHistory.value = entries.sortedBy { it.date }
        }
    }

    private suspend fun loadExerciseStats() {
        exerciseStatsRepository.getExerciseStats(_currentUsername.value).collect { stats ->
            _exerciseStats.value = stats.sortedBy { it.date }
            updateAvailableExercises(stats)
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
            }
        }

        if (_currentUsername.value.isNotEmpty()) {
            cycleRepository.getCycleHistory(_currentUsername.value).collect { history ->
                _cycleHistory.value = history
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
        val exerciseName = _availableExercises.value.firstOrNull()?.name ?: ""

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
}
