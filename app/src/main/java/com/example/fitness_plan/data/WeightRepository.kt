package com.example.fitness_plan.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.model.WeightEntry
import com.example.fitness_plan.domain.repository.WeightRepository as DomainWeightRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WeightRepository"

private val Context.weightDataStore: DataStore<Preferences> by preferencesDataStore(name = "weight_history")

@Singleton
class WeightRepository @Inject constructor(
    private val context: Context
) : DomainWeightRepository {

    private val gson = Gson()

    private fun getDataStorePath(): String {
        return try {
            val dataDir = context.dataDir?.absolutePath ?: "N/A"
            val datastorePath = File(dataDir, "datastore/weight_history.preferences_pb").absolutePath
            Log.d(TAG, "DataStore path: $datastorePath")
            Log.d(TAG, "DataStore exists: ${File(datastorePath).exists()}")
            datastorePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get DataStore path", e)
            "Error getting path: ${e.message}"
        }
    }

    init {
        Log.d(TAG, "=== WeightRepository initialized ===")
        getDataStorePath()
    }

    private fun getWeightsKey(username: String): Preferences.Key<String> {
        return stringPreferencesKey("${username}_weights")
    }

    override suspend fun saveWeight(username: String, weight: Double, date: Long) {
        try {
            Log.d(TAG, "saveWeight: START for username=$username, weight=$weight, date=$date")

            val entry = WeightEntry(date, weight)
            val key = getWeightsKey(username)
            val currentList = getWeightsList(username)
            val updatedList = currentList + entry
            val json = gson.toJson(updatedList)

            Log.d(TAG, "saveWeight: saving ${updatedList.size} entries for username=$username")

            context.weightDataStore.edit { preferences ->
                preferences[key] = json
            }

            Log.d(TAG, "saveWeight: SUCCESS - saved weight entry for username=$username")

            verifyStoredWeights(username, updatedList)
        } catch (e: Exception) {
            Log.e(TAG, "saveWeight: FAILED", e)
            throw e
        }
    }

    private suspend fun verifyStoredWeights(username: String, expectedList: List<WeightEntry>) {
        try {
            val storedList = getWeightsList(username)
            if (storedList.size == expectedList.size) {
                Log.d(TAG, "verifyStoredWeights: SUCCESS - ${storedList.size} entries match")
            } else {
                Log.w(TAG, "verifyStoredWeights: WARNING - expected ${expectedList.size}, got ${storedList.size} entries")
            }
        } catch (e: Exception) {
            Log.e(TAG, "verifyStoredWeights: FAILED", e)
        }
    }

    override fun getWeightHistory(username: String): Flow<List<WeightEntry>> {
        val key = getWeightsKey(username)
        Log.d(TAG, "getWeightHistory: requesting history for username=$username")

        return context.weightDataStore.data.map { preferences ->
            val json = preferences[key] ?: "[]"
            val type = object : TypeToken<List<WeightEntry>>() {}.type
            try {
                val result: List<WeightEntry> = gson.fromJson(json, type) ?: emptyList()
                Log.d(TAG, "getWeightHistory: loaded ${result.size} entries for username=$username")
                result
            } catch (e: Exception) {
                Log.e(TAG, "getWeightHistory: failed to parse weights for username=$username", e)
                emptyList()
            }
        }
    }

    override suspend fun clearWeightHistory(username: String) {
        val key = getWeightsKey(username)
        context.weightDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    private suspend fun getWeightsList(username: String): List<WeightEntry> {
        return getWeightHistory(username).first()
    }

    companion object {
        @Deprecated("Use getWeightHistory with username instead")
        private val WEIGHTS_KEY = stringPreferencesKey("weights")
    }
}