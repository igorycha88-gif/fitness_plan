package com.example.fitness_plan.domain.model

data class WeightChartData(
    val dataPoints: List<WeightDataPoint>,
    val overallStats: WeightStats
) {
    val isEmpty: Boolean
        get() = dataPoints.isEmpty()
}

data class WeightDataPoint(
    val date: Long,
    val weight: Double
)

data class VolumeChartData(
    val dataPoints: List<VolumeDataPoint>,
    val totalVolume: Long
) {
    val isEmpty: Boolean
        get() = dataPoints.isEmpty()
}

data class VolumeDataPoint(
    val date: Long,
    val volume: Long,
    val workoutsCount: Int
)

data class FrequencyChartData(
    val dataPoints: List<FrequencyDataPoint>,
    val totalWorkouts: Int
) {
    val isEmpty: Boolean
        get() = dataPoints.isEmpty()
}

data class FrequencyDataPoint(
    val date: Long,
    val workoutsCount: Int
)

data class OverallStats(
    val currentWeight: Double,
    val weightChange: Double,
    val weightChangePercentage: Float,
    val totalVolume: Long,
    val totalWorkouts: Int,
    val cycleProgress: Float,
    val cycleDaysCompleted: Int,
    val cycleTotalDays: Int,
    val periodDays: Int
) {
    val isWeightGain: Boolean
        get() = weightChange > 0

    val isWeightLoss: Boolean
        get() = weightChange < 0

    val weightChangeText: String
        get() = when {
            weightChange > 0 -> "+%.1f кг".format(weightChange)
            weightChange < 0 -> "%.1f кг".format(weightChange)
            else -> "Без изменений"
        }

    val volumeText: String
        get() = formatVolume(totalVolume)

    val cycleProgressText: String
        get() = "$cycleDaysCompleted/$cycleTotalDays"

    companion object {
        fun formatVolume(volume: Long): String {
            return when {
                volume >= 1_000_000 -> "%.1fM".format(volume / 1_000_000.0)
                volume >= 1_000 -> "%.1fK".format(volume / 1_000.0)
                else -> "$volume"
            }
        }
    }
}