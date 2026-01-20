package com.example.fitness_plan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.*

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun isAllSetsCompleted(exerciseName: String, totalSets: Int, exerciseStats: List<ExerciseStats>): Boolean {
    val completedSets = exerciseStats
        .filter { it.exerciseName == exerciseName && it.weight > 0 && it.reps > 0 }
        .size
    return completedSets >= totalSets
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onExerciseClick: (Exercise) -> Unit = {}
) {
    val workoutPlan by viewModel.currentWorkoutPlan.collectAsState()
    val exerciseStats by viewModel.exerciseStats.collectAsState()
    val completedExercises by viewModel.completedExercises.collectAsState()
    val completedDays by viewModel.completedDays.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeWorkout()
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isExpandedScreen = screenWidthDp >= 600

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Уведомления")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Person, contentDescription = "Профиль")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (workoutPlan == null || isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            ) {
                CircularProgressIndicator()
                Text(
                    "Загрузка плана...",
                    modifier = Modifier.padding(top = if (isExpandedScreen) 60.dp else 40.dp)
                )
            }
        } else {
            if (isExpandedScreen) {
                AdaptivePlanDetailsScreen(
                    plan = workoutPlan!!,
                    exerciseStats = exerciseStats,
                    completedExercises = completedExercises,
                    completedDays = completedDays,
                    onExerciseClick = onExerciseClick,
                    onExerciseToggle = { name, completed ->
                        viewModel.toggleExerciseCompletion(name, completed)
                    },
                    onDateChange = { dayIndex, date ->
                        viewModel.updateWorkoutDayDate(dayIndex, date)
                    },
                    modifier = Modifier.padding(paddingValues).padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                )
            } else {
                PlanDetailsScreen(
                    plan = workoutPlan!!,
                    exerciseStats = exerciseStats,
                    completedExercises = completedExercises,
                    completedDays = completedDays,
                    onExerciseClick = onExerciseClick,
                    onExerciseToggle = { name, completed ->
                        viewModel.toggleExerciseCompletion(name, completed)
                    },
                    onDateChange = { dayIndex, date ->
                        viewModel.updateWorkoutDayDate(dayIndex, date)
                    },
                    modifier = Modifier.padding(paddingValues).padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                )
            }
        }
    }
}

@Composable
fun AdaptivePlanDetailsScreen(
    plan: WorkoutPlan,
    exerciseStats: List<ExerciseStats>,
    completedExercises: Set<String>,
    completedDays: Set<Int>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: (Int, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableStateOf(0) }

    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Группы мышц: ${plan.muscleGroups.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Дни",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            plan.days.forEachIndexed { index, day ->
                val isSelected = selectedDayIndex == index
                val isCompleted = index in completedDays
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedDayIndex = index },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else if (isCompleted)
                            Color(0xFF2DD4BF).copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = if (isCompleted && !isSelected) {
                        BorderStroke(1.dp, Color(0xFF2DD4BF))
                    } else null
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = day.dayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "${day.exercises.size} упражнений",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isCompleted) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2DD4BF)
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .padding(vertical = 16.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        Column(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            val selectedDay = plan.days.getOrNull(selectedDayIndex)
            selectedDay?.let { day ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedDayIndex in completedDays)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = day.dayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Группы мышц: ${day.muscleGroups.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${day.exercises.size} упражнений",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (selectedDayIndex in completedDays) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ Выполнено",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2DD4BF),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Упражнения:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                day.exercises.forEachIndexed { index, exercise ->
                    val exerciseKey = "${selectedDayIndex}_${exercise.name}"
                    val isExerciseCompleted = exerciseKey in completedExercises
                    val isAllSetsDone = isAllSetsCompleted(exercise.name, exercise.sets, exerciseStats)
                    val showCheckmark = isAllSetsDone || isExerciseCompleted
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (showCheckmark)
                                SuccessGreen.copy(alpha = 0.08f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (showCheckmark) {
                            BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                        } else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (showCheckmark) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Выполнено",
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Не выполнено",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = exercise.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (showCheckmark) SuccessGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${exercise.sets} сетов",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = exercise.reps,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanDetailsScreen(
    plan: WorkoutPlan,
    exerciseStats: List<ExerciseStats>,
    completedExercises: Set<String>,
    completedDays: Set<Int>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Int, Long?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = plan.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Группы мышц: ${plan.muscleGroups.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Тренировки:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        WorkoutDaysList(
            days = plan.days,
            exerciseStats = exerciseStats,
            completedExercises = completedExercises,
            completedDays = completedDays,
            onExerciseClick = onExerciseClick,
            onExerciseToggle = onExerciseToggle,
            onDateChange = onDateChange
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun WorkoutDaysList(
    days: List<WorkoutDay>,
    exerciseStats: List<ExerciseStats>,
    completedExercises: Set<String>,
    completedDays: Set<Int>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Int, Long?) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        days.forEachIndexed { index, day ->
            val isCompleted = index in completedDays
            WorkoutDayCard(
                day = day,
                dayIndex = index,
                isCompleted = isCompleted,
                completedExercises = completedExercises,
                exerciseStats = exerciseStats,
                onExerciseClick = onExerciseClick,
                onExerciseToggle = onExerciseToggle,
                onDateChange = if (onDateChange != null) { date -> onDateChange(index, date) } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDayCard(
    day: WorkoutDay,
    dayIndex: Int = 0,
    isCompleted: Boolean = false,
    completedExercises: Set<String>,
    exerciseStats: List<ExerciseStats>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Long?) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isCompleted -> Color(0xFF2DD4BF).copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isCompleted) {
            BorderStroke(2.dp, Color(0xFF2DD4BF))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = day.dayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        day.scheduledDate?.let { date ->
                            Text(
                                text = formatDate(date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (onDateChange != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Filled.DateRange,
                                    contentDescription = "Изменить дату",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Text(
                        text = "Группы мышц: ${day.muscleGroups.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isCompleted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Выполнено",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2DD4BF),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${day.exercises.size} упр.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isExpanded) "-" else "+",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                day.exercises.forEachIndexed { index, exercise ->
                    val exerciseKey = "${dayIndex}_${exercise.name}"
                    val isExerciseCompleted = exerciseKey in completedExercises
                    ExerciseRow(
                        exercise = exercise,
                        isCompleted = isExerciseCompleted,
                        isLast = index == day.exercises.lastIndex,
                        dayIndex = dayIndex,
                        exerciseStats = exerciseStats,
                        onClick = { onExerciseClick(exercise) },
                        onToggle = { completed -> onExerciseToggle(exerciseKey, completed) }
                    )
                }
            }
        }
    }

    if (showDatePicker && onDateChange != null) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = day.scheduledDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date ->
                        onDateChange(date)
                    }
                    showDatePicker = false
                }) {
                    Text("ОК")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        onDateChange(null)
                        showDatePicker = false
                    }) {
                        Text("Очистить")
                    }
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Отмена")
                    }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ExerciseRow(
    exercise: Exercise,
    isCompleted: Boolean,
    isLast: Boolean,
    dayIndex: Int = 0,
    exerciseStats: List<ExerciseStats> = emptyList(),
    onClick: () -> Unit = {},
    onToggle: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val isAllSetsDone = isAllSetsCompleted(exercise.name, exercise.sets, exerciseStats)
    val showCheckmark = isAllSetsDone || isCompleted

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showCheckmark) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Выполнено",
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Не выполнено",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (showCheckmark) SuccessGreen else MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${exercise.sets} сетов",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = exercise.reps,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        exercise.imageRes?.let { resName ->
            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = exercise.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 8.dp)
                )
            }
        }
    }

    if (!isLast) {
        Divider(modifier = Modifier.padding(top = 4.dp))
    }
}
