package com.example.fitness_plan.domain.model

data class ExerciseStats(
    val exerciseName: String,
    val date: Long,
    val weight: Double,
    val reps: Int,
    val setNumber: Int = 1,
    val sets: Int = 1
) {
    val volume: Long
        get() = (weight * reps).toLong()
}

data class ExerciseProgress(
    val exerciseName: String,
    val startDate: Long,
    val endDate: Long,
    val maxWeight: Double,
    val minWeight: Double,
    val averageWeight: Double,
    val totalVolume: Long,
    val totalSets: Int,
    val totalReps: Int,
    val weightChange: Double,
    val weightChangePercentage: Float
) {
    val isProgressImproved: Boolean
        get() = weightChange > 0
}

data class ExerciseSummary(
    val exerciseName: String,
    val maxWeight: Double,
    val averageWeight: Double,
    val totalVolume: Long,
    val totalSets: Int,
    val completionPercentage: Float
)
