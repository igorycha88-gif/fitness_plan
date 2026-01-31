package com.example.fitness_plan.domain.model

data class ExerciseLibrary(
    val id: String,
    val name: String,
    val description: String,
    val exerciseType: ExerciseType,
    val equipment: List<EquipmentType>,
    val muscleGroups: List<MuscleGroup>,
    val difficulty: String,
    val stepByStepInstructions: String,
    val animationUrl: String? = null,
    val imageUrl: String? = null,
    val tipsAndAdvice: String? = null,
    val progressionAdvice: String? = null
)
