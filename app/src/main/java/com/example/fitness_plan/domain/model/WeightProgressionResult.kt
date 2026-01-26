package com.example.fitness_plan.domain.model

data class WeightProgressionResult(
    val exerciseName: String,
    val oldWeight: Float?,
    val newWeight: Float?,
    val changeType: WeightChangeType,
    val reason: String
)

enum class WeightChangeType {
    INCREASED,
    DECREASED,
    UNCHANGED,
    NO_HISTORY
}