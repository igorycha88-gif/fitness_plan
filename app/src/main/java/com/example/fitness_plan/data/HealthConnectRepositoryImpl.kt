package com.example.fitness_plan.data

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.fitness_plan.domain.model.HeartRateZone
import com.example.fitness_plan.domain.model.SmartwatchData
import com.example.fitness_plan.domain.model.SmartwatchSessionSummary
import com.example.fitness_plan.domain.repository.HealthConnectRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HealthConnectRepo"

@Singleton
class HealthConnectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HealthConnectRepository {

    private var healthConnectClient: HealthConnectClient? = null
    
    private var sessionStartTime: Instant? = null
    private var currentUserAge: Int = 30
    private var monitoringJob: Job? = null
    
    private val heartRateSamples = mutableListOf<Int>()
    private val caloriesData = mutableListOf<Double>()
    
    private val _isAvailable = MutableStateFlow(false)
    override val isAvailable: Flow<Boolean> = _isAvailable.asStateFlow()
    
    private val _hasPermissions = MutableStateFlow(false)
    override val hasPermissions: Flow<Boolean> = _hasPermissions.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: Flow<Boolean> = _isConnected.asStateFlow()
    
    private val _smartwatchData = MutableStateFlow<SmartwatchData?>(null)
    override val smartwatchData: Flow<SmartwatchData?> = _smartwatchData.asStateFlow()

    companion object {
        val REQUIRED_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class)
        )
    }

    private fun getClient(): HealthConnectClient? {
        if (healthConnectClient == null) {
            try {
                healthConnectClient = HealthConnectClient.getOrCreate(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create HealthConnectClient", e)
            }
        }
        return healthConnectClient
    }

    override suspend fun checkAvailability(): Boolean {
        return try {
            val status = HealthConnectClient.getSdkStatus(context, context.packageName)
            val available = status == HealthConnectClient.SDK_AVAILABLE
            _isAvailable.value = available
            Log.d(TAG, "Health Connect availability: $available (status: $status)")
            available
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Health Connect availability", e)
            _isAvailable.value = false
            false
        }
    }

    override suspend fun checkPermissions(): Boolean {
        val client = getClient() ?: run {
            _hasPermissions.value = false
            return false
        }
        
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            val hasAll = REQUIRED_PERMISSIONS.all { it in granted }
            _hasPermissions.value = hasAll
            Log.d(TAG, "Permissions check: $hasAll (granted: ${granted.size}/${REQUIRED_PERMISSIONS.size})")
            hasAll
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            _hasPermissions.value = false
            false
        }
    }

    override suspend fun hasRecentData(): Boolean {
        val client = getClient() ?: return false
        
        return try {
            val now = Instant.now()
            val oneMinuteAgo = now.minusSeconds(60)
            
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(oneMinuteAgo, now),
                pageSize = 1
            )
            
            val response = client.readRecords(request)
            val hasData = response.records.isNotEmpty()
            _isConnected.value = hasData
            Log.d(TAG, "Recent data check: $hasData")
            hasData
        } catch (e: Exception) {
            Log.e(TAG, "Error checking recent data", e)
            _isConnected.value = false
            false
        }
    }

    override fun getRequiredPermissions(): Set<String> = REQUIRED_PERMISSIONS

    override suspend fun startSession(userAge: Int) {
        sessionStartTime = Instant.now()
        currentUserAge = userAge
        heartRateSamples.clear()
        caloriesData.clear()
        Log.d(TAG, "Session started at $sessionStartTime for age $userAge")
    }

    override suspend fun stopSession(): SmartwatchSessionSummary? {
        val startTime = sessionStartTime ?: return null
        val endTime = Instant.now()
        
        val summary = SmartwatchSessionSummary(
            startTime = startTime.toEpochMilli(),
            endTime = endTime.toEpochMilli(),
            avgHeartRate = if (heartRateSamples.isNotEmpty()) heartRateSamples.average().toInt() else null,
            minHeartRate = heartRateSamples.minOrNull(),
            maxHeartRate = heartRateSamples.maxOrNull(),
            totalCalories = if (caloriesData.isNotEmpty()) caloriesData.sum() else null,
            totalSteps = null,
            totalDistance = null,
            totalDuration = endTime.toEpochMilli() - startTime.toEpochMilli(),
            dominantHeartRateZone = calculateDominantZone(),
            heartRateZoneDistribution = calculateZoneDistribution()
        )
        
        sessionStartTime = null
        heartRateSamples.clear()
        caloriesData.clear()
        
        Log.d(TAG, "Session stopped. Summary: avgHr=${summary.avgHeartRate}, calories=${summary.totalCalories}")
        return summary
    }

    override suspend fun getCurrentData(): SmartwatchData? {
        val client = getClient() ?: return null
        val startTime = sessionStartTime ?: Instant.now().minusSeconds(60)
        val now = Instant.now()
        
        return try {
            val heartRates = readHeartRates(client, startTime, now)
            val calories = readCalories(client, startTime, now)
            
            val lastHr = heartRates.lastOrNull()
            
            heartRates.forEach { heartRateSamples.add(it) }
            calories.forEach { caloriesData.add(it) }
            
            SmartwatchData(
                timestamp = now.toEpochMilli(),
                heartRate = lastHr,
                heartRateMin = heartRates.minOrNull(),
                heartRateMax = heartRates.maxOrNull(),
                heartRateAvg = if (heartRates.isNotEmpty()) heartRates.average().toInt() else null,
                caloriesBurned = calories.sum(),
                steps = null,
                distance = null,
                activeDuration = now.toEpochMilli() - startTime.toEpochMilli(),
                heartRateZone = HeartRateZone.fromHeartRate(lastHr, currentUserAge)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current data", e)
            null
        }
    }

    override fun startMonitoring(userAge: Int): Flow<SmartwatchData> = flow {
        currentUserAge = userAge
        if (sessionStartTime == null) {
            sessionStartTime = Instant.now()
        }
        
        while (currentCoroutineContext().isActive) {
            val data = getCurrentData()
            if (data != null) {
                _smartwatchData.value = data
                _isConnected.value = true
                emit(data)
            } else {
                _isConnected.value = false
            }
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

    override fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _smartwatchData.value = null
        Log.d(TAG, "Monitoring stopped")
    }

    override fun setCurrentUserAge(age: Int) {
        currentUserAge = age
    }

    private suspend fun readHeartRates(
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): List<Int> {
        return try {
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            
            val response = client.readRecords(request)
            response.records
                .flatMap { it.samples }
                .map { it.beatsPerMinute.toInt() }
                .filter { it in 40..220 }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading heart rates", e)
            emptyList()
        }
    }

    private suspend fun readCalories(
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): List<Double> {
        return try {
            val request = ReadRecordsRequest(
                recordType = ActiveCaloriesBurnedRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            
            val response = client.readRecords(request)
            response.records.map { it.energy.inKilocalories }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading calories", e)
            emptyList()
        }
    }

    private fun calculateDominantZone(): HeartRateZone? {
        if (heartRateSamples.isEmpty()) return null
        
        val zoneCounts = mutableMapOf<HeartRateZone, Int>()
        heartRateSamples.forEach { hr ->
            val zone = HeartRateZone.fromHeartRate(hr, currentUserAge) ?: return@forEach
            zoneCounts[zone] = (zoneCounts[zone] ?: 0) + 1
        }
        
        return zoneCounts.entries.maxByOrNull { it.value }?.key
    }

    private fun calculateZoneDistribution(): Map<HeartRateZone, Int> {
        if (heartRateSamples.isEmpty()) return emptyMap()
        
        val zoneCounts = mutableMapOf<HeartRateZone, Int>()
        heartRateSamples.forEach { hr ->
            val zone = HeartRateZone.fromHeartRate(hr, currentUserAge) ?: return@forEach
            zoneCounts[zone] = (zoneCounts[zone] ?: 0) + 1
        }
        
        val total = heartRateSamples.size
        return zoneCounts.mapValues { (_, count) -> ((count.toDouble() / total) * 100).toInt() }
    }
}
