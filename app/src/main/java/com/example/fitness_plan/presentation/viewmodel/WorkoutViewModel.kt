package com.example.fitness_plan.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleHistoryEntry
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.usecase.CycleUseCase
import com.example.fitness_plan.domain.usecase.WorkoutUseCase
import com.example.fitness_plan.domain.usecase.WeightProgressionUseCase
import com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.calculator.WeightCalculator
import com.example.fitness_plan.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val applicationContext: android.content.Context,
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
    private val exerciseStatsRepository: ExerciseStatsRepository,
    private val cycleUseCase: CycleUseCase,
    private val workoutUseCase: WorkoutUseCase,
    private val weightCalculator: WeightCalculator,
    private val exerciseLibraryUseCase: ExerciseLibraryUseCase
) : ViewModel() {

    private val _currentWorkoutPlan = MutableStateFlow<WorkoutPlan?>(null)
    val currentWorkoutPlan: StateFlow<WorkoutPlan?> = _currentWorkoutPlan.asStateFlow()

    private val _adminWorkoutPlan = MutableStateFlow<WorkoutPlan?>(null)
    val adminWorkoutPlan: StateFlow<WorkoutPlan?> = _adminWorkoutPlan.asStateFlow()

    private val _completedExercises = MutableStateFlow<Set<String>>(emptySet())
    val completedExercises: StateFlow<Set<String>> = _completedExercises.asStateFlow()

    private val _completedDays = MutableStateFlow<Set<Int>>(emptySet())
    val completedDays: StateFlow<Set<Int>> = _completedDays.asStateFlow()

    private val _partiallyCompletedDays = MutableStateFlow<Set<Int>>(emptySet())
    val partiallyCompletedDays: StateFlow<Set<Int>> = _partiallyCompletedDays.asStateFlow()

    private val _currentCycle = MutableStateFlow<Cycle?>(null)
    val currentCycle: StateFlow<Cycle?> = _currentCycle.asStateFlow()

    private val _cycleHistory = MutableStateFlow<List<CycleHistoryEntry>>(emptyList())
    val cycleHistory: StateFlow<List<CycleHistoryEntry>> = _cycleHistory.asStateFlow()

    private val _exerciseStats = MutableStateFlow<List<ExerciseStats>>(emptyList())
    val exerciseStats: StateFlow<List<ExerciseStats>> = _exerciseStats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _alternativeExercises = MutableStateFlow<List<ExerciseLibrary>>(emptyList())
    val alternativeExercises: StateFlow<List<ExerciseLibrary>> = _alternativeExercises.asStateFlow()

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
        loadAdminWorkoutPlan()
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

    private fun loadAdminWorkoutPlan() {
        viewModelScope.launch {
            workoutUseCase.getAdminWorkoutPlan().collect { plan ->
                _adminWorkoutPlan.value = plan
            }
        }
    }

    fun refreshAdminWorkoutPlan() {
        loadAdminWorkoutPlan()
    }

    private fun updateCompletedDays(completedExercises: Set<String>) {
        val plan = _currentWorkoutPlan.value ?: return
        val completed = mutableSetOf<Int>()
        val partiallyCompleted = mutableSetOf<Int>()
        plan.days.forEachIndexed { dayIndex, day ->
            val dayExerciseNames = day.exercises.map { "${dayIndex}_${it.name}" }.toSet()
            val completedCount = dayExerciseNames.count { it in completedExercises }
            val totalCount = dayExerciseNames.size
            when {
                completedCount == 0 -> {}
                completedCount == totalCount -> completed.add(dayIndex)
                else -> partiallyCompleted.add(dayIndex)
            }
        }
        _completedDays.value = completed
        _partiallyCompletedDays.value = partiallyCompleted
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

            val allCompletedExercises = workoutUseCase.getCompletedExercises(username).first()
            updateCompletedDays(allCompletedExercises)

            cycleUseCase.updateProgress(username, newCompletedDays.size)

            val progressionSummary = cycleUseCase.checkAndApplyMicrocycleProgression(username, newCompletedDays.size)
            if (progressionSummary != null) {
                NotificationHelper.showWeightProgressionNotification(
                    applicationContext,
                    progressionSummary.totalIncreased,
                    progressionSummary.totalDecreased,
                    progressionSummary.totalUnchanged
                )
            }

            checkCycleCompletion(username, newCompletedDays.size)
        }
    }

    // Admin plan management
    fun createAdminPlan(name: String, description: String) {
        viewModelScope.launch {
            val newPlan = WorkoutPlan(
                id = "admin_plan",
                name = name,
                description = description,
                muscleGroups = listOf(),
                goal = "Admin",
                level = "Admin",
                days = emptyList()
            )
            workoutUseCase.saveAdminWorkoutPlan(newPlan)
            _adminWorkoutPlan.value = newPlan
        }
    }

    fun addDayToAdminPlan(dayName: String) {
        viewModelScope.launch {
            val currentPlan = _adminWorkoutPlan.value ?: return@launch
            val newDay = WorkoutDay(
                id = currentPlan.days.size,
                dayName = dayName,
                exercises = emptyList(),
                muscleGroups = listOf()
            )
            val updatedPlan = currentPlan.copy(days = currentPlan.days + newDay)
            workoutUseCase.saveAdminWorkoutPlan(updatedPlan)
            _adminWorkoutPlan.value = updatedPlan
        }
    }

    fun removeDayFromAdminPlan(dayIndex: Int) {
        viewModelScope.launch {
            val currentPlan = _adminWorkoutPlan.value ?: return@launch
            val updatedDays = currentPlan.days.toMutableList().apply { removeAt(dayIndex) }
            val updatedPlan = currentPlan.copy(days = updatedDays)
            workoutUseCase.saveAdminWorkoutPlan(updatedPlan)
            _adminWorkoutPlan.value = updatedPlan
        }
    }

    fun updateDayDate(dayIndex: Int, date: Long?) {
        viewModelScope.launch {
            val currentPlan = _adminWorkoutPlan.value ?: return@launch
            val updatedDays = currentPlan.days.toMutableList().apply {
                if (dayIndex in indices) {
                    this[dayIndex] = this[dayIndex].copy(scheduledDate = date)
                }
            }
            val updatedPlan = currentPlan.copy(days = updatedDays)
            workoutUseCase.saveAdminWorkoutPlan(updatedPlan)
            _adminWorkoutPlan.value = updatedPlan
        }
    }

    fun addExerciseToDay(dayIndex: Int, exercise: com.example.fitness_plan.domain.model.Exercise) {
        viewModelScope.launch {
            val currentPlan = _adminWorkoutPlan.value ?: return@launch
            val updatedDays = currentPlan.days.toMutableList().apply {
                if (dayIndex in indices) {
                    val day = this[dayIndex]
                    this[dayIndex] = day.copy(exercises = day.exercises + exercise)
                }
            }
            val updatedPlan = currentPlan.copy(days = updatedDays)
            workoutUseCase.saveAdminWorkoutPlan(updatedPlan)
            _adminWorkoutPlan.value = updatedPlan
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

    suspend fun getAdaptiveWeightForExercise(
        exerciseName: String,
        baseReps: List<Int>
    ): Float? {
        val username = _currentUsername.value
        if (username.isEmpty()) return null

        val exerciseHistory = exerciseStatsRepository.getLastNExerciseStats(
            username,
            exerciseName,
            2
        )

        return weightCalculator.calculateAdaptiveWeight(
            exerciseName,
            exerciseHistory,
            baseReps
        )
    }

    suspend fun loadAlternativeExercises(
        exerciseName: String,
        muscleGroups: List<com.example.fitness_plan.domain.model.MuscleGroup>,
        limit: Int = 3
    ) {
        val alternatives = exerciseLibraryUseCase.getAlternativeExercises(
            exerciseName,
            muscleGroups,
            limit
        )
        _alternativeExercises.value = alternatives
    }
}
