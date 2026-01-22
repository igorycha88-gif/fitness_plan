package com.example.fitness_plan.data

import android.content.Context
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
import javax.inject.Inject
import javax.inject.Singleton

private val Context.weightDataStore: DataStore<Preferences> by preferencesDataStore(name = "weight_history")

@Singleton
class WeightRepository @Inject constructor(
    private val context: Context
) : DomainWeightRepository {

    private val gson = Gson()

    private fun getWeightsKey(username: String): Preferences.Key<String> {
        return stringPreferencesKey("${username}_weights")
    }

    override suspend fun saveWeight(username: String, weight: Double, date: Long) {
        val entry = WeightEntry(date, weight)
        val key = getWeightsKey(username)
        val currentList = getWeightsList(username)
        val updatedList = currentList + entry
        val json = gson.toJson(updatedList)
        context.weightDataStore.edit { preferences ->
            preferences[key] = json
        }
    }

    override fun getWeightHistory(username: String): Flow<List<WeightEntry>> {
        val key = getWeightsKey(username)
        return context.weightDataStore.data.map { preferences ->
            val json = preferences[key] ?: "[]"
            val type = object : TypeToken<List<WeightEntry>>() {}.type
            try {
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
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