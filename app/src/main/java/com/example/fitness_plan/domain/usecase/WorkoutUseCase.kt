package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.ExerciseSummary
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseStatsRepository: ExerciseStatsRepository,
    private val exerciseCompletionRepository: ExerciseCompletionRepository
) {
    data class WorkoutState(
        val workoutPlan: WorkoutPlan,
        val completedExercises: Set<String>,
        val completedDays: Set<Int>
    )

    suspend fun getWorkoutPlan(username: String): WorkoutPlan? {
        return workoutRepository.getWorkoutPlan(username).first()
    }

    fun getCompletedExercises(username: String): Flow<Set<String>> {
        return exerciseCompletionRepository.getAllCompletedExercises(username)
    }

    fun getExerciseStats(username: String): Flow<List<ExerciseStats>> {
        return exerciseStatsRepository.getExerciseStats(username)
    }

    suspend fun toggleExerciseCompletion(
        username: String,
        exerciseKey: String,
        completed: Boolean,
        currentPlan: WorkoutPlan?
    ): Set<Int> {
        val exerciseName = if (exerciseKey.contains("_")) {
            exerciseKey.substringAfter("_")
        } else {
            exerciseKey
        }

        if (exerciseKey.contains("_")) {
            exerciseCompletionRepository.setExerciseCompleted(username, exerciseKey, completed)
        } else {
            val exerciseKeysToToggle = mutableSetOf<String>()
            currentPlan?.days?.forEachIndexed { dayIndex, day ->
                if (day.exercises.any { it.name == exerciseName }) {
                    exerciseKeysToToggle.add("${dayIndex}_${exerciseName}")
                }
            }

            exerciseKeysToToggle.forEach { key ->
                exerciseCompletionRepository.setExerciseCompleted(username, key, completed)
            }
        }

        val allCompleted = exerciseCompletionRepository.getAllCompletedExercises(username).first()
        return calculateCompletedDays(allCompleted, currentPlan)
    }

    private fun calculateCompletedDays(
        completedExercises: Set<String>,
        plan: WorkoutPlan?
    ): Set<Int> {
        val completed = mutableSetOf<Int>()
        plan?.days?.forEachIndexed { dayIndex, day ->
            val dayExerciseKeys = day.exercises.map { "${dayIndex}_${it.name}" }.toSet()
            val hasCompleted = dayExerciseKeys.any { it in completedExercises }
            if (hasCompleted) {
                completed.add(dayIndex)
            }
        }
        return completed
    }

    suspend fun saveExerciseStats(
        username: String,
        exerciseName: String,
        weight: Double,
        reps: Int,
        setNumber: Int,
        sets: Int
    ) {
        val stats = ExerciseStats(
            exerciseName = exerciseName,
            date = System.currentTimeMillis(),
            weight = weight,
            reps = reps,
            setNumber = setNumber,
            sets = sets
        )
        exerciseStatsRepository.saveExerciseStats(username, stats)
    }

    fun getExerciseSummaries(
        username: String,
        exerciseNames: List<String>
    ): Flow<List<ExerciseSummary>> {
        return exerciseStatsRepository.getExerciseStats(username).map { allStats ->
            exerciseNames.map { name ->
                val exerciseStats = allStats.filter { it.exerciseName == name }
                ExerciseSummary(
                    exerciseName = name,
                    maxWeight = exerciseStats.maxOfOrNull { it.weight } ?: 0.0,
                    averageWeight = if (exerciseStats.isNotEmpty())
                        exerciseStats.map { it.weight }.average()
                    else 0.0,
                    totalVolume = exerciseStats.sumOf { (it.weight * it.reps).toLong() },
                    totalSets = exerciseStats.size,
                    completionPercentage = 0f
                )
            }
        }
    }

    suspend fun updateWorkoutSchedule(username: String, dates: List<Long>) {
        workoutRepository.saveWorkoutSchedule(username, dates)
    }

    suspend fun saveAdminWorkoutPlan(plan: WorkoutPlan) {
        workoutRepository.saveAdminWorkoutPlan(plan)
    }

    fun getAdminWorkoutPlan(): Flow<WorkoutPlan?> {
        return workoutRepository.getAdminWorkoutPlan()
    }
}
