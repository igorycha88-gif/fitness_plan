package com.example.fitness_plan.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository as DomainExerciseStatsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ExerciseStatsRepo"

private val Context.exerciseStatsDataStore: DataStore<Preferences> by preferencesDataStore(name = "exercise_stats")

@Singleton
class ExerciseStatsRepository @Inject constructor(
    private val context: Context
) : DomainExerciseStatsRepository {

    private val gson = Gson()

    private fun getStatsKey(username: String): Preferences.Key<String> {
        return stringPreferencesKey("${username}_stats")
    }

    override suspend fun saveExerciseStats(username: String, stats: ExerciseStats) {
        Log.d(TAG, "Saving exercise stats: username=$username, exercise=${stats.exerciseName}, weight=${stats.weight}, reps=${stats.reps}, volume=${stats.volume}")
        val key = getStatsKey(username)
        context.exerciseStatsDataStore.edit { preferences ->
            val json = preferences[key] ?: "[]"
            val type = object : TypeToken<List<ExerciseStats>>() {}.type
            val currentList: MutableList<ExerciseStats> = try {
                gson.fromJson(json, type) ?: mutableListOf()
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing existing stats", e)
                mutableListOf()
            }
            currentList.add(stats)
            preferences[key] = gson.toJson(currentList)
            Log.d(TAG, "Saved stats. Total records for user: ${currentList.size}")
        }
    }

    override fun getExerciseStats(username: String): Flow<List<ExerciseStats>> {
        val key = getStatsKey(username)
        return context.exerciseStatsDataStore.data.map { preferences ->
            val json = preferences[key] ?: "[]"
            val type = object : TypeToken<List<ExerciseStats>>() {}.type
            try {
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun clearExerciseStats(username: String) {
        val key = getStatsKey(username)
        context.exerciseStatsDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    override suspend fun getLastNExerciseStats(username: String, exerciseName: String, count: Int): List<ExerciseStats> {
        val allStats = getStatsList(username)
        return allStats
            .filter { it.exerciseName == exerciseName }
            .sortedByDescending { it.date }
            .take(count)
    }

    private suspend fun getStatsList(username: String): List<ExerciseStats> {
        return getExerciseStats(username).first()
    }
}