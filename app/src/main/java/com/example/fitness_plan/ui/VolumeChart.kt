package com.example.fitness_plan.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness_plan.domain.model.VolumeEntry
import com.example.fitness_plan.presentation.viewmodel.VolumeTimeFilter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VolumeChartCard(
    volumeData: List<VolumeEntry>,
    selectedFilter: com.example.fitness_plan.presentation.viewmodel.VolumeTimeFilter,
    onBarClick: (VolumeEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        VolumeChart(
            volumeData = volumeData,
            selectedFilter = selectedFilter,
            onBarClick = onBarClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun VolumeChart(
    volumeData: List<VolumeEntry>,
    selectedFilter: com.example.fitness_plan.presentation.viewmodel.VolumeTimeFilter,
    onBarClick: (VolumeEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    if (volumeData.isEmpty()) {
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

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val volumes = volumeData.map { it.volume.toFloat() }
    val maxVolume = volumes.maxOrNull() ?: 0f
    val volumeRange = maxVolume

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "volume_chart_animation"
    )

    var tappedBar by remember { mutableStateOf<VolumeEntry?>(null) }

    Canvas(
        modifier = modifier
            .padding(16.dp)
            .pointerInput(volumeData) {
                detectTapGestures { offset ->
                    val width = size.width
                    val height = size.height
                    val padding = 40f
                    val chartWidth = width - padding * 2
                    val chartHeight = height - padding * 2

                    val stepX = if (volumeData.size > 1) {
                        chartWidth / volumeData.size
                    } else {
                        chartWidth / 2
                    }

                    val barWidth = (stepX * 0.6f)

                    volumeData.forEachIndexed { index, entry ->
                        val x = padding + index * stepX + (stepX - barWidth) / 2
                        val barRect = Rect(
                            left = x,
                            top = padding,
                            right = x + barWidth,
                            bottom = height - padding
                        )

                        if (offset.x >= barRect.left && offset.x <= barRect.right &&
                            offset.y >= barRect.top && offset.y <= barRect.bottom) {
                            tappedBar = entry
                            onBarClick(entry)
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

        val stepX = if (volumeData.size > 1) {
            chartWidth / volumeData.size
        } else {
            chartWidth / 2
        }

        val barWidth = (stepX * 0.6f).coerceAtMost(60f)
        val barSpacing = (stepX - barWidth) / 2

        volumeData.forEachIndexed { index, entry ->
            val x = padding + index * stepX + barSpacing
            val normalizedVolume = if (volumeRange > 0) {
                entry.volume.toFloat() / volumeRange
            } else {
                0.5f
            }
            val animatedHeight = (chartHeight * normalizedVolume * animatedProgress)
            val barHeight = animatedHeight.coerceAtLeast(4.dp.toPx())
            val y = height - padding - barHeight

            val gradient = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.8f),
                    tertiaryColor.copy(alpha = 0.6f)
                ),
                startY = y,
                endY = height - padding
            )

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )

            val isSelected = tappedBar == entry
            if (isSelected) {
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#6B7280")
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            val labelText = when (selectedFilter) {
                VolumeTimeFilter.DAY -> {
                    formatVolumeDateHour(entry.date)
                }
                VolumeTimeFilter.WEEK -> {
                    formatVolumeDateShort(entry.date)
                }
                VolumeTimeFilter.MONTH,
                VolumeTimeFilter.YEAR -> {
                    formatVolumeDateWeek(entry.date)
                }
            }

            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                x + barWidth / 2,
                height - 5.dp.toPx(),
                paint
            )

            val volumeText = formatVolume(entry.volume)
            val volumePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#2DD4BF")
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }

            if (isSelected || volumeData.size <= 8) {
                drawContext.canvas.nativeCanvas.drawText(
                    volumeText,
                    x + barWidth / 2,
                    padding - 10.dp.toPx(),
                    volumePaint
                )
            }
        }

        val path = Path()
        path.moveTo(padding, height - padding)
        path.lineTo(width - padding, height - padding)

        drawPath(
            path = path,
            color = Color(0xFFE5E7EB),
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )
        )
    }
}

fun formatVolume(volume: Long): String {
    return if (volume >= 1000000) {
        "%.1fM".format(volume / 1000000.0)
    } else if (volume >= 1000) {
        "%.1fK".format(volume / 1000.0)
    } else {
        volume.toString()
    }
}

fun formatVolumeDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("d MMM", Locale("ru"))
    return sdf.format(Date(timestamp))
}

fun formatVolumeDateHour(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    return "${hour}:00"
}

fun formatVolumeDateWeek(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = SimpleDateFormat("MMM", Locale("ru")).format(Date(timestamp))
    return "$day $month"
}
