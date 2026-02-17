package com.example.fitness_plan.domain.repository

import com.example.fitness_plan.domain.model.PlanHistory
import com.example.fitness_plan.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

interface PlanHistoryRepository {
    fun getPlanHistory(username: String): Flow<PlanHistory>
    suspend fun archivePlan(username: String, plan: WorkoutPlan)
}
