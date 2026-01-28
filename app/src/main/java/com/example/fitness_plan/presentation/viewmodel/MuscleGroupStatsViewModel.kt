package com.example.fitness_plan.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.MuscleGroupDetail
import com.example.fitness_plan.domain.model.MuscleGroupInsight
import com.example.fitness_plan.domain.model.MuscleGroupStatsFilter
import com.example.fitness_plan.domain.model.MuscleGroupSummary
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import com.example.fitness_plan.domain.usecase.MuscleGroupStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MuscleGroupStatsViewModel"

@HiltViewModel
class MuscleGroupStatsViewModel @Inject constructor(
    private val credentialsRepository: ICredentialsRepository,
    private val muscleGroupStatsUseCase: MuscleGroupStatsUseCase
) : ViewModel() {

    private val _currentUsername = MutableStateFlow("")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    private val _selectedFilter = MutableStateFlow(MuscleGroupStatsFilter.MONTH)
    val selectedFilter: StateFlow<MuscleGroupStatsFilter> = _selectedFilter.asStateFlow()

    private val _summaries = MutableStateFlow<List<MuscleGroupSummary>>(emptyList())
    val summaries: StateFlow<List<MuscleGroupSummary>> = _summaries.asStateFlow()

    private val _insights = MutableStateFlow<List<MuscleGroupInsight>>(emptyList())
    val insights: StateFlow<List<MuscleGroupInsight>> = _insights.asStateFlow()

    private val _selectedMuscleGroup = MutableStateFlow<com.example.fitness_plan.domain.model.MuscleGroup?>(null)
    val selectedMuscleGroup: StateFlow<com.example.fitness_plan.domain.model.MuscleGroup?> = _selectedMuscleGroup.asStateFlow()

    private val _muscleGroupDetail = MutableStateFlow<MuscleGroupDetail?>(null)
    val muscleGroupDetail: StateFlow<MuscleGroupDetail?> = _muscleGroupDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            val username = credentialsRepository.getUsername() ?: ""
            _currentUsername.value = username
            if (username.isNotEmpty()) {
                observeData()
            }
        }
    }

    fun setFilter(filter: MuscleGroupStatsFilter) {
        _selectedFilter.value = filter
    }

    fun selectMuscleGroup(muscleGroup: com.example.fitness_plan.domain.model.MuscleGroup?) {
        _selectedMuscleGroup.value = muscleGroup
        if (muscleGroup != null) {
            loadMuscleGroupDetail(muscleGroup)
        } else {
            _muscleGroupDetail.value = null
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Starting to observe data")

            combine(_currentUsername, _selectedFilter) { username, filter ->
                username to filter
            }.collect { (username, filter) ->
                if (username.isNotEmpty()) {
                    Log.d(TAG, "Loading data: username=$username, filter=$filter")
                    try {
                        val summaries = muscleGroupStatsUseCase.getMuscleGroupSummaries(username, filter).first()
                        Log.d(TAG, "Received ${summaries.size} summaries")
                        _summaries.value = summaries
                        _isLoading.value = false
                        generateInsights(username, filter)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading data", e)
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    private suspend fun generateInsights(username: String, filter: MuscleGroupStatsFilter) {
        Log.d(TAG, "Generating insights for username=$username, filter=$filter")
        try {
            val insights = muscleGroupStatsUseCase.generateInsights(username, filter)
            _insights.value = insights
            Log.d(TAG, "Generated ${insights.size} insights")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating insights", e)
        }
    }

    private fun loadMuscleGroupDetail(muscleGroup: com.example.fitness_plan.domain.model.MuscleGroup) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val username = _currentUsername.value
                val filter = _selectedFilter.value
                val detail = muscleGroupStatsUseCase.getMuscleGroupDetail(
                    username,
                    muscleGroup,
                    filter
                )
                _muscleGroupDetail.value = detail
            } catch (e: Exception) {
                Log.e(TAG, "Error loading muscle group detail", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
    }
}