package com.example.fitness_plan.domain.model

enum class MuscleGroupSequence(
    val displayName: String,
    val muscleGroups: List<MuscleGroup>,
    val exercisesPerDay: Int
) {
    ARMS("Руки", listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.FOREARMS), 4),
    SHOULDERS("Плечи", listOf(MuscleGroup.SHOULDERS), 4),
    CHEST_BACK("Грудь и Спина", listOf(MuscleGroup.CHEST, MuscleGroup.LATS, MuscleGroup.TRAPS), 4),
    LEGS_CORE("Ноги и Пресс", listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES, MuscleGroup.ABS), 4),
    CARDIO("Кардио", emptyList(), 2);

    companion object {
        val STRENGTH_SEQUENCE = listOf(ARMS, SHOULDERS, CHEST_BACK, LEGS_CORE)
        val FULL_SEQUENCE = listOf(ARMS, SHOULDERS, CHEST_BACK, LEGS_CORE, CARDIO)
    }
}
