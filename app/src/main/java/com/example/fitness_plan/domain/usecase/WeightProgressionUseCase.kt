package com.example.fitness_plan.domain.usecase

import android.util.Log
import com.example.fitness_plan.domain.calculator.WeightCalculator
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.WeightProgressionResult
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.WorkoutRepository
import com.example.fitness_plan.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private const val TAG = "WeightProgressionUseCase"

class WeightProgressionUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseStatsRepository: ExerciseStatsRepository,
    private val userRepository: UserRepository,
    private val weightCalculator: WeightCalculator
) {
    
    data class WeightProgressionSummary(
        val exerciseResults: List<WeightProgressionResult>,
        val totalIncreased: Int,
        val totalDecreased: Int,
        val totalUnchanged: Int,
        val totalNoHistory: Int
    )
    
    suspend fun applyAdaptiveProgression(username: String): Result<WeightProgressionSummary> {
        return try {
            Log.d(TAG, "applyAdaptiveProgression: username=$username")
            
            val plan = workoutRepository.getWorkoutPlan(username).first()
            if (plan == null) {
                Log.w(TAG, "No workout plan found for user $username")
                return Result.failure(Exception("Workout plan not found"))
            }
            
            val profile = userRepository.getUserProfile().first()
            if (profile == null) {
                Log.w(TAG, "No user profile found for $username")
                return Result.failure(Exception("User profile not found"))
            }
            
            val results = mutableListOf<WeightProgressionResult>()
            
            for (day in plan.days) {
                for (exercise in day.exercises) {
                    if (exercise.recommendedWeight == null) continue
                    
                    val exerciseHistory = exerciseStatsRepository.getLastNExerciseStats(
                        username,
                        exercise.name,
                        2
                    )
                    
                    val baseReps = exercise.recommendedRepsPerSet?.let { parseRepsString(it) }
                        ?: weightCalculator.getRecommendedReps(profile.level)
                    
                    val progressionResult = weightCalculator.calculateWeightProgression(
                        exerciseName = exercise.name,
                        currentWeight = exercise.recommendedWeight,
                        history = exerciseHistory,
                        baseReps = baseReps
                    )
                    
                    results.add(progressionResult)
                    Log.d(TAG, "Progression for ${exercise.name}: ${progressionResult.changeType}, reason: ${progressionResult.reason}")
                }
            }
            
            val updatedPlan = updatePlanWithNewWeights(plan, results)
            workoutRepository.saveWorkoutPlan(username, updatedPlan)
            
            Log.d(TAG, "Workout plan saved with adaptive weights")
            
            val summary = WeightProgressionSummary(
                exerciseResults = results,
                totalIncreased = results.count { it.changeType == com.example.fitness_plan.domain.model.WeightChangeType.INCREASED },
                totalDecreased = results.count { it.changeType == com.example.fitness_plan.domain.model.WeightChangeType.DECREASED },
                totalUnchanged = results.count { it.changeType == com.example.fitness_plan.domain.model.WeightChangeType.UNCHANGED },
                totalNoHistory = results.count { it.changeType == com.example.fitness_plan.domain.model.WeightChangeType.NO_HISTORY }
            )
            
            Result.success(summary)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying adaptive progression", e)
            Result.failure(e)
        }
    }
    
    private fun updatePlanWithNewWeights(
        plan: WorkoutPlan,
        results: List<WeightProgressionResult>
    ): WorkoutPlan {
        val weightMap = results.associateBy { it.exerciseName }
        
        val updatedDays = plan.days.map { day ->
            val updatedExercises = day.exercises.map { exercise ->
                val result = weightMap[exercise.name]
                if (result != null && result.newWeight != null && result.newWeight != result.oldWeight) {
                    exercise.copy(recommendedWeight = result.newWeight)
                } else {
                    exercise
                }
            }
            day.copy(exercises = updatedExercises)
        }
        
        return plan.copy(days = updatedDays)
    }
    
    private fun parseRepsString(repsString: String): List<Int> {
        return try {
            repsString.split(",").map { it.trim().toInt() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}