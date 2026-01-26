package com.example.fitness_plan.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.WeightChartData

@Composable
fun WeightChart(
    chartData: WeightChartData,
    modifier: Modifier = Modifier,
    primaryColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Gray
) {
    if (chartData.isEmpty) {
        return
    }

    val minWeight = chartData.dataPoints.minOf { it.weight }.toFloat() * 0.95f
    val maxWeight = chartData.dataPoints.maxOf { it.weight }.toFloat() * 1.05f
    val weightRange = maxWeight - minWeight

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

        val stepX = usableWidth / (chartData.dataPoints.size - 1)
        val points = chartData.dataPoints.mapIndexed { index, point ->
            val x = padding + index * stepX
            val y = canvasHeight - padding - ((point.weight.toFloat() - minWeight) / weightRange * usableHeight)
            Offset(x, y)
        }

        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, points.first().y)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
            },
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )

        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = point
            )
        }
    }
}