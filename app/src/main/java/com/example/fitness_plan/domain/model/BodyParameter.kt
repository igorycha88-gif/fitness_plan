package com.example.fitness_plan.domain.model

import java.util.UUID

data class BodyParameter(
    val parameterType: BodyParameterType,
    val value: Double,
    val unit: String,
    val date: Long,
    val calculationMethod: CalculationMethod = CalculationMethod.MANUAL,
    val measurementId: String = UUID.randomUUID().toString()
)
