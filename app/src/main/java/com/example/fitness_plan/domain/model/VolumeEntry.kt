package com.example.fitness_plan.domain.model

data class VolumeEntry(
    val date: Long,
    val volume: Long,
    val exerciseCount: Int,
    val stats: List<ExerciseStats>
)

data class WorkoutSummary(
    val exerciseName: String,
    val totalVolume: Long,
    val sets: Int,
    val reps: Int,
    val weight: Double
) {
    val volume: Long
        get() = (weight * reps).toLong()
}
