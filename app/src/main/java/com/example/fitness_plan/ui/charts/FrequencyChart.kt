package com.example.fitness_plan.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.FrequencyChartData

@Composable
fun FrequencyChart(
    chartData: FrequencyChartData,
    modifier: Modifier = Modifier,
    tertiaryColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Gray
) {
    if (chartData.isEmpty) {
        return
    }

    val maxWorkouts = chartData.dataPoints.maxOf { it.workoutsCount }.toFloat() * 1.2f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 20.dp.toPx()

        val usableWidth = canvasWidth - padding * 2
        val usableHeight = canvasHeight - padding * 2

        val barWidth = (usableWidth / chartData.dataPoints.size) * 0.7f
        val barSpacing = (usableWidth / chartData.dataPoints.size) * 0.3f

        chartData.dataPoints.forEachIndexed { index, point ->
            val x = padding + index * (barWidth + barSpacing)
            val barHeight = (point.workoutsCount.toFloat() / maxWorkouts) * usableHeight
            val y = canvasHeight - padding - barHeight

            drawRoundRect(
                color = tertiaryColor,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
        }
    }
}