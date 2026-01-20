package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.WeightEntry
import com.example.fitness_plan.domain.model.WeightHistory
import com.example.fitness_plan.domain.model.WeightStats
import com.example.fitness_plan.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WeightUseCase @Inject constructor(
    private val weightRepository: WeightRepository
) {
    data class WeightState(
        val history: List<WeightEntry>,
        val stats: WeightStats?
    )

    fun getWeightHistory(username: String): Flow<List<WeightEntry>> {
        return weightRepository.getWeightHistory(username)
    }

    fun getWeightStats(username: String): Flow<WeightState> {
        return weightRepository.getWeightHistory(username).map { entries ->
            val stats = calculateStats(entries)
            WeightState(entries, stats)
        }
    }

    suspend fun saveWeight(username: String, weight: Double, date: Long = System.currentTimeMillis()) {
        weightRepository.saveWeight(username, weight, date)
    }

    private fun calculateStats(entries: List<WeightEntry>): WeightStats? {
        if (entries.isEmpty()) return null

        val sortedEntries = entries.sortedBy { it.date }
        val startWeight = sortedEntries.first().weight
        val currentWeight = sortedEntries.last().weight
        val minWeight = entries.minOfOrNull { it.weight } ?: 0.0
        val maxWeight = entries.maxOfOrNull { it.weight } ?: 0.0
        val averageWeight = entries.map { it.weight }.average()
        val totalChange = currentWeight - startWeight

        return WeightStats(
            startWeight = startWeight,
            currentWeight = currentWeight,
            minWeight = minWeight,
            maxWeight = maxWeight,
            averageWeight = averageWeight,
            totalChange = totalChange
        )
    }

    fun getFilteredHistory(username: String, days: Int): Flow<List<WeightEntry>> {
        return weightRepository.getWeightHistory(username).map { entries ->
            val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            entries.filter { it.date >= cutoffTime }.sortedBy { it.date }
        }
    }
}
