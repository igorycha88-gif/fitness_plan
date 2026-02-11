package com.example.fitness_plan.domain.model

data class Cycle(
    val cycleNumber: Int,
    val startDate: Long,
    val completedDate: Long? = null,
    val daysCompleted: Int = 0,
    val totalDays: Int = DAYS_IN_CYCLE,
    val completedMicrocycles: Int = 0
) {
    companion object {
        const val DAYS_IN_CYCLE = 30
        const val DAYS_IN_MICROCYCLE = 10
    }

    val isCompleted: Boolean
        get() = completedDate != null || daysCompleted >= totalDays

    val progress: Float
        get() = if (totalDays == 0) 0f else daysCompleted.toFloat() / totalDays

    val remainingDays: Int
        get() = (totalDays - daysCompleted).coerceAtLeast(0)

    val isActive: Boolean
        get() = !isCompleted && startDate > 0
}

data class CycleHistory(
    val entries: List<CycleHistoryEntry>
) {
    val totalCompletedCycles: Int
        get() = entries.size

    val totalTrainingDays: Int
        get() = entries.sumOf { it.daysCompleted }

    val averageCompletionPercentage: Float
        get() = if (entries.isEmpty()) 0f
                else entries.map { it.daysCompleted.toFloat() / Cycle.DAYS_IN_CYCLE }.average().toFloat()
}

data class CycleHistoryEntry(
    val cycleNumber: Int,
    val startDate: Long,
    val completedDate: Long,
    val daysCompleted: Int
) {
    val completionPercentage: Float
        get() = if (Cycle.DAYS_IN_CYCLE == 0) 0f
                else daysCompleted.toFloat() / Cycle.DAYS_IN_CYCLE
}
