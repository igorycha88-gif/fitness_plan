package com.example.fitness_plan.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleHistoryEntry
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.usecase.CycleUseCase
import com.example.fitness_plan.domain.usecase.WorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WorkoutViewModel"

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
    private val exerciseStatsRepository: ExerciseStatsRepository,
    private val cycleUseCase: CycleUseCase,
    private val workoutUseCase: WorkoutUseCase
) : ViewModel() {

    private val _currentWorkoutPlan = MutableStateFlow<WorkoutPlan?>(null)
    val currentWorkoutPlan: StateFlow<WorkoutPlan?> = _currentWorkoutPlan.asStateFlow()

    private val _completedExercises = MutableStateFlow<Set<String>>(emptySet())
    val completedExercises: StateFlow<Set<String>> = _completedExercises.asStateFlow()

    private val _completedDays = MutableStateFlow<Set<Int>>(emptySet())
    val completedDays: StateFlow<Set<Int>> = _completedDays.asStateFlow()

    private val _currentCycle = MutableStateFlow<Cycle?>(null)
    val currentCycle: StateFlow<Cycle?> = _currentCycle.asStateFlow()

    private val _cycleHistory = MutableStateFlow<List<CycleHistoryEntry>>(emptyList())
    val cycleHistory: StateFlow<List<CycleHistoryEntry>> = _cycleHistory.asStateFlow()

    private val _exerciseStats = MutableStateFlow<List<ExerciseStats>>(emptyList())
    val exerciseStats: StateFlow<List<ExerciseStats>> = _exerciseStats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _currentUsername = MutableStateFlow("")

    fun initializeWorkout() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "initializeWorkout started")

            var attempts = 0
            var profile: UserProfile? = null

            while (profile == null && attempts < 10) {
                profile = userRepository.getUserProfile().first()
                Log.d(TAG, "Attempt $attempts: profile = ${profile?.username}, goal=${profile?.goal}, level=${profile?.level}")
                if (profile == null) {
                    delay(200)
                    attempts++
                }
            }

            if (profile != null) {
                Log.d(TAG, "Profile found: ${profile.username}, goal=${profile.goal}, level=${profile.level}")
                loadWorkoutData(profile)
            } else {
                Log.e(TAG, "Profile not found after 10 attempts")
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadWorkoutData(profile: UserProfile) {
        val username = profile.username
        _currentUsername.value = username
        Log.d(TAG, "loadWorkoutData: username=$username")

        try {
            val cycleState = cycleUseCase.initializeCycleForUser(username, profile)
            Log.d(TAG, "Cycle state: cycle=${cycleState.cycle?.cycleNumber}, workoutPlan=${cycleState.workoutPlan?.name}, historySize=${cycleState.history.size}")

            _currentCycle.value = cycleState.cycle
            _currentWorkoutPlan.value = cycleState.workoutPlan
            _cycleHistory.value = cycleState.history

            Log.d(TAG, "Workout plan set: ${_currentWorkoutPlan.value?.name}, days=${_currentWorkoutPlan.value?.days?.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading workout data", e)
        }

        _isLoading.value = false

        loadCompletedExercises(username)
        loadExerciseStats(username)
    }

    private fun loadCompletedExercises(username: String) {
        viewModelScope.launch {
            workoutUseCase.getCompletedExercises(username).collect { completed ->
                _completedExercises.value = completed
                updateCompletedDays(completed)
            }
        }
    }

    private fun loadExerciseStats(username: String) {
        viewModelScope.launch {
            workoutUseCase.getExerciseStats(username).collect { stats ->
                _exerciseStats.value = stats
            }
        }
    }

    private fun updateCompletedDays(completedExercises: Set<String>) {
        val plan = _currentWorkoutPlan.value ?: return
        val completed = mutableSetOf<Int>()
        plan.days.forEachIndexed { dayIndex, day ->
            val dayExerciseNames = day.exercises.map { "${dayIndex}_${it.name}" }.toSet()
            val hasCompleted = dayExerciseNames.any { it in completedExercises }
            if (hasCompleted) {
                completed.add(dayIndex)
            }
        }
        _completedDays.value = completed
    }

    fun toggleExerciseCompletion(exerciseKey: String, completed: Boolean) {
        viewModelScope.launch {
            val username = _currentUsername.value
            if (username.isEmpty()) return@launch

            val newCompletedDays = workoutUseCase.toggleExerciseCompletion(
                username,
                exerciseKey,
                completed,
                _currentWorkoutPlan.value
            )
            _completedDays.value = newCompletedDays

            cycleUseCase.updateProgress(username, newCompletedDays.size)

            checkCycleCompletion(username, newCompletedDays.size)
        }
    }

    private fun checkCycleCompletion(username: String, completedDaysCount: Int) {
        viewModelScope.launch {
            val cycle = _currentCycle.value ?: return@launch

            if (completedDaysCount >= Cycle.DAYS_IN_CYCLE) {
                cycleRepository.markCycleCompleted(username, System.currentTimeMillis())
                _currentCycle.value = cycle.copy(completedDate = System.currentTimeMillis())
            }
        }
    }

    fun updateWorkoutDayDate(dayIndex: Int, newDate: Long?) {
        viewModelScope.launch {
            val currentPlan = _currentWorkoutPlan.value ?: return@launch
            val username = _currentUsername.value
            if (username.isEmpty()) return@launch

            val updatedDays = currentPlan.days.mapIndexed { index, day ->
                if (index == dayIndex) {
                    day.copy(scheduledDate = newDate)
                } else {
                    day
                }
            }

            val updatedPlan = currentPlan.copy(days = updatedDays)
            _currentWorkoutPlan.value = updatedPlan

            val dates = updatedDays.mapNotNull { it.scheduledDate }
            workoutUseCase.updateWorkoutSchedule(username, dates)
        }
    }

    fun saveExerciseStats(
        exerciseName: String,
        weight: Double,
        reps: Int,
        setNumber: Int,
        sets: Int
    ) {
        viewModelScope.launch {
            val username = _currentUsername.value
            if (username.isNotEmpty()) {
                workoutUseCase.saveExerciseStats(
                    username,
                    exerciseName,
                    weight,
                    reps,
                    setNumber,
                    sets
                )
            }
        }
    }

    fun setCurrentUsername(username: String) {
        _currentUsername.value = username
        initializeWorkout()
    }
}
