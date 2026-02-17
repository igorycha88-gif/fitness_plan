package com.example.fitness_plan.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.repository.WorkoutScheduleRepository as DomainWorkoutScheduleRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WorkoutScheduleRepository"

private val Context.workoutScheduleDataStore: DataStore<Preferences> by preferencesDataStore(name = "workout_schedule")

@Singleton
class WorkoutScheduleRepository @Inject constructor(
    private val context: Context
) : DomainWorkoutScheduleRepository {

    private val gson = Gson()

    private fun getDataStorePath(): String {
        return try {
            val dataDir = context.dataDir?.absolutePath ?: "N/A"
            val datastorePath = File(dataDir, "datastore/workout_schedule.preferences_pb").absolutePath
            Log.d(TAG, "DataStore path: $datastorePath")
            Log.d(TAG, "DataStore exists: ${File(datastorePath).exists()}")
            datastorePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get DataStore path", e)
            "Error getting path: ${e.message}"
        }
    }

    init {
        Log.d(TAG, "=== WorkoutScheduleRepository initialized ===")
        getDataStorePath()
    }

    override suspend fun saveWorkoutSchedule(username: String, dates: List<Long>) {
        try {
            Log.d(TAG, "saveWorkoutSchedule: START for username=$username, dates count=${dates.size}")

            val key = stringPreferencesKey("${username}_schedule")
            val json = gson.toJson(dates)

            context.workoutScheduleDataStore.edit { preferences ->
                preferences[key] = json
            }

            Log.d(TAG, "saveWorkoutSchedule: SUCCESS - saved ${dates.size} dates for username=$username")
        } catch (e: Exception) {
            Log.e(TAG, "saveWorkoutSchedule: FAILED", e)
            throw e
        }
    }

    override fun getWorkoutSchedule(username: String): Flow<List<Long>> {
        val key = stringPreferencesKey("${username}_schedule")
        Log.d(TAG, "getWorkoutSchedule: requesting schedule for username=$username")

        return context.workoutScheduleDataStore.data.map { preferences ->
            val json = preferences[key] ?: "[]"
            val type = object : TypeToken<List<Long>>() {}.type
            try {
                val result: List<Long> = gson.fromJson(json, type) ?: emptyList()
                Log.d(TAG, "getWorkoutSchedule: loaded ${result.size} dates for username=$username")
                result
            } catch (e: Exception) {
                Log.e(TAG, "getWorkoutSchedule: failed to parse schedule for username=$username", e)
                emptyList()
            }
        }
    }

    override suspend fun clearSchedule(username: String) {
        try {
            Log.d(TAG, "clearSchedule: START for username=$username")

            val key = stringPreferencesKey("${username}_schedule")
            context.workoutScheduleDataStore.edit { preferences ->
                preferences.remove(key)
            }

            Log.d(TAG, "clearSchedule: SUCCESS - cleared schedule for username=$username")
        } catch (e: Exception) {
            Log.e(TAG, "clearSchedule: FAILED", e)
            throw e
        }
    }
}
