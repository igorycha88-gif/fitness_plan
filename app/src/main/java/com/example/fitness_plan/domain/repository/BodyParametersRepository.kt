package com.example.fitness_plan.domain.repository

import com.example.fitness_plan.domain.model.BodyParameter
import com.example.fitness_plan.domain.model.BodyParameterType
import kotlinx.coroutines.flow.Flow

interface BodyParametersRepository {
    suspend fun saveMeasurement(username: String, parameters: List<BodyParameter>)
    fun getMeasurements(username: String): Flow<List<BodyParameter>>
    fun getLatestMeasurements(username: String): Flow<Map<BodyParameterType, BodyParameter>>
    suspend fun deleteMeasurement(username: String, measurementId: String)
    suspend fun clearAllMeasurements(username: String)
    fun getMeasurementsByType(
        username: String,
        type: BodyParameterType
    ): Flow<List<BodyParameter>>
    fun getMeasurementsByDateRange(
        username: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<BodyParameter>>
}
