package com.example.fitness_plan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.data.UserProfile
import com.example.fitness_plan.data.UserRepository
import com.example.fitness_plan.data.WorkoutPlan
import com.example.fitness_plan.data.WorkoutPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first // <-- ДОБАВЛЕН импорт
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workoutPlanRepository: WorkoutPlanRepository
) : ViewModel() {

    private val _currentWorkoutPlan = MutableStateFlow<WorkoutPlan?>(null)
    val currentWorkoutPlan: StateFlow<WorkoutPlan?> = _currentWorkoutPlan

    // Состояние для отслеживания, завершена ли первоначальная проверка профиля
    private val _isProfileChecked = MutableStateFlow(false)
    val isProfileChecked: StateFlow<Boolean> = _isProfileChecked

    init {
        // Запускаем проверку профиля сразу при создании ViewModel
        checkUserProfile()
    }

    private fun checkUserProfile() {
        viewModelScope.launch {
            // Получаем первый доступный профиль из DataStore
            val profile = userRepository.getUserProfile().first()
            if (profile != null) {
                // Если профиль есть, генерируем план сразу
                generatePlan(profile)
            }
            _isProfileChecked.value = true // Устанавливаем флаг, что проверка завершена
        }
    }

    // Эта функция теперь вызывает приватную generatePlan
    fun saveUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            userRepository.saveUserProfile(userProfile)
            generatePlan(userProfile)
        }
    }

    // Приватная функция для генерации плана
    private fun generatePlan(userProfile: UserProfile) {
        val generatedPlan = workoutPlanRepository.getWorkoutPlanForUser(userProfile)
        _currentWorkoutPlan.value = generatedPlan
    }
}
