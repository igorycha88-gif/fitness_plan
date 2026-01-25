package com.example.fitness_plan.domain.repository

import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.model.ExerciseStats
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun getWorkoutPlanForUser(profile: com.example.fitness_plan.domain.model.UserProfile): WorkoutPlan
    suspend fun getCycleWorkoutPlan(basePlan: WorkoutPlan, frequency: String): WorkoutPlan
    suspend fun generateCycleDates(startDate: Long, frequency: String): List<Long>
    suspend fun getWorkoutPlanWithDates(plan: WorkoutPlan, dates: List<Long>): WorkoutPlan
    suspend fun saveWorkoutPlan(username: String, plan: WorkoutPlan)
    fun getWorkoutPlan(username: String): Flow<WorkoutPlan?>
    suspend fun saveWorkoutSchedule(username: String, dates: List<Long>)
    suspend fun saveAdminWorkoutPlan(plan: WorkoutPlan)
    fun getAdminWorkoutPlan(): Flow<WorkoutPlan?>
}

interface ExerciseStatsRepository {
    fun getExerciseStats(username: String): Flow<List<ExerciseStats>>
    suspend fun saveExerciseStats(username: String, stats: ExerciseStats)
    suspend fun clearExerciseStats(username: String)
    suspend fun getLastNExerciseStats(username: String, exerciseName: String, count: Int = 2): List<ExerciseStats>
}

interface ExerciseCompletionRepository {
    fun getAllCompletedExercises(username: String): Flow<Set<String>>
    suspend fun setExerciseCompleted(username: String, exerciseName: String, completed: Boolean)
    suspend fun clearCompletion(username: String)
}

interface WorkoutScheduleRepository {
    fun getWorkoutSchedule(username: String): Flow<List<Long>>
    suspend fun saveWorkoutSchedule(username: String, dates: List<Long>)
    suspend fun clearSchedule(username: String)
}
