package com.example.fitness_plan.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.BodyParameter
import com.example.fitness_plan.domain.model.BodyParameterType
import com.example.fitness_plan.domain.model.MeasurementInput
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.usecase.BodyParametersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BodyParametersViewModel"

@HiltViewModel
class BodyParametersViewModel @Inject constructor(
    private val useCase: BodyParametersUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUsername = MutableStateFlow("")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    val measurements = _currentUsername.flatMapLatest { username ->
        if (username.isEmpty()) {
            MutableStateFlow(emptyList())
        } else {
            useCase.getMeasurements(username)
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    emptyList()
                )
        }
    }

    val latestMeasurements = _currentUsername.flatMapLatest { username ->
        if (username.isEmpty()) {
            MutableStateFlow(emptyMap())
        } else {
            useCase.getLatestMeasurements(username)
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    emptyMap()
                )
        }
    }

    init {
        loadUsername()
    }

    private fun loadUsername() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile().first()
            _currentUsername.value = profile?.username ?: ""
        }
    }

    fun setShowAddDialog(show: Boolean) {
        _showAddDialog.value = show
    }

    fun saveMeasurement(input: MeasurementInput, calculateAuto: Boolean = true) {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile().first()
            val username = profile?.username

            if (username.isNullOrEmpty()) {
                _errorMessage.value = "Пользователь не найден"
                return@launch
            }

            val result = useCase.saveMeasurement(
                username = username,
                input = input,
                gender = profile?.gender,
                age = null,
                calculateAuto = calculateAuto
            )

            result.fold(
                onSuccess = {
                    _successMessage.value = "Измерение успешно сохранено"
                    _showAddDialog.value = false
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Ошибка при сохранении"
                }
            )
        }
    }

    suspend fun deleteMeasurement(measurementId: String) {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile().first()
            val username = profile?.username ?: return@launch

            val result = useCase.deleteMeasurement(username, measurementId)
            result.fold(
                onSuccess = {
                    _successMessage.value = "Измерение удалено"
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Ошибка при удалении"
                }
            )
        }
    }

    suspend fun clearAllMeasurements() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile().first()
            val username = profile?.username ?: return@launch

            val result = useCase.clearAllMeasurements(username)
            result.fold(
                onSuccess = {
                    _successMessage.value = "Все измерения удалены"
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Ошибка при удалении"
                }
            )
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun formatDate(date: Long): String {
        val dateFormat = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(date))
    }

    fun formatValue(value: Double, unit: String): String {
        return if (unit == "%") {
            "%.1f".format(value)
        } else {
            "%.1f".format(value)
        }
    }
}
