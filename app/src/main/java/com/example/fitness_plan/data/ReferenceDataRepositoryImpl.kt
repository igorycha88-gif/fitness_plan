package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.model.Equipment
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.model.MuscleGroupInfo
import com.example.fitness_plan.domain.repository.ReferenceDataRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.referenceDataStore: DataStore<Preferences> by preferencesDataStore(name = "reference_data")

@Singleton
class ReferenceDataRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : ReferenceDataRepository {

    private val equipmentKey = stringPreferencesKey("equipment_list")
    private val muscleGroupsKey = stringPreferencesKey("muscle_groups_list")

    override fun getAllEquipment(): Flow<List<Equipment>> {
        return context.referenceDataStore.data.map { preferences ->
            val json = preferences[equipmentKey]
            if (json != null) {
                try {
                    gson.fromJson(json, object : TypeToken<List<Equipment>>() {}.type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                getInitialEquipment()
            }
        }
    }

    override fun getEquipmentByType(type: EquipmentType): Flow<Equipment?> {
        return getAllEquipment().map { equipmentList ->
            equipmentList.find { it.type == type }
        }
    }

    override fun getAllMuscleGroups(): Flow<List<MuscleGroupInfo>> {
        return context.referenceDataStore.data.map { preferences ->
            val json = preferences[muscleGroupsKey]
            if (json != null) {
                try {
                    gson.fromJson(json, object : TypeToken<List<MuscleGroupInfo>>() {}.type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                getInitialMuscleGroups()
            }
        }
    }

    override fun getMuscleGroupByType(type: MuscleGroup): Flow<MuscleGroupInfo?> {
        return getAllMuscleGroups().map { muscleGroupsList ->
            muscleGroupsList.find { it.type == type }
        }
    }

    suspend fun initializeReferenceData() {
        val equipment = getInitialEquipment()
        val muscleGroups = getInitialMuscleGroups()

        context.referenceDataStore.edit { preferences ->
            preferences[equipmentKey] = gson.toJson(equipment)
            preferences[muscleGroupsKey] = gson.toJson(muscleGroups)
        }
    }

    private fun getInitialEquipment(): List<Equipment> {
        return EquipmentType.values().toList().map { type ->
            Equipment(
                type = type,
                imageUrl = "file:///android_asset/equipment/${type.imageName}.png"
            )
        }
    }

    private fun getInitialMuscleGroups(): List<MuscleGroupInfo> {
        return MuscleGroup.values().toList().map { group ->
            MuscleGroupInfo(
                type = group,
                imageUrl = "file:///android_asset/muscles/${group.imageName}.png"
            )
        }
    }
}
