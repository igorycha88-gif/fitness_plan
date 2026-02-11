package com.example.fitness_plan.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness_plan.presentation.viewmodel.ChartSeries
import com.example.fitness_plan.presentation.viewmodel.ChartDataPoint
import kotlin.math.sqrt
import kotlin.math.pow

@Composable
fun BodyParametersChartCard(
    seriesList: List<ChartSeries>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            BodyParametersChart(
                seriesList = seriesList,
                modifier = Modifier.fillMaxSize()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ChartLegend(
                seriesList = seriesList
            )
        }
    }
}

@Composable
fun BodyParametersChart(
    seriesList: List<ChartSeries>,
    modifier: Modifier = Modifier
) {
    if (seriesList.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Нет данных",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var tappedPoint by remember { mutableStateOf<Pair<ChartDataPoint, String>?>(null) }

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "chart_animation"
    )

    Canvas(
        modifier = modifier
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    seriesList.forEach { series ->
                        series.data.forEach { point ->
                            val width = size.width
                            val height = size.height
                            val padding = 40f
                            val chartWidth = width - padding * 2
                            val chartHeight = height - padding * 2

                            val allPoints = seriesList.flatMap { it.data }
                            val minDate = allPoints.minOfOrNull { it.date } ?: 0L
                            val maxDate = allPoints.maxOfOrNull { it.date } ?: 0L
                            val dateRange = maxDate - minDate

                            val x = if (dateRange > 0) {
                                padding + ((point.date - minDate).toFloat() / dateRange) * chartWidth
                            } else {
                                padding + chartWidth / 2
                            }

                            val allValues = seriesList.flatMap { it.data }.map { it.value }
                            val minValue = allValues.minOrNull() ?: 0.0
                            val maxValue = allValues.maxOrNull() ?: 0.0
                            val valueRange = maxValue - minValue

                            val y = if (valueRange > 0) {
                                height - padding - (((point.value - minValue) / valueRange) * chartHeight * animatedProgress).toFloat()
                            } else {
                                height - padding - (chartHeight / 2)
                            }

                            val tapRadius = 20.dp.toPx()
                            val distance = sqrt((offset.x - x).toDouble().pow(2) + (offset.y - y).toDouble().pow(2))

                            if (distance <= tapRadius) {
                                tappedPoint = point to series.parameterType.displayName
                            }
                        }
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val allPoints = seriesList.flatMap { it.data }
        val allValues = seriesList.flatMap { it.data }.map { it.value }

        val minDate = allPoints.minOfOrNull { it.date } ?: 0L
        val maxDate = allPoints.maxOfOrNull { it.date } ?: 0L
        val dateRange = maxDate - minDate

        val minValue = allValues.minOrNull() ?: 0.0
        val maxValue = allValues.maxOrNull() ?: 0.0
        val valueRange = maxValue - minValue

        seriesList.forEach { series ->
            if (series.data.isEmpty()) return@forEach

            val path = Path()

            series.data.forEachIndexed { index, point ->
                val x = if (dateRange > 0) {
                    padding + ((point.date - minDate).toFloat() / dateRange) * chartWidth
                } else {
                    padding + chartWidth / 2
                }

                val normalizedValue = if (valueRange > 0) {
                    ((point.value - minValue) / valueRange)
                } else {
                    0.5
                }
                val animatedY = (chartHeight * normalizedValue * animatedProgress).toFloat()
                val y = height - padding - animatedY

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = series.color,
                style = Stroke(width = 3.dp.toPx())
            )

            series.data.forEach { point ->
                val x = if (dateRange > 0) {
                    padding + ((point.date - minDate).toFloat() / dateRange) * chartWidth
                } else {
                    padding + chartWidth / 2
                }

                val normalizedValue = if (valueRange > 0) {
                    ((point.value - minValue) / valueRange)
                } else {
                    0.5
                }
                val animatedY = (chartHeight * normalizedValue * animatedProgress).toFloat()
                val y = height - padding - animatedY

                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )

                drawCircle(
                    color = series.color,
                    radius = 6.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#6B7280")
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val uniqueDates = allPoints.map { it.date }.distinct().sorted()
        uniqueDates.forEach { date ->
            val x = if (dateRange > 0) {
                padding + ((date - minDate).toFloat() / dateRange) * chartWidth
            } else {
                padding + chartWidth / 2
            }

            val sdf = java.text.SimpleDateFormat("d MMM", java.util.Locale("ru"))
            drawContext.canvas.nativeCanvas.drawText(
                sdf.format(date),
                x,
                height - 5.dp.toPx(),
                textPaint
            )
        }

        if (tappedPoint != null) {
            val (point, name) = tappedPoint!!

            val tooltipPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#1A1A1A")
                alpha = 220
                isAntiAlias = true
            }

            val x = if (dateRange > 0) {
                padding + ((point.date - minDate).toFloat() / dateRange) * chartWidth
            } else {
                padding + chartWidth / 2
            }

            val normalizedValue = if (valueRange > 0) {
                ((point.value - minValue) / valueRange)
            } else {
                0.5
            }
            val animatedY = (chartHeight * normalizedValue * animatedProgress).toFloat()
            val y = height - padding - animatedY

            val tooltipText = "$name: ${String.format("%.1f", point.value)}"
            val tooltipWidth = textPaint.measureText(tooltipText) + 40f
            val tooltipHeight = 60f

            drawContext.canvas.nativeCanvas.drawRoundRect(
                x - tooltipWidth / 2,
                y - tooltipHeight - 20.dp.toPx(),
                x + tooltipWidth / 2,
                y - 20.dp.toPx(),
                8f,
                8f,
                tooltipPaint
            )

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }

            drawContext.canvas.nativeCanvas.drawText(
                tooltipText,
                x,
                y - 35.dp.toPx(),
                textPaint
            )

            drawContext.canvas.nativeCanvas.drawText(
                point.label,
                x,
                y - 10.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#CCCCCC")
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }
    }
}

@Composable
fun ChartLegend(
    seriesList: List<ChartSeries>
) {
    if (seriesList.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Легенда",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        seriesList.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { series ->
                    LegendItem(
                        type = series.parameterType.displayName,
                        unit = series.parameterType.unit,
                        color = series.color,
                        isVisible = series.isVisible
                    )
                }

                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun LegendItem(
    type: String,
    unit: String,
    color: Color,
    isVisible: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(16.dp)
                .padding(end = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = if (isVisible) color else color.copy(alpha = 0.3f),
                    radius = size.minDimension / 2
                )
            }
        }

        Text(
            text = "$type ($unit)",
            style = MaterialTheme.typography.bodySmall,
            color = if (isVisible) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }
        )
    }
}
