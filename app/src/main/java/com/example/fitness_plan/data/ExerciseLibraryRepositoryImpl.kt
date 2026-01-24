package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.exerciseLibraryDataStore: DataStore<Preferences> by preferencesDataStore(name = "exercise_library")

@Singleton
class ExerciseLibraryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : ExerciseLibraryRepository {

    private val exercisesKey = stringPreferencesKey("exercises_list")

    override fun getAllExercises(): Flow<List<ExerciseLibrary>> {
        return context.exerciseLibraryDataStore.data.map { preferences ->
            val json = preferences[exercisesKey]
            if (json != null) {
                try {
                    gson.fromJson(json, object : TypeToken<List<ExerciseLibrary>>() {}.type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    override fun getExercisesByType(type: ExerciseType): Flow<List<ExerciseLibrary>> {
        return getAllExercises().map { exercises ->
            exercises.filter { it.exerciseType == type }
        }
    }

    override fun getExercisesByEquipment(equipment: List<EquipmentType>): Flow<List<ExerciseLibrary>> {
        return getAllExercises().map { exercises ->
            exercises.filter { exercise ->
                equipment.any { equip -> exercise.equipment.contains(equip) }
            }
        }
    }

    override fun getExercisesByMuscleGroups(muscleGroups: List<MuscleGroup>): Flow<List<ExerciseLibrary>> {
        return getAllExercises().map { exercises ->
            exercises.filter { exercise ->
                muscleGroups.any { muscle -> exercise.muscleGroups.contains(muscle) }
            }
        }
    }

    override fun searchExercises(query: String): Flow<List<ExerciseLibrary>> {
        return getAllExercises().map { exercises ->
            exercises.filter { exercise ->
                exercise.name.lowercase().contains(query.lowercase()) ||
                exercise.description.lowercase().contains(query.lowercase())
            }
        }
    }

    override suspend fun addExercise(exercise: ExerciseLibrary) {
        val currentExercises = getAllExercises().first().toMutableList()
        currentExercises.add(exercise)

        context.exerciseLibraryDataStore.edit { preferences ->
            preferences[exercisesKey] = gson.toJson(currentExercises)
        }
    }

    override suspend fun deleteExercise(exerciseId: String) {
        val currentExercises = getAllExercises().first().toMutableList()
        currentExercises.removeAll { it.id == exerciseId }

        context.exerciseLibraryDataStore.edit { preferences ->
            preferences[exercisesKey] = gson.toJson(currentExercises)
        }
    }

    override suspend fun getExerciseById(id: String): ExerciseLibrary? {
        return getAllExercises().first().find { it.id == id }
    }
}
