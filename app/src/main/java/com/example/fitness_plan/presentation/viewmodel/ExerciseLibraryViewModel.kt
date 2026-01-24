package com.example.fitness_plan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    private val exerciseLibraryUseCase: ExerciseLibraryUseCase
) : ViewModel() {

    private val _exercises = MutableStateFlow<List<ExerciseLibrary>>(emptyList())
    val exercises: StateFlow<List<ExerciseLibrary>> = _exercises.asStateFlow()

    fun initialize() {
        viewModelScope.launch {
            exerciseLibraryUseCase.getAllExercises().collect { exercises ->
                _exercises.value = exercises
            }
        }
    }

    fun getAllExercises(): Flow<List<ExerciseLibrary>> {
        return exerciseLibraryUseCase.getAllExercises()
    }
}
