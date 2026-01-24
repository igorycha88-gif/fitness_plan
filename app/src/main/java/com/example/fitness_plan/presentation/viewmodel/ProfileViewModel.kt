package com.example.fitness_plan.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WeightEntry
import com.example.fitness_plan.domain.repository.Credentials
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.repository.WeightRepository
import com.example.fitness_plan.domain.usecase.AuthUseCase
import com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase
import com.example.fitness_plan.domain.usecase.ReferenceDataUseCase
import com.example.fitness_plan.domain.usecase.WorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ProfileViewModel"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val credentialsRepository: com.example.fitness_plan.domain.repository.ICredentialsRepository,
    private val cycleRepository: CycleRepository,
    private val weightRepository: WeightRepository,
    private val authUseCase: AuthUseCase,
    private val workoutUseCase: WorkoutUseCase,
    private val exerciseLibraryUseCase: ExerciseLibraryUseCase,
    private val referenceDataUseCase: ReferenceDataUseCase
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = userRepository.getUserProfile()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    private val _isProfileChecked = MutableStateFlow(false)
    val isProfileChecked: StateFlow<Boolean> = _isProfileChecked.asStateFlow()

    private val _currentUsername = MutableStateFlow("")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _logoutTrigger = MutableStateFlow(false)
    val logoutTrigger: StateFlow<Boolean> = _logoutTrigger.asStateFlow()

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            val credentials = credentialsRepository.getCredentials()
            if (credentials != null) {
                Log.d(TAG, "checkUserSession: found credentials for user=${credentials.username}")
                _currentUsername.value = credentials.username
            } else {
                Log.d(TAG, "checkUserSession: no credentials found")
            }
            _isProfileChecked.value = true
        }
    }

    fun setCurrentUsername(username: String) {
        Log.d(TAG, "setCurrentUsername: username=$username")
        _currentUsername.value = username
    }

    fun setIsAdmin(isAdmin: Boolean) {
        Log.d(TAG, "setIsAdmin: isAdmin=$isAdmin")
        _isAdmin.value = isAdmin
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            val currentProfile = userRepository.getUserProfile().first()
            val goalOrLevelChanged = currentProfile == null ||
                    currentProfile.goal != profile.goal ||
                    currentProfile.level != profile.level ||
                    currentProfile.frequency != profile.frequency

            Log.d(TAG, "saveUserProfile: saving profile for user=${profile.username}")
            userRepository.saveUserProfile(profile)

            if (goalOrLevelChanged) {
                Log.d(TAG, "saveUserProfile: goal or level changed, resetting cycle")
                cycleRepository.resetCycle(profile.username)
            }

            _isProfileChecked.value = true
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "logout: clearing credentials")
            authUseCase.logout()
            _currentUsername.value = ""
            _isAdmin.value = false
            _logoutTrigger.value = true
        }
    }

    fun clearLogoutTrigger() {
        _logoutTrigger.value = false
    }

    suspend fun verifyPassword(username: String, plainPassword: String): Boolean {
        Log.d(TAG, "verifyPassword: checking password for user=$username")
        return credentialsRepository.verifyPassword(username, plainPassword)
    }

    suspend fun getCredentials(): Credentials? {
        return credentialsRepository.getCredentials()
    }

    suspend fun saveCredentials(username: String, plainPassword: String) {
        Log.d(TAG, "saveCredentials: saving for user=$username")
        credentialsRepository.saveCredentials(username, plainPassword)
        _currentUsername.value = username
    }

    suspend fun updateCredentials(currentUsername: String, newUsername: String, newPassword: String) {
        Log.d(TAG, "updateCredentials: updating from $currentUsername to $newUsername")
        credentialsRepository.saveCredentials(newUsername, newPassword)
        _currentUsername.value = newUsername
    }

    suspend fun getCurrentUsername(): String {
        return credentialsRepository.getUsername() ?: ""
    }

    fun saveWeightEntry(weight: Double, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val username = getCurrentUsername()
            Log.d(TAG, "saveWeightEntry: saving weight=$weight for user=$username")
            weightRepository.saveWeight(username, weight, date)
        }
    }

    fun getWeightHistory(): kotlinx.coroutines.flow.Flow<List<WeightEntry>> {
        return weightRepository.getWeightHistory(_currentUsername.value)
    }

    fun saveWorkoutDates(dates: List<Long>) {
        viewModelScope.launch {
            workoutUseCase.updateWorkoutSchedule(_currentUsername.value, dates)
        }
    }

    fun toggleFavoriteExercise(exerciseName: String) {
        viewModelScope.launch {
            val currentProfile = userRepository.getUserProfile().first()
            val updatedFavorites = if (exerciseName in (currentProfile?.favoriteExercises ?: emptySet())) {
                (currentProfile?.favoriteExercises ?: emptySet()) - exerciseName
            } else {
                (currentProfile?.favoriteExercises ?: emptySet()) + exerciseName
            }
            val updatedProfile = currentProfile?.copy(favoriteExercises = updatedFavorites)
            if (updatedProfile != null) {
                userRepository.saveUserProfile(updatedProfile)
            }
        }
    }

    fun getFavoriteExercises(): kotlinx.coroutines.flow.Flow<kotlin.collections.Set<String>> {
        return userRepository.getUserProfile().map { it?.favoriteExercises ?: emptySet() }
    }
}
