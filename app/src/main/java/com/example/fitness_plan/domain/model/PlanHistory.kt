package com.example.fitness_plan.domain.model

data class PlanHistory(
    val username: String,
    val archivedPlans: List<ArchivedPlan>
)

data class ArchivedPlan(
    val id: String,
    val name: String,
    val archivedAt: Long,
    val completedDays: Int,
    val totalDays: Int
)
