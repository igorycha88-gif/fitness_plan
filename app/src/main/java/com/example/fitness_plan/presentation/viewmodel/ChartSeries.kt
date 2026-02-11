package com.example.fitness_plan.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import com.example.fitness_plan.domain.model.BodyParameterType

data class ChartSeries(
    val parameterType: BodyParameterType,
    val data: List<ChartDataPoint>,
    val color: Color,
    val isVisible: Boolean
)

data class ChartDataPoint(
    val date: Long,
    val value: Double,
    val label: String
)
