package com.example.fitness_plan.domain.repository

import com.example.fitness_plan.domain.model.Equipment
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.model.MuscleGroupInfo
import kotlinx.coroutines.flow.Flow

interface ReferenceDataRepository {
    fun getAllEquipment(): Flow<List<Equipment>>
    fun getEquipmentByType(type: EquipmentType): Flow<Equipment?>
    
    fun getAllMuscleGroups(): Flow<List<MuscleGroupInfo>>
    fun getMuscleGroupByType(type: MuscleGroup): Flow<MuscleGroupInfo?>
}
