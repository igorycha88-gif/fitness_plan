package com.example.fitness_plan.domain.model

import kotlin.math.roundToInt

enum class MuscleGroupStatsFilter(val days: Int, val label: String) {
    DAY(1, "День"),
    WEEK(7, "Неделя"),
    MONTH(30, "Месяц"),
    THREE_MONTHS(90, "3 месяца"),
    ALL(0, "Всё время")
}

data class MuscleGroupStats(
    val muscleGroup: MuscleGroup,
    val exerciseName: String,
    val date: Long,
    val weight: Double,
    val reps: Int,
    val setNumber: Int = 1
) {
    val volume: Long
        get() = (weight * reps).toLong()
}

data class MuscleGroupSummary(
    val muscleGroup: MuscleGroup,
    val totalVolume: Long,
    val totalSets: Int,
    val exerciseCount: Int,
    val maxWeight: Double,
    val percentage: Float,
    val daysSinceLastWorkout: Int,
    val averageWeeklyFrequency: Float,
    val exerciseNames: List<String>
) {
    val isFresh: Boolean
        get() = daysSinceLastWorkout <= 3

    val needsAttention: Boolean
        get() = daysSinceLastWorkout > 7

    val isOverdue: Boolean
        get() = daysSinceLastWorkout > 14
}

data class MuscleGroupProgress(
    val muscleGroup: MuscleGroup,
    val date: Long,
    val volume: Long,
    val sets: Int,
    val maxWeight: Double
)

data class MuscleGroupTimeSeries(
    val date: Long,
    val volume: Long,
    val sets: Int,
    val exercises: Int
)

data class MuscleGroupDetail(
    val muscleGroup: MuscleGroup,
    val progress: List<MuscleGroupProgress>,
    val recentExercises: List<MuscleGroupExerciseInfo>,
    val averageWeeklyFrequency: Float,
    val totalVolume: Long,
    val totalSets: Int,
    val totalExercises: Int,
    val maxWeight: Double,
    val lastWorkoutDate: Long
)

data class MuscleGroupExerciseInfo(
    val exerciseName: String,
    val date: Long,
    val weight: Double,
    val reps: Int,
    val volume: Long
) {
    fun formatDate(): String {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale("ru"))
        return sdf.format(java.util.Date(date))
    }
}