package com.example.fitness_plan.presentation.viewmodel

import com.example.fitness_plan.domain.model.BodyParameterType

data class ParameterOption(
    val type: BodyParameterType,
    val lastValue: Double?,
    val lastDate: Long?,
    val hasData: Boolean
)
