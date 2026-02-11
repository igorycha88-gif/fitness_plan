package com.example.fitness_plan.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.model.BodyParameter
import com.example.fitness_plan.domain.model.BodyParameterType
import com.example.fitness_plan.domain.repository.BodyParametersRepository as DomainBodyParametersRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BodyParametersRepository"

private val Context.bodyParametersDataStore: DataStore<Preferences> by preferencesDataStore(name = "body_parameters")

@Singleton
class BodyParametersRepository @Inject constructor(
    private val context: Context
) : DomainBodyParametersRepository {

    private val gson = Gson()

    init {
        Log.d(TAG, "=== BodyParametersRepository initialized ===")
    }

    private fun getParametersKey(username: String): Preferences.Key<String> {
        return stringPreferencesKey("${username}_body_parameters")
    }

    override suspend fun saveMeasurement(username: String, parameters: List<BodyParameter>) {
        try {
            Log.d(TAG, "saveMeasurement: START for username=$username, parameters count=${parameters.size}")

            val key = getParametersKey(username)
            val currentList = getParametersList(username)
            val updatedList = currentList + parameters
            val json = gson.toJson(updatedList)

            Log.d(TAG, "saveMeasurement: saving ${updatedList.size} entries for username=$username")

            context.bodyParametersDataStore.edit { preferences ->
                preferences[key] = json
            }

            Log.d(TAG, "saveMeasurement: SUCCESS - saved body parameters for username=$username")
        } catch (e: Exception) {
            Log.e(TAG, "saveMeasurement: FAILED", e)
            throw e
        }
    }

    override fun getMeasurements(username: String): Flow<List<BodyParameter>> {
        val key = getParametersKey(username)
        Log.d(TAG, "getMeasurements: requesting history for username=$username")

        return context.bodyParametersDataStore.data.map { preferences ->
            val json = preferences[key] ?: "[]"
            val type = object : TypeToken<List<BodyParameter>>() {}.type
            try {
                val result: List<BodyParameter> = gson.fromJson(json, type) ?: emptyList()
                Log.d(TAG, "getMeasurements: loaded ${result.size} entries for username=$username")
                result
            } catch (e: Exception) {
                Log.e(TAG, "getMeasurements: failed to parse parameters for username=$username", e)
                emptyList()
            }
        }
    }

    override fun getLatestMeasurements(username: String): Flow<Map<BodyParameterType, BodyParameter>> {
        return getMeasurements(username).map { parameters ->
            parameters.groupBy { it.parameterType }
                .mapValues { (_, params) -> params.maxByOrNull { it.date }!! }
                .filterValues { it.value > 0.0 }
        }
    }

    override suspend fun deleteMeasurement(username: String, measurementId: String) {
        try {
            Log.d(TAG, "deleteMeasurement: START for username=$username, measurementId=$measurementId")

            val key = getParametersKey(username)
            val currentList = getParametersList(username)
            val updatedList = currentList.filter { it.measurementId != measurementId }
            val json = gson.toJson(updatedList)

            context.bodyParametersDataStore.edit { preferences ->
                preferences[key] = json
            }

            Log.d(TAG, "deleteMeasurement: SUCCESS - deleted measurement for username=$username")
        } catch (e: Exception) {
            Log.e(TAG, "deleteMeasurement: FAILED", e)
            throw e
        }
    }

    override suspend fun clearAllMeasurements(username: String) {
        try {
            Log.d(TAG, "clearAllMeasurements: START for username=$username")

            val key = getParametersKey(username)
            context.bodyParametersDataStore.edit { preferences ->
                preferences.remove(key)
            }

            Log.d(TAG, "clearAllMeasurements: SUCCESS - cleared all measurements for username=$username")
        } catch (e: Exception) {
            Log.e(TAG, "clearAllMeasurements: FAILED", e)
            throw e
        }
    }

    override fun getMeasurementsByType(
        username: String,
        type: BodyParameterType
    ): Flow<List<BodyParameter>> {
        return getMeasurements(username).map { parameters ->
            parameters.filter { it.parameterType == type }.sortedBy { it.date }
        }
    }

    override fun getMeasurementsByDateRange(
        username: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<BodyParameter>> {
        return getMeasurements(username).map { parameters ->
            parameters.filter { it.date >= startDate && it.date <= endDate }.sortedBy { it.date }
        }
    }

    private suspend fun getParametersList(username: String): List<BodyParameter> {
        return getMeasurements(username).first()
    }
}
