package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.repository.WorkoutScheduleRepository as DomainWorkoutScheduleRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.workoutScheduleDataStore: DataStore<Preferences> by preferencesDataStore(name = "workout_schedule")

@Singleton
class WorkoutScheduleRepository @Inject constructor(
    private val context: Context
) : DomainWorkoutScheduleRepository {

    private val gson = Gson()

    override suspend fun saveWorkoutSchedule(username: String, dates: List<Long>) {
        val key = stringPreferencesKey("${username}_schedule")
        val json = gson.toJson(dates)
        context.workoutScheduleDataStore.edit { preferences ->
            preferences[key] = json
        }
    }

    override fun getWorkoutSchedule(username: String): Flow<List<Long>> {
        val key = stringPreferencesKey("${username}_schedule")
        return context.workoutScheduleDataStore.data.map { preferences ->
            val json = preferences[key] ?: "[]"
            val type = object : TypeToken<List<Long>>() {}.type
            try {
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun clearSchedule(username: String) {
        val key = stringPreferencesKey("${username}_schedule")
        context.workoutScheduleDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}
