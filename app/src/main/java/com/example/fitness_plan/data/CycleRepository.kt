package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleHistoryEntry
import com.example.fitness_plan.domain.repository.CycleRepository as DomainCycleRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cycleDataStore: DataStore<Preferences> by preferencesDataStore(name = "cycles")

@Singleton
class CycleRepository @Inject constructor(
    private val context: Context
) : DomainCycleRepository {

    private val gson = Gson()

    private fun getCycleKey(username: String) = stringPreferencesKey("${username}_current_cycle")
    private fun getHistoryKey(username: String) = stringPreferencesKey("${username}_cycle_history")
    private fun getCompletedDateKey(username: String) = longPreferencesKey("${username}_cycle_completed_date")

    override suspend fun startNewCycle(username: String, startDate: Long): Cycle {
        val currentCycle = getCurrentCycleSync(username)
        val newCycleNumber = if (currentCycle != null) currentCycle.cycleNumber + 1 else 1

        val newCycle = Cycle(
            cycleNumber = newCycleNumber,
            startDate = startDate,
            completedDate = null,
            daysCompleted = 0
        )

        val key = getCycleKey(username)
        val json = gson.toJson(newCycle)
        context.cycleDataStore.edit { preferences ->
            preferences[key] = json
            preferences.remove(getCompletedDateKey(username))
        }

        return newCycle
    }

    override fun getCurrentCycle(username: String): Flow<Cycle?> {
        val key = getCycleKey(username)
        return context.cycleDataStore.data.map { preferences ->
            val json = preferences[key] ?: return@map null
            try {
                gson.fromJson(json, Cycle::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getCurrentCycleSync(username: String): Cycle? {
        val key = getCycleKey(username)
        val json = context.cycleDataStore.data.first()[key] ?: return null
        return try {
            gson.fromJson(json, Cycle::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateDaysCompleted(username: String, daysCompleted: Int) {
        val current = getCurrentCycleSync(username) ?: return
        val updated = current.copy(daysCompleted = daysCompleted)
        val key = getCycleKey(username)
        val json = gson.toJson(updated)
        context.cycleDataStore.edit { preferences ->
            preferences[key] = json
        }
    }
    
    override suspend fun updateCompletedMicrocycles(username: String, completedMicrocycles: Int) {
        val current = getCurrentCycleSync(username) ?: return
        val updated = current.copy(completedMicrocycles = completedMicrocycles)
        val key = getCycleKey(username)
        val json = gson.toJson(updated)
        context.cycleDataStore.edit { preferences ->
            preferences[key] = json
        }
    }

    override suspend fun markCycleCompleted(username: String, completedDate: Long) {
        val current = getCurrentCycleSync(username) ?: return
        val updated = current.copy(completedDate = completedDate)

        val cycleKey = getCycleKey(username)
        val historyKey = getHistoryKey(username)
        val completedDateKey = getCompletedDateKey(username)

        val historyJson = context.cycleDataStore.data.first()[historyKey] ?: "[]"
        val historyType = object : TypeToken<List<CycleHistoryEntry>>() {}.type
        val history: MutableList<CycleHistoryEntry> = try {
            gson.fromJson(historyJson, historyType) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        history.add(
            CycleHistoryEntry(
                cycleNumber = updated.cycleNumber,
                startDate = updated.startDate,
                completedDate = completedDate,
                daysCompleted = updated.daysCompleted
            )
        )

        context.cycleDataStore.edit { preferences ->
            preferences[cycleKey] = gson.toJson(updated)
            preferences[historyKey] = gson.toJson(history)
            preferences[completedDateKey] = completedDate
        }
    }

    override fun getCycleHistory(username: String): Flow<List<CycleHistoryEntry>> {
        val key = getHistoryKey(username)
        return context.cycleDataStore.data.map { preferences ->
            val json = preferences[key] ?: return@map emptyList()
            try {
                val type = object : TypeToken<List<CycleHistoryEntry>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getCompletedDate(username: String): Long? {
        val key = getCompletedDateKey(username)
        return context.cycleDataStore.data.first()[key]
    }

    override suspend fun resetCycle(username: String) {
        context.cycleDataStore.edit { preferences ->
            preferences.remove(getCycleKey(username))
            preferences.remove(getCompletedDateKey(username))
        }
    }

    override suspend fun hasActiveCycle(username: String): Boolean {
        val completedDate = getCompletedDate(username)
        if (completedDate != null) {
            return false
        }
        val cycle = getCurrentCycleSync(username)
        return cycle != null
    }
}
