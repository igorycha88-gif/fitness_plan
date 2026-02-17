package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository as DomainExerciseCompletionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.exerciseCompletionDataStore: DataStore<Preferences> by preferencesDataStore(name = "exercise_completion")

@Singleton
class ExerciseCompletionRepository @Inject constructor(
    private val context: Context
) : DomainExerciseCompletionRepository {

    private var migrationPerformed = false

    override suspend fun setExerciseCompleted(username: String, exerciseName: String, completed: Boolean) {
        val key = stringPreferencesKey("${username}_${exerciseName}")
        context.exerciseCompletionDataStore.edit { preferences ->
            if (completed) {
                preferences[key] = "true"
            } else {
                preferences.remove(key)
            }
        }
    }

    override fun getAllCompletedExercises(username: String): Flow<Set<String>> {
        return context.exerciseCompletionDataStore.data.map { preferences ->
            preferences.asMap().entries
                .filter { it.key.name.startsWith("${username}_") }
                .filter { it.value == "true" }
                .map { it.key.name.removePrefix("${username}_") }
                .toSet()
        }
    }

    override suspend fun clearCompletion(username: String) {
        context.exerciseCompletionDataStore.edit { preferences ->
            preferences.asMap().entries
                .filter { it.key.name.startsWith("${username}_") }
                .forEach { preferences.remove(it.key) }
        }
    }

    override suspend fun migrateOldFormatExercises(
        username: String,
        workoutPlan: com.example.fitness_plan.domain.model.WorkoutPlan
    ) {
        if (migrationPerformed) return

        context.exerciseCompletionDataStore.edit { preferences ->
            val allKeys = preferences.asMap().keys

            allKeys.filter { key ->
                val keyName = key.name
                keyName.startsWith("${username}_") && keyName.indexOf("_", username.length + 1) == -1
            }.forEach { oldKey ->
                val exerciseName = oldKey.name.substring(username.length + 1)

                workoutPlan.days.forEachIndexed { dayIndex, day ->
                    if (day.exercises.any { it.name == exerciseName }) {
                        val newKey = stringPreferencesKey("${username}_${dayIndex}_${exerciseName}")
                        preferences[newKey] = "true"
                    }
                }

                preferences.remove(oldKey)
            }
        }

        migrationPerformed = true
    }
}
