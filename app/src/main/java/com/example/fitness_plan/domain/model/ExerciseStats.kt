package com.example.fitness_plan.domain.model

data class ExerciseStats(
    val exerciseName: String,
    val date: Long,
    val weight: Double,
    val reps: Int,
    val setNumber: Int = 1,
    val sets: Int = 1,
    val duration: Int = 0
) {
    val volume: Long
        get() = (weight * reps).toLong()
}

data class DailyExerciseStats(
    val exerciseName: String,
    val date: Long,
    val averageWeight: Double,
    val averageReps: Int,
    val totalSets: Int,
    val exerciseType: ExerciseType,
    val duration: Int = 0
) {
    val volume: Long
        get() = (averageWeight * averageReps * totalSets).toLong()
}

data class ProgressChartData(
    val date: Long,
    val xValue: Double,
    val xLabel: String,
    val yValue: Double,
    val exerciseName: String
)

enum class ProgressTimeFilter(val days: Int, val label: String) {
    DAY(1, "День"),
    MONTH(30, "Месяц"),
    THREE_MONTHS(90, "3 месяца")
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
