package com.example.fitness_plan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.data.CredentialsRepository
import com.example.fitness_plan.data.ExerciseCompletionRepository
import com.example.fitness_plan.data.ExerciseStats
import com.example.fitness_plan.data.UserCredentials
import com.example.fitness_plan.data.UserProfile
import com.example.fitness_plan.data.UserRepository
import com.example.fitness_plan.data.WeightEntry
import com.example.fitness_plan.data.WeightRepository
import com.example.fitness_plan.data.WorkoutPlan
import com.example.fitness_plan.data.WorkoutPlanRepository
import com.example.fitness_plan.data.ExerciseStatsRepository
import com.example.fitness_plan.data.WorkoutScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val weightRepository: WeightRepository,
    private val exerciseStatsRepository: ExerciseStatsRepository,
    private val credentialsRepository: CredentialsRepository,
    private val exerciseCompletionRepository: ExerciseCompletionRepository,
    private val workoutScheduleRepository: WorkoutScheduleRepository
) : ViewModel() {

    private val _currentWorkoutPlan = MutableStateFlow<WorkoutPlan?>(null)
    val currentWorkoutPlan: StateFlow<WorkoutPlan?> = _currentWorkoutPlan

    // Состояние для отслеживания, завершена ли первоначальная проверка профиля
    private val _isProfileChecked = MutableStateFlow(false)
    val isProfileChecked: StateFlow<Boolean> = _isProfileChecked

    // Текущий профиль пользователя
    val userProfile = userRepository.getUserProfile().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // История веса
    val weightHistory = weightRepository.getWeightHistory().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Статистика упражнений
    val exerciseStats = exerciseStatsRepository.getExerciseStats().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Текущий username для привязки данных
    private val _currentUsername = MutableStateFlow("")
    val currentUsername: StateFlow<String> = _currentUsername

    // Выполненные упражнения (per user)
    private val _completedExercises = MutableStateFlow<Set<String>>(emptySet())
    val completedExercises: StateFlow<Set<String>> = _completedExercises

    // Расписание тренировок (даты)
    private val _workoutScheduleDates = MutableStateFlow<List<Long>>(emptyList())
    val workoutScheduleDates: StateFlow<List<Long>> = _workoutScheduleDates

    init {
        checkUserCredentials()
    }

    private fun checkUserCredentials() {
        viewModelScope.launch {
            val credentials = credentialsRepository.getCredentials()
            if (credentials != null) {
                checkUserProfile()
            } else {
                checkUserProfile()
            }
        }
    }

    private fun checkUserProfile() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile().first()
            if (profile != null) {
                generatePlan(profile)
            }
            _isProfileChecked.value = true
        }
    }

    fun saveUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            userRepository.saveUserProfile(userProfile)
            generatePlan(userProfile)
            _isProfileChecked.value = true
        }
    }

    private fun generatePlan(userProfile: UserProfile) {
        val generatedPlan = workoutPlanRepository.getWorkoutPlanForUser(userProfile)
        _currentWorkoutPlan.value = generatedPlan
        _currentUsername.value = userProfile.username
        loadCompletedExercises(userProfile.username)
        loadWorkoutSchedule(userProfile.username)
    }

    private fun loadCompletedExercises(username: String) {
        viewModelScope.launch {
            exerciseCompletionRepository.getAllCompletedExercises(username).collect { completed ->
                _completedExercises.value = completed
            }
        }
    }

    private fun loadWorkoutSchedule(username: String) {
        viewModelScope.launch {
            workoutScheduleRepository.getWorkoutSchedule(username).collect { dates ->
                _workoutScheduleDates.value = dates
                val currentPlan = _currentWorkoutPlan.value
                if (currentPlan != null && dates.isNotEmpty()) {
                    val planWithDates = workoutPlanRepository.getWorkoutPlanWithDates(currentPlan, dates)
                    _currentWorkoutPlan.value = planWithDates
                }
            }
        }
    }

    fun saveWorkoutSchedule(dates: List<Long>) {
        viewModelScope.launch {
            val username = currentUsername.value
            if (username.isNotEmpty()) {
                workoutScheduleRepository.saveWorkoutSchedule(username, dates)
                _workoutScheduleDates.value = dates

                val currentPlan = _currentWorkoutPlan.value
                if (currentPlan != null) {
                    val planWithDates = workoutPlanRepository.getWorkoutPlanWithDates(currentPlan, dates)
                    _currentWorkoutPlan.value = planWithDates
                }
            }
        }
    }

    fun updateWorkoutDayDate(dayIndex: Int, newDate: Long?) {
        viewModelScope.launch {
            val currentPlan = _currentWorkoutPlan.value ?: return@launch
            val username = currentUsername.value
            if (username.isEmpty()) return@launch

            val updatedWeeks = currentPlan.weeks.map { week ->
                val updatedDays = week.workoutPlan.days.mapIndexed { index, day ->
                    if (index == dayIndex) {
                        day.copy(scheduledDate = newDate)
                    } else {
                        day
                    }
                }
                week.copy(workoutPlan = week.workoutPlan.copy(days = updatedDays))
            }

            val updatedPlan = currentPlan.copy(weeks = updatedWeeks)
            _currentWorkoutPlan.value = updatedPlan

            val dates = updatedWeeks.flatMap { week ->
                week.workoutPlan.days.mapNotNull { it.scheduledDate }
            }
            _workoutScheduleDates.value = dates
            workoutScheduleRepository.saveWorkoutSchedule(username, dates)
        }
    }

    // Сохранить текущий вес в статистику
    fun saveCurrentWeight(weight: Double, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            weightRepository.saveWeight(weight, date)
        }
    }

    // Сохранить статистику упражнения
    fun saveExerciseStats(stats: ExerciseStats) {
        viewModelScope.launch {
            exerciseStatsRepository.saveExerciseStats(stats)
        }
    }

    // Сохранить креды
    fun saveCredentials(credentials: UserCredentials) {
        viewModelScope.launch {
            credentialsRepository.saveCredentials(credentials)
        }
    }

    // Получить креды
    suspend fun getCredentials(): UserCredentials? {
        return credentialsRepository.getCredentials()
    }

    fun toggleExerciseCompletion(exerciseName: String, completed: Boolean) {
        viewModelScope.launch {
            val username = currentUsername.value
            if (username.isNotEmpty()) {
                exerciseCompletionRepository.setExerciseCompleted(username, exerciseName, completed)
            }
        }
    }

    fun setCurrentUsername(username: String) {
        _currentUsername.value = username
    }
}
