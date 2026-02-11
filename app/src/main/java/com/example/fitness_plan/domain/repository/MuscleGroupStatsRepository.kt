package com.example.fitness_plan.domain.repository

import com.example.fitness_plan.domain.model.MuscleGroupStats
import kotlinx.coroutines.flow.Flow

interface MuscleGroupStatsRepository {
    suspend fun saveMuscleGroupStats(username: String, stats: List<MuscleGroupStats>)
    fun getMuscleGroupStats(username: String): Flow<List<MuscleGroupStats>>
    suspend fun clearMuscleGroupStats(username: String)
}