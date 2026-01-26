package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExerciseLibraryUseCase @Inject constructor(
    private val exerciseLibraryRepository: ExerciseLibraryRepository
) {
    fun getAllExercises(): Flow<List<ExerciseLibrary>> {
        return exerciseLibraryRepository.getAllExercises()
    }

    fun getExercisesByType(type: ExerciseType): Flow<List<ExerciseLibrary>> {
        return exerciseLibraryRepository.getExercisesByType(type)
    }

    fun getExercisesByEquipment(equipment: List<EquipmentType>): Flow<List<ExerciseLibrary>> {
        return exerciseLibraryRepository.getExercisesByEquipment(equipment)
    }

    fun getExercisesByMuscleGroups(muscleGroups: List<MuscleGroup>): Flow<List<ExerciseLibrary>> {
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

    suspend fun initializeDefaultExercises() {
        exerciseLibraryRepository.initializeDefaultExercises()
    }

    suspend fun getAlternativeExercises(
        currentExerciseName: String,
        currentMuscleGroups: List<MuscleGroup>,
        limit: Int = 3
    ): List<ExerciseLibrary> {
        return exerciseLibraryRepository.getAlternativeExercises(
            currentExerciseName,
            currentMuscleGroups,
            limit
        )
    }
}
