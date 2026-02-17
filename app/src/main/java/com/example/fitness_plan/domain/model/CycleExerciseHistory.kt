package com.example.fitness_plan.domain.model

data class CycleExerciseHistory(
    val cycleNumber: Int,
    val startDate: Long,
    val usedExercises: Map<String, Set<String>>,
    val poolId: String
) {
    fun getUsedExercisesForGroup(groupName: String): Set<String> {
        return usedExercises[groupName] ?: emptySet()
    }

    fun getTotalUniqueExercisesUsed(): Int {
        return usedExercises.values.sumOf { it.size }
    }

    fun hasUsedExercise(exerciseName: String): Boolean {
        return usedExercises.values.any { exerciseName in it }
    }
}
