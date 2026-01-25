package com.example.fitness_plan.domain.repository

import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import kotlinx.coroutines.flow.Flow

interface ExerciseLibraryRepository {
    fun getAllExercises(): Flow<List<ExerciseLibrary>>
    fun getExercisesByType(type: ExerciseType): Flow<List<ExerciseLibrary>>
    fun getExercisesByEquipment(equipment: List<EquipmentType>): Flow<List<ExerciseLibrary>>
    fun getExercisesByMuscleGroups(muscleGroups: List<MuscleGroup>): Flow<List<ExerciseLibrary>>
    fun searchExercises(query: String): Flow<List<ExerciseLibrary>>
    suspend fun addExercise(exercise: ExerciseLibrary)
    suspend fun deleteExercise(exerciseId: String)
    suspend fun getExerciseById(id: String): ExerciseLibrary?
    suspend fun initializeDefaultExercises()
    suspend fun getAllExercisesAsList(): List<ExerciseLibrary>
}
