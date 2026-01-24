package com.example.fitness_plan.domain.model

data class Equipment(
    val type: EquipmentType,
    val imageUrl: String
)

data class MuscleGroupInfo(
    val type: MuscleGroup,
    val imageUrl: String
)
