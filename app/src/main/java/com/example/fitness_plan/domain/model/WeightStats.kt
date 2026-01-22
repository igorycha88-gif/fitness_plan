package com.example.fitness_plan.domain.model

data class WeightEntry(
    val date: Long,
    val weight: Double
) {
    companion object {
        fun fromTimestampAndWeight(date: Long, weight: Double): WeightEntry {
            return WeightEntry(date, weight)
        }
    }
}

data class WeightStats(
    val startWeight: Double,
    val currentWeight: Double,
    val minWeight: Double,
    val maxWeight: Double,
    val averageWeight: Double,
    val totalChange: Double
) {
    val isWeightLoss: Boolean
        get() = totalChange < 0

    val isWeightGain: Boolean
        get() = totalChange > 0

    val changePercentage: Float
        get() = if (startWeight == 0.0) 0f
                else ((totalChange / startWeight) * 100).toFloat()
}

data class WeightHistory(
    val entries: List<WeightEntry>
) {
    val latestEntry: WeightEntry?
        get() = entries.maxByOrNull { it.date }

    val oldestEntry: WeightEntry?
        get() = entries.minByOrNull { it.date }

    val totalChange: Double
        get() {
            val latest = latestEntry ?: return 0.0
            val oldest = oldestEntry ?: return 0.0
            return latest.weight - oldest.weight
        }

    fun getEntriesForPeriod(days: Int): List<WeightEntry> {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return entries.filter { it.date >= cutoffTime }
    }
}
