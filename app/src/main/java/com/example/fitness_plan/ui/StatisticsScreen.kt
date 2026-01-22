package com.example.fitness_plan.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.WeightEntry
import com.example.fitness_plan.presentation.viewmodel.StatisticsViewModel
import com.example.fitness_plan.presentation.viewmodel.TimeFilter
import com.example.fitness_plan.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class StatCategory { WEIGHT, STRENGTH, TRAINING }

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val weightHistory by viewModel.weightHistory.collectAsState()
    val exerciseStats by viewModel.exerciseStats.collectAsState()
    val currentCycle by viewModel.currentCycle.collectAsState()
    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val availableExercises by viewModel.availableExercises.collectAsState()

    var selectedCategory by remember { mutableStateOf(StatCategory.WEIGHT) }

    val adaptiveInfo = rememberAdaptiveInfo()
    val spacing = getSpacing()
    val cornerRadius = getCornerRadius()
    val screenPadding = getScreenPadding()
    val maxWidth = getContentMaxWidth()

    // Расчет адаптивных размеров для StatisticsScreen
    val screenInsets = calculateScreenInsets()

    Scaffold(
        containerColor = BackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Статистика",
                    style = when (adaptiveInfo.deviceType) {
                        DeviceType.COMPACT -> MaterialTheme.typography.headlineSmall
                        DeviceType.MEDIUM -> MaterialTheme.typography.headlineMedium
                        DeviceType.EXPANDED -> MaterialTheme.typography.headlineLarge
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(screenPadding)
                )
            }

            item {
                CategoryTabs(selectedCategory) { selectedCategory = it }
            }

            item {
                when (selectedCategory) {
                    StatCategory.WEIGHT -> WeightTab(
                        viewModel = viewModel,
                        history = weightHistory,
                        adaptiveInfo = adaptiveInfo,
                        spacing = spacing,
                        cornerRadius = cornerRadius,
                        maxWidth = maxWidth
                    )
                    StatCategory.STRENGTH -> StrengthTab(
                        viewModel = viewModel,
                        stats = exerciseStats,
                        availableExercises = availableExercises,
                        adaptiveInfo = adaptiveInfo,
                        spacing = spacing,
                        cornerRadius = cornerRadius,
                        maxWidth = maxWidth
                    )
                    StatCategory.TRAINING -> TrainingTab(
                        viewModel = viewModel,
                        cycle = currentCycle,
                        adaptiveInfo = adaptiveInfo,
                        spacing = spacing,
                        cornerRadius = cornerRadius,
                        maxWidth = maxWidth
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryTabs(selected: StatCategory, onSelect: (StatCategory) -> Unit) {
    val spacing = getSpacing()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(listOf(StatCategory.WEIGHT, StatCategory.STRENGTH, StatCategory.TRAINING)) { cat ->
            CategoryTab(
                category = cat,
                isSelected = selected == cat,
                onClick = { onSelect(cat) }
            )
        }
    }
}

@Composable
fun CategoryTab(
    category: StatCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (icon, label) = when (category) {
        StatCategory.WEIGHT -> Icons.Default.Home to "Вес"
        StatCategory.STRENGTH -> Icons.Default.Build to "Сила"
        StatCategory.TRAINING -> Icons.Default.DateRange to "Тренировки"
    }

    val backgroundColor = if (isSelected) FitnessPrimary else SurfaceLight
    val contentColor = if (isSelected) TextOnPrimary else TextPrimary

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

@Composable
fun WeightTab(
    viewModel: StatisticsViewModel,
    history: List<WeightEntry>,
    adaptiveInfo: AdaptiveInfo,
    spacing: Dp,
    cornerRadius: Dp,
    maxWidth: Dp
) {
    val selectedFilter by viewModel.selectedTimeFilter.collectAsState()
    val filteredHistory = viewModel.getFilteredWeightHistory()
    val weightChange = viewModel.getWeightChange()
    val weightChangeText = viewModel.getWeightChangeText()

    Column(
        modifier = Modifier.width(maxWidth),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        FilterDropdown(
            selectedFilter = selectedFilter,
            onFilterSelected = { viewModel.setTimeFilter(it) },
            cornerRadius = cornerRadius
        )

        if (filteredHistory.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Home,
                title = "Нет данных о весе",
                description = "Начните записывать вес в профиле",
                cornerRadius = cornerRadius
            )
        } else {
            WeightStatsRow(
                history = filteredHistory,
                change = weightChange,
                changeText = weightChangeText,
                adaptiveInfo = adaptiveInfo,
                cornerRadius = cornerRadius
            )

            Spacer(modifier = Modifier.height(spacing))

            WeightChartCard(
                history = filteredHistory,
                filter = selectedFilter,
                cornerRadius = cornerRadius
            )

            Spacer(modifier = Modifier.height(spacing))

            WeightInsights(history = filteredHistory, cornerRadius = cornerRadius)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit,
    cornerRadius: Dp
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        shape = RoundedCornerShape(cornerRadius),
        color = SurfaceLight,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = FitnessPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = selectedFilter.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TimeFilter.values().forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.label) },
                    onClick = {
                        onFilterSelected(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun WeightStatsRow(
    history: List<WeightEntry>,
    change: Double,
    changeText: String,
    adaptiveInfo: AdaptiveInfo,
    cornerRadius: Dp
) {
    val startWeight = history.firstOrNull()?.weight ?: 0.0
    val currentWeight = history.lastOrNull()?.weight ?: 0.0

    val columns = getGridColumns()
    if (columns > 1) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Начальный",
                value = "%.1f кг".format(startWeight),
                color = FitnessTertiary,
                modifier = Modifier.weight(1f),
                cornerRadius = cornerRadius
            )
            StatCard(
                title = "Текущий",
                value = "%.1f кг".format(currentWeight),
                color = FitnessPrimary,
                modifier = Modifier.weight(1f),
                cornerRadius = cornerRadius
            )
            StatCard(
                title = "Изменение",
                value = changeText,
                color = if (change <= 0) SuccessGreen else ErrorRed,
                modifier = Modifier.weight(1f),
                cornerRadius = cornerRadius
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Начальный",
                    value = "%.1f кг".format(startWeight),
                    color = FitnessTertiary,
                    modifier = Modifier.weight(1f),
                    cornerRadius = cornerRadius
                )
                StatCard(
                    title = "Текущий",
                    value = "%.1f кг".format(currentWeight),
                    color = FitnessPrimary,
                    modifier = Modifier.weight(1f),
                    cornerRadius = cornerRadius
                )
            }
            StatCard(
                title = "Изменение",
                value = changeText,
                color = if (change <= 0) SuccessGreen else ErrorRed,
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = cornerRadius
            )
        }
    }
}

@Composable
fun WeightChartCard(
    history: List<WeightEntry>,
    filter: TimeFilter,
    cornerRadius: Dp
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = getCardElevation())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "График веса",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            SimpleLineChart(
                data = history.map { it.weight },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(getChartHeight())
            )
        }
    }
}

@Composable
fun SimpleLineChart(
    data: List<Double>,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Нужно минимум 2 записи",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        return
    }

    val primaryColor = FitnessPrimary
    val gridColor = Color.LightGray.copy(alpha = 0.3f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40f

        val minValue = data.minOrNull()?.toFloat() ?: 0f
        val maxValue = data.maxOrNull()?.toFloat() ?: 100f
        val range = (maxValue - minValue).coerceAtLeast(1f)

        val stepX = (width - 2 * padding) / (data.size - 1).coerceAtLeast(1)

        for (i in 0..4) {
            val y = padding + (height - 2 * padding) * i / 4
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(padding, y),
                end = androidx.compose.ui.geometry.Offset(width - padding, y),
                strokeWidth = 1f
            )
        }

        val path = Path()
        data.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = height - padding - ((value.toFloat() - minValue) / range * (height - 2 * padding))

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3f)
        )

        data.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = height - padding - ((value.toFloat() - minValue) / range * (height - 2 * padding))

            drawCircle(
                color = primaryColor,
                radius = 6f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

@Composable
fun WeightInsights(history: List<WeightEntry>, cornerRadius: Dp) {
    if (history.size < 2) return

    val minWeight = history.minOfOrNull { it.weight } ?: 0.0
    val maxWeight = history.maxOfOrNull { it.weight } ?: 0.0
    val avgWeight = history.map { it.weight }.average()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = InfoBlueLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = InfoBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Аналитика",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InsightItem("Минимум", "%.1f кг".format(minWeight))
                InsightItem("Среднее", "%.1f кг".format(avgWeight))
                InsightItem("Максимум", "%.1f кг".format(maxWeight))
            }
        }
    }
}

@Composable
fun InsightItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = InfoBlue
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrengthTab(
    viewModel: StatisticsViewModel,
    stats: List<ExerciseStats>,
    availableExercises: List<String>,
    adaptiveInfo: AdaptiveInfo,
    spacing: Dp,
    cornerRadius: Dp,
    maxWidth: Dp
) {
    val selectedFilter by viewModel.selectedTimeFilter.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val filteredStats = viewModel.getFilteredExerciseStats()
    val strengthProgress = viewModel.getStrengthProgress()
    val strengthProgressText = viewModel.getStrengthProgressText()

    Column(
        modifier = Modifier.width(maxWidth),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        ExerciseSelector(
            exercises = availableExercises,
            selectedExercise = selectedExercise,
            onExerciseSelected = { viewModel.setSelectedExercise(it) },
            cornerRadius = cornerRadius
        )

        FilterDropdown(
            selectedFilter = selectedFilter,
            onFilterSelected = { viewModel.setTimeFilter(it) },
            cornerRadius = cornerRadius
        )

        if (availableExercises.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Build,
                title = "Нет данных о силе",
                description = "Выполняйте упражнения с указанием веса",
                cornerRadius = cornerRadius
            )
        } else if (filteredStats.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Build,
                title = "Нет данных за период",
                description = "Попробуйте выбрать другой период",
                cornerRadius = cornerRadius
            )
        } else {
            StrengthStatsRow(
                stats = filteredStats,
                progress = strengthProgress,
                progressText = strengthProgressText,
                adaptiveInfo = adaptiveInfo,
                cornerRadius = cornerRadius
            )

            Spacer(modifier = Modifier.height(spacing))

            StrengthChartCard(
                stats = filteredStats,
                exerciseName = selectedExercise,
                cornerRadius = cornerRadius
            )

            Spacer(modifier = Modifier.height(spacing))

            StrengthDetails(stats = filteredStats, cornerRadius = cornerRadius)
        }
    }
}

@Composable
fun ExerciseSelector(
    exercises: List<String>,
    selectedExercise: String,
    onExerciseSelected: (String) -> Unit,
    cornerRadius: Dp
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        shape = RoundedCornerShape(cornerRadius),
        color = SurfaceLight,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = FitnessSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = selectedExercise.ifEmpty { "Выберите упражнение" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise) },
                    onClick = {
                        onExerciseSelected(exercise)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun StrengthStatsRow(
    stats: List<ExerciseStats>,
    progress: Double,
    progressText: String,
    adaptiveInfo: AdaptiveInfo,
    cornerRadius: Dp
) {
    val maxWeight = stats.maxOfOrNull { it.weight } ?: 0.0
    val avgWeight = stats.map { it.weight }.average()

    val columns = getGridColumns()
    if (columns > 1) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Макс вес",
                value = "%.1f кг".format(maxWeight),
                color = FitnessSecondary,
                modifier = Modifier.weight(1f),
                cornerRadius = cornerRadius
            )
            StatCard(
                title = "Среднее",
                value = "%.1f кг".format(avgWeight),
                color = FitnessPrimary,
                modifier = Modifier.weight(1f),
                cornerRadius = cornerRadius
            )
            StatCard(
                title = "Прогресс",
                value = progressText,
                color = if (progress >= 0) SuccessGreen else ErrorRed,
                modifier = Modifier.weight(1f),
                cornerRadius = cornerRadius
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Макс вес",
                    value = "%.1f кг".format(maxWeight),
                    color = FitnessSecondary,
                    modifier = Modifier.weight(1f),
                    cornerRadius = cornerRadius
                )
                StatCard(
                    title = "Среднее",
                    value = "%.1f кг".format(avgWeight),
                    color = FitnessPrimary,
                    modifier = Modifier.weight(1f),
                    cornerRadius = cornerRadius
                )
            }
            StatCard(
                title = "Прогресс",
                value = progressText,
                color = if (progress >= 0) SuccessGreen else ErrorRed,
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = cornerRadius
            )
        }
    }
}

@Composable
fun StrengthChartCard(
    stats: List<ExerciseStats>,
    exerciseName: String,
    cornerRadius: Dp
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = getCardElevation())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Динамика: $exerciseName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            SimpleLineChart(
                data = stats.map { it.weight },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(getChartHeight())
            )
        }
    }
}

@Composable
fun StrengthDetails(stats: List<ExerciseStats>, cornerRadius: Dp) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = getCardElevation())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val totalSets = stats.size
                val totalReps = stats.sumOf { it.reps }
                val volume = stats.sumOf { (it.weight * it.reps).toLong() }

                Column {
                    Text("Подходов", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("$totalSets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FitnessSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Повторений", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("$totalReps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FitnessPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Объём", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("${"%.0f".format(volume)} кг", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FitnessTertiary)
                }
            }
        }
    }
}

@Composable
fun TrainingTab(
    viewModel: StatisticsViewModel,
    cycle: com.example.fitness_plan.domain.model.Cycle?,
    adaptiveInfo: AdaptiveInfo,
    spacing: Dp,
    cornerRadius: Dp,
    maxWidth: Dp
) {
    val cycleProgress = viewModel.getCycleProgress()
    val cycleProgressText = viewModel.getCycleProgressText()

    Column(
        modifier = Modifier.width(maxWidth),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(cornerRadius * 2),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = getCardElevation() + 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(FitnessPrimary.copy(alpha = 0.2f), FitnessSecondary.copy(alpha = 0.2f))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Прогресс цикла",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = cycleProgressText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(FitnessPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(cycleProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextOnPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = cycleProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = FitnessPrimary,
                        trackColor = FitnessPrimary.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrainingStatItem(
                            icon = Icons.Default.DateRange,
                            label = "Дней в цикле",
                            value = "30"
                        )
                        TrainingStatItem(
                            icon = Icons.Default.CheckCircle,
                            label = "Выполнено",
                            value = "${cycle?.daysCompleted ?: 0}"
                        )
                        TrainingStatItem(
                            icon = Icons.Default.Refresh,
                            label = "Осталось",
                            value = "${30 - (cycle?.daysCompleted ?: 0)}"
                        )
                    }
                }
            }
        }

        if (cycle != null && cycle.daysCompleted >= 30) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cornerRadius),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = SuccessGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Цикл завершён!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                        Text(
                            text = "Отличная работа! Начните новый цикл",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        TrainingTips(cornerRadius = cornerRadius)
    }
}

@Composable
fun TrainingStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FitnessPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
fun TrainingTips(cornerRadius: Dp) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = getCardElevation())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = WarningYellow
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Совет",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Тренировка считается выполненной, когда все упражнения отмечены зелёной галочкой.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    cornerRadius: Dp
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(FitnessPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FitnessPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = getCardElevation())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
