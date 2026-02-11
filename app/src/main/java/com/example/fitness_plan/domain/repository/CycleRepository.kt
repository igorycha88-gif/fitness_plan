package com.example.fitness_plan.domain.repository

import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleHistoryEntry
import kotlinx.coroutines.flow.Flow

interface CycleRepository {
    fun getCurrentCycle(username: String): Flow<Cycle?>
    suspend fun getCurrentCycleSync(username: String): Cycle?
    suspend fun startNewCycle(username: String, startDate: Long): Cycle
    suspend fun updateDaysCompleted(username: String, daysCompleted: Int)
    suspend fun updateCompletedMicrocycles(username: String, completedMicrocycles: Int)
    suspend fun markCycleCompleted(username: String, completedDate: Long)
    suspend fun resetCycle(username: String)
    suspend fun getCompletedDate(username: String): Long?
    suspend fun hasActiveCycle(username: String): Boolean
    fun getCycleHistory(username: String): Flow<List<CycleHistoryEntry>>
}
