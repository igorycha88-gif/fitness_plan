package com.example.fitness_plan.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.model.MuscleGroupStats
import com.example.fitness_plan.domain.repository.MuscleGroupStatsRepository as DomainMuscleGroupStatsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MuscleGroupStatsRepo"

private val Context.muscleGroupStatsDataStore: DataStore<Preferences> by preferencesDataStore(name = "muscle_group_stats")

@Singleton
class MuscleGroupStatsRepository @Inject constructor(
    private val context: Context
) : DomainMuscleGroupStatsRepository {

    private val gson = Gson()

    private fun getStatsKey(username: String): Preferences.Key<String> {
        return stringPreferencesKey("${username}_muscle_stats")
    }

    override suspend fun saveMuscleGroupStats(username: String, stats: List<MuscleGroupStats>) {
        Log.d(TAG, "=== Сохранение статистики по группам мышц ===")
        Log.d(TAG, "Пользователь: $username")
        Log.d(TAG, "Количество записей: ${stats.size}")

        if (stats.isEmpty()) {
            Log.d(TAG, "Нет данных для сохранения")
            return
        }

        val key = getStatsKey(username)
        context.muscleGroupStatsDataStore.edit { preferences ->
            val json = preferences[key] ?: "[]"
            val type = object : TypeToken<List<MuscleGroupStats>>() {}.type
            val currentList: MutableList<MuscleGroupStats> = try {
                gson.fromJson(json, type) ?: mutableListOf()
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при чтении существующих данных", e)
                mutableListOf()
            }
            val previousSize = currentList.size
            currentList.addAll(stats)
            preferences[key] = gson.toJson(currentList)
            Log.d(TAG, "✅ Статистика успешно сохранена!")
            Log.d(TAG, "Количество записей до: $previousSize")
            Log.d(TAG, "Количество записей после: ${currentList.size}")
        }
    }

    override fun getMuscleGroupStats(username: String): Flow<List<MuscleGroupStats>> {
        val key = getStatsKey(username)
        return context.muscleGroupStatsDataStore.data.map { preferences ->
            val json = preferences[key] ?: "[]"
            val type = object : TypeToken<List<MuscleGroupStats>>() {}.type
            try {
                val stats = gson.fromJson<List<MuscleGroupStats>>(json, type) ?: emptyList()
                if (stats.isNotEmpty()) {
                    Log.d(TAG, "Loaded ${stats.size} muscle group stats for user $username")
                    val uniqueGroups = stats.map { it.muscleGroup.displayName }.distinct()
                    Log.d(TAG, "Unique muscle groups: ${uniqueGroups.size} - ${uniqueGroups}")
                } else {
                    Log.d(TAG, "No muscle group stats found for user $username")
                }
                stats
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing muscle group stats for user $username", e)
                emptyList()
            }
        }
    }

    override suspend fun clearMuscleGroupStats(username: String) {
        val key = getStatsKey(username)
        context.muscleGroupStatsDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}