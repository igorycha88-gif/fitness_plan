package com.example.fitness_plan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    private val exerciseLibraryUseCase: ExerciseLibraryUseCase
) : ViewModel() {

    private val _exercises = MutableStateFlow<List<ExerciseLibrary>>(emptyList())
    val exercises: StateFlow<List<ExerciseLibrary>> = _exercises.asStateFlow()

    private val _favoriteExercises = MutableStateFlow<Set<String>>(emptySet())
    val favoriteExercises: StateFlow<Set<String>> = _favoriteExercises.asStateFlow()

    private val _selectedType = MutableStateFlow<ExerciseType?>(null)
    val selectedType: StateFlow<ExerciseType?> = _selectedType.asStateFlow()

    private val _selectedEquipment = MutableStateFlow<List<EquipmentType>>(emptyList())
    val selectedEquipment: StateFlow<List<EquipmentType>> = _selectedEquipment.asStateFlow()

    private val _selectedMuscles = MutableStateFlow<List<MuscleGroup>>(emptyList())
    val selectedMuscles: StateFlow<List<MuscleGroup>> = _selectedMuscles.asStateFlow()

    val filteredExercises: StateFlow<List<ExerciseLibrary>> = combine(
        _exercises,
        _favoriteExercises,
        _selectedType,
        _selectedEquipment,
        _selectedMuscles
    ) { exercises, favorites, type, equipment, muscles ->
        getFilteredAndSortedExercises(
            typeFilter = type,
            equipmentFilter = equipment,
            muscleFilter = muscles,
            favorites = favorites
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun initialize() {
        viewModelScope.launch {
            exerciseLibraryUseCase.initializeDefaultExercises()
            exerciseLibraryUseCase.getAllExercises().collect { exercises ->
                _exercises.value = exercises
            }
        }
    }

    fun setFavoriteExercises(favorites: Set<String>) {
        _favoriteExercises.value = favorites
    }

    suspend fun getExerciseById(id: String): ExerciseLibrary? {
        return exerciseLibraryUseCase.getExerciseById(id)
    }

    fun getAllExercises(): Flow<List<ExerciseLibrary>> {
        return exerciseLibraryUseCase.getAllExercises()
    }

    fun getFilteredAndSortedExercises(
        searchQuery: String = "",
        typeFilter: ExerciseType? = null,
        equipmentFilter: List<EquipmentType> = emptyList(),
        muscleFilter: List<MuscleGroup> = emptyList(),
        favorites: Set<String> = emptySet()
    ): List<ExerciseLibrary> {
        val allExercises = _exercises.value

        return allExercises
            .asSequence()
            .filter { exercise ->
                val matchesSearch = searchQuery.isBlank() ||
                    exercise.name.lowercase().contains(searchQuery.lowercase()) ||
                    exercise.description.lowercase().contains(searchQuery.lowercase())

                val matchesType = typeFilter == null || exercise.exerciseType == typeFilter

                val matchesEquipment = equipmentFilter.isEmpty() ||
                    equipmentFilter.any { equipment -> exercise.equipment.contains(equipment) }

                val matchesMuscle = muscleFilter.isEmpty() ||
                    muscleFilter.any { muscle -> exercise.muscleGroups.contains(muscle) }

                matchesSearch && matchesType && matchesEquipment && matchesMuscle
            }
            .sortedWith(
                compareByDescending<ExerciseLibrary> { favorites.contains(it.name) }
                    .thenBy { getMusclePriority(it.muscleGroups) }
                    .thenBy { it.name }
            )
            .toList()
    }

    private fun getMusclePriority(muscleGroups: List<MuscleGroup>): Int {
        val priorityOrder = listOf(
            MuscleGroup.SHOULDERS,
            MuscleGroup.LATS,
            MuscleGroup.BICEPS,
            MuscleGroup.TRICEPS,
            MuscleGroup.FOREARMS,
            MuscleGroup.TRAPS,
            MuscleGroup.CHEST,
            MuscleGroup.ABS,
            MuscleGroup.GLUTES,
            MuscleGroup.QUADS,
            MuscleGroup.HAMSTRINGS,
            MuscleGroup.CALVES
        )

        val minIndex = muscleGroups.minOfOrNull<MuscleGroup, Int> { group ->
            priorityOrder.indexOf(group)
        }
        return minIndex ?: Int.MAX_VALUE
    }

    fun setTypeFilter(type: ExerciseType?) {
        _selectedType.value = type
    }

    fun setEquipmentFilter(equipment: List<EquipmentType>) {
        _selectedEquipment.value = equipment
    }

    fun toggleMuscleFilter(muscle: MuscleGroup) {
        val current = _selectedMuscles.value.toMutableList()
        if (current.contains(muscle)) {
            current.remove(muscle)
        } else {
            current.add(muscle)
        }
        _selectedMuscles.value = current
    }

    fun resetFilters() {
        _selectedType.value = null
        _selectedEquipment.value = emptyList()
        _selectedMuscles.value = emptyList()
    }
}
