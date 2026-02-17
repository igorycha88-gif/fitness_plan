package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroupSequence
import com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExercisePoolManager @Inject constructor(
    private val exerciseLibraryRepository: ExerciseLibraryRepository
) {
    private val poolRotationSize = 3

    suspend fun getExercisesForSequence(
        sequence: MuscleGroupSequence,
        excludedExerciseNames: Set<String>,
        count: Int
    ): List<ExerciseLibrary> {
        val allExercises = exerciseLibraryRepository.getAllExercisesAsList()

        val filteredExercises = when (sequence) {
            MuscleGroupSequence.CARDIO -> {
                allExercises.filter { it.exerciseType == ExerciseType.CARDIO }
            }
            else -> {
                allExercises.filter { exercise ->
                    exercise.exerciseType == ExerciseType.STRENGTH &&
                    exercise.muscleGroups.any { it in sequence.muscleGroups } &&
                    exercise.name !in excludedExerciseNames
                }
            }
        }

        return filteredExercises.shuffled().take(count)
    }

    fun determinePoolId(cycleNumber: Int): String {
        val poolIndex = ((cycleNumber - 1) % poolRotationSize)
        return "pool_${('A'.code + poolIndex).toChar()}"
    }

    suspend fun getAllStrengthExercises(): List<ExerciseLibrary> {
        return exerciseLibraryRepository.getAllExercisesAsList()
            .filter { it.exerciseType == ExerciseType.STRENGTH }
    }

    suspend fun getAllCardioExercises(): List<ExerciseLibrary> {
        return exerciseLibraryRepository.getAllExercisesAsList()
            .filter { it.exerciseType == ExerciseType.CARDIO }
    }

    fun calculateExerciseOverlap(history1: Set<String>, history2: Set<String>): Float {
        if (history1.isEmpty() || history2.isEmpty()) return 0f
        val intersection = history1.intersect(history2)
        return intersection.size.toFloat() / history1.size.coerceAtLeast(history2.size)
    }
}
