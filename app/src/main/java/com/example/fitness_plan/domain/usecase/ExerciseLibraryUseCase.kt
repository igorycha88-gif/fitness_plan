package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
import com.example.fitness_plan.domain.model.ExerciseLibrary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExerciseLibraryUseCase @Inject constructor(
    private val exerciseLibraryRepository: ExerciseLibraryRepository
) {
    fun getAllExercises(): Flow<List<ExerciseLibrary>> {
        return exerciseLibraryRepository.getAllExercises()
    }

    fun getExercisesByType(type: com.example.fitness_plan.domain.model.ExerciseType): Flow<List<ExerciseLibrary>> {
        return exerciseLibraryRepository.getExercisesByType(type)
    }

    fun getExercisesByEquipment(equipment: List<com.example.fitness_plan.domain.model.EquipmentType>): Flow<List<ExerciseLibrary>> {
        return exerciseLibraryRepository.getExercisesByEquipment(equipment)
    }

    fun getExercisesByMuscleGroups(muscleGroups: List<com.example.fitness_plan.domain.model.MuscleGroup>): Flow<List<ExerciseLibrary>> {
        return exerciseLibraryRepository.getExercisesByMuscleGroups(muscleGroups)
    }

    fun searchExercises(query: String): Flow<List<ExerciseLibrary>> {
        return exerciseLibraryRepository.searchExercises(query)
    }

    suspend fun addExercise(exercise: ExerciseLibrary) {
        exerciseLibraryRepository.addExercise(exercise)
    }

    suspend fun deleteExercise(exerciseId: String) {
        exerciseLibraryRepository.deleteExercise(exerciseId)
    }

    suspend fun getExerciseById(id: String): ExerciseLibrary? {
        return exerciseLibraryRepository.getExerciseById(id)
    }

    suspend fun getFilteredExercises(
        typeFilter: com.example.fitness_plan.domain.model.ExerciseType? = null,
        equipmentFilter: List<com.example.fitness_plan.domain.model.EquipmentType> = emptyList(),
        muscleFilter: List<com.example.fitness_plan.domain.model.MuscleGroup> = emptyList(),
        searchQuery: String = ""
    ): List<ExerciseLibrary> {
        val allExercises = exerciseLibraryRepository.getAllExercises().first()

        return allExercises.filter { exercise ->
            val matchesType = typeFilter == null || exercise.exerciseType == typeFilter
            val matchesEquipment = equipmentFilter.isEmpty() || exercise.equipment.any { equip -> equipmentFilter.contains(equip) }
            val matchesMuscle = muscleFilter.isEmpty() || exercise.muscleGroups.any { muscle -> muscleFilter.contains(muscle) }
            val matchesSearch = searchQuery.isBlank() || exercise.name.lowercase().contains(searchQuery.lowercase()) || exercise.description.lowercase().contains(searchQuery.lowercase())

            matchesType && matchesEquipment && matchesMuscle && matchesSearch
        }
    }
}
