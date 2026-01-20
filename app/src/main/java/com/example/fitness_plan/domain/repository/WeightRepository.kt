package com.example.fitness_plan.domain.repository

import com.example.fitness_plan.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun getWeightHistory(username: String): Flow<List<WeightEntry>>
    suspend fun saveWeight(username: String, weight: Double, date: Long = System.currentTimeMillis())
    suspend fun clearWeightHistory(username: String)
}
