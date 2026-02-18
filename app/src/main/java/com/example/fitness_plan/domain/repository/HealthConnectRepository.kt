package com.example.fitness_plan.domain.repository

import com.example.fitness_plan.domain.model.SmartwatchData
import com.example.fitness_plan.domain.model.SmartwatchSessionSummary
import kotlinx.coroutines.flow.Flow

interface HealthConnectRepository {
    
    val isAvailable: Flow<Boolean>
    
    val hasPermissions: Flow<Boolean>
    
    val isConnected: Flow<Boolean>
    
    val smartwatchData: Flow<SmartwatchData?>

    suspend fun checkAvailability(): Boolean
    
    suspend fun checkPermissions(): Boolean
    
    suspend fun hasRecentData(): Boolean
    
    fun getRequiredPermissions(): Set<String>
    
    suspend fun startSession(userAge: Int)
    
    suspend fun stopSession(): SmartwatchSessionSummary?
    
    suspend fun getCurrentData(): SmartwatchData?
    
    fun startMonitoring(userAge: Int): Flow<SmartwatchData>
    
    fun stopMonitoring()
    
    fun setCurrentUserAge(age: Int)
}
