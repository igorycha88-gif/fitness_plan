package com.example.fitness_plan.domain.model

data class SmartwatchData(
    val timestamp: Long,
    val heartRate: Int?,
    val heartRateMin: Int?,
    val heartRateMax: Int?,
    val heartRateAvg: Int?,
    val caloriesBurned: Double?,
    val steps: Int?,
    val distance: Double?,
    val activeDuration: Long,
    val heartRateZone: HeartRateZone?
) {
    companion object {
        fun empty(timestamp: Long = System.currentTimeMillis()): SmartwatchData {
            return SmartwatchData(
                timestamp = timestamp,
                heartRate = null,
                heartRateMin = null,
                heartRateMax = null,
                heartRateAvg = null,
                caloriesBurned = null,
                steps = null,
                distance = null,
                activeDuration = 0L,
                heartRateZone = null
            )
        }
    }

    val isActive: Boolean
        get() = heartRate != null || caloriesBurned != null

    fun formatDuration(): String {
        val totalSeconds = activeDuration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "${minutes}:${seconds.toString().padStart(2, '0')}"
    }
}

enum class HeartRateZone(val displayName: String, val minPercentage: Double, val maxPercentage: Double) {
    REST("Отдых", 0.0, 0.50),
    FAT_BURN("Жиросжигание", 0.50, 0.60),
    CARDIO("Кардио", 0.60, 0.70),
    HARD("Интенсивная", 0.70, 0.80),
    MAXIMUM("Максимальная", 0.80, 0.90),
    EXTREME("Экстремальная", 0.90, 1.0);

    companion object {
        fun fromHeartRate(heartRate: Int?, age: Int): HeartRateZone? {
            if (heartRate == null || age <= 0) return null
            
            val maxHr = 220 - age
            val percentage = heartRate.toDouble() / maxHr
            
            return values().find { zone ->
                percentage >= zone.minPercentage && percentage < zone.maxPercentage
            } ?: EXTREME
        }
    }
}

data class SmartwatchSessionSummary(
    val startTime: Long,
    val endTime: Long,
    val avgHeartRate: Int?,
    val minHeartRate: Int?,
    val maxHeartRate: Int?,
    val totalCalories: Double?,
    val totalSteps: Int?,
    val totalDistance: Double?,
    val totalDuration: Long,
    val dominantHeartRateZone: HeartRateZone?,
    val heartRateZoneDistribution: Map<HeartRateZone, Int>
) {
    val durationFormatted: String
        get() {
            val totalSeconds = totalDuration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "${minutes}:${seconds.toString().padStart(2, '0')}"
        }
}
