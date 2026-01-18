package com.example.fitness_plan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.data.Exercise
import com.example.fitness_plan.data.ExerciseStats
import com.example.fitness_plan.data.Week
import com.example.fitness_plan.data.WorkoutDay
import com.example.fitness_plan.data.WorkoutPlan
import java.text.SimpleDateFormat
import java.util.*

enum class DayCompletionStatus {
    COMPLETED,  // All exercises completed - GREEN
    PARTIAL,    // Some exercises completed - YELLOW
    NONE        // No exercises completed - GRAY/WHITE
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onExerciseClick: (Exercise) -> Unit = {}
) {
    val workoutPlan by viewModel.currentWorkoutPlan.collectAsState()
    val exerciseStats by viewModel.exerciseStats.collectAsState()
    val completedExercises by viewModel.completedExercises.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Мой план тренировок") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)) {

            if (workoutPlan == null) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                    Text("Загрузка плана...", modifier = Modifier.padding(top = 40.dp))
                }
            } else {
                PlanDetailsScreen(
                    plan = workoutPlan!!,
                    exerciseStats = exerciseStats,
                    completedExercises = completedExercises,
                    onExerciseClick = onExerciseClick,
                    onExerciseToggle = { exerciseName, completed ->
                        viewModel.toggleExerciseCompletion(exerciseName, completed)
                    },
                    onDateChange = { dayIndex, date ->
                        viewModel.updateWorkoutDayDate(dayIndex, date)
                    }
                )
            }
        }
    }
}

@Composable
fun PlanDetailsScreen(
    plan: WorkoutPlan,
    exerciseStats: List<ExerciseStats>,
    completedExercises: Set<String>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Int, Long?) -> Unit)? = null
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(text = plan.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = plan.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "Группы мышц: ${plan.muscleGroups.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (plan.weeks.isNotEmpty()) {
            WeekPager(
                weeks = plan.weeks,
                exerciseStats = exerciseStats,
                completedExercises = completedExercises,
                onExerciseClick = onExerciseClick,
                onExerciseToggle = onExerciseToggle,
                onDateChange = onDateChange
            )
        } else if (plan.days.isNotEmpty()) {
            WorkoutDaysList(
                days = plan.days,
                exerciseStats = exerciseStats,
                completedExercises = completedExercises,
                onExerciseClick = onExerciseClick,
                onExerciseToggle = onExerciseToggle,
                onDateChange = onDateChange
            )
        } else {
            Text("План пуст", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekPager(
    weeks: List<Week>,
    exerciseStats: List<ExerciseStats>,
    completedExercises: Set<String>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Int, Long?) -> Unit)? = null
) {
    var selectedWeekIndex by remember { mutableStateOf(0) }

    Column {
        TabRow(selectedTabIndex = selectedWeekIndex) {
            weeks.forEachIndexed { index, week ->
                Tab(
                    selected = selectedWeekIndex == index,
                    onClick = { selectedWeekIndex = index },
                    text = { Text("Неделя ${week.weekNumber}") }
                )
            }
        }

        WeekContent(
            week = weeks[selectedWeekIndex],
            exerciseStats = exerciseStats,
            completedExercises = completedExercises,
            onExerciseClick = onExerciseClick,
            onExerciseToggle = onExerciseToggle,
            onDateChange = onDateChange
        )
    }
}

@Composable
fun WeekContent(
    week: Week,
    exerciseStats: List<ExerciseStats>,
    completedExercises: Set<String>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Int, Long?) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Неделя ${week.weekNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = week.focus,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = week.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Тренировочные дни:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutDaysList(
            days = week.workoutPlan.days,
            exerciseStats = exerciseStats,
            completedExercises = completedExercises,
            onExerciseClick = onExerciseClick,
            onExerciseToggle = onExerciseToggle,
            onDateChange = onDateChange
        )
    }
}

@Composable
fun WorkoutDaysList(
    days: List<WorkoutDay>,
    exerciseStats: List<ExerciseStats>,
    completedExercises: Set<String>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Int, Long?) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        days.forEachIndexed { index, day ->
            val completionStatus = getDayCompletionStatus(day, completedExercises, exerciseStats)
            WorkoutDayCard(
                day = day,
                completionStatus = completionStatus,
                completedExercises = completedExercises,
                exerciseStats = exerciseStats,
                onExerciseClick = onExerciseClick,
                onExerciseToggle = onExerciseToggle,
                onDateChange = if (onDateChange != null) { date -> onDateChange(index, date) } else null
            )
        }
    }
}

fun getDayCompletionStatus(day: WorkoutDay, completedExercises: Set<String>, exerciseStats: List<ExerciseStats>): DayCompletionStatus {
    if (day.exercises.isEmpty()) return DayCompletionStatus.NONE

    val completedCount = day.exercises.count { exercise ->
        val hasManualCompletion = completedExercises.contains(exercise.name)
        val hasStats = exerciseStats.any { it.exerciseName == exercise.name }
        hasManualCompletion || hasStats
    }

    return when {
        completedCount == day.exercises.size -> DayCompletionStatus.COMPLETED
        completedCount > 0 -> DayCompletionStatus.PARTIAL
        else -> DayCompletionStatus.NONE
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDayCard(
    day: WorkoutDay,
    completionStatus: DayCompletionStatus,
    completedExercises: Set<String>,
    exerciseStats: List<ExerciseStats>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Long?) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val backgroundColor = when (completionStatus) {
        DayCompletionStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        DayCompletionStatus.PARTIAL -> Color(0xFFFFEB3B).copy(alpha = 0.3f)
        DayCompletionStatus.NONE -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (completionStatus != DayCompletionStatus.NONE) {
            BorderStroke(1.dp, when (completionStatus) {
                DayCompletionStatus.COMPLETED -> Color(0xFF4CAF50)
                DayCompletionStatus.PARTIAL -> Color(0xFFFFEB3B)
                DayCompletionStatus.NONE -> Color.Transparent
            })
        } else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = day.dayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                    Text(text = "Группы мышц: ${day.muscleGroups.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusText = when (completionStatus) {
                        DayCompletionStatus.COMPLETED -> "✓"
                        DayCompletionStatus.PARTIAL -> "◐"
                        DayCompletionStatus.NONE -> ""
                    }
                    val statusColor = when (completionStatus) {
                        DayCompletionStatus.COMPLETED -> Color(0xFF4CAF50)
                        DayCompletionStatus.PARTIAL -> Color(0xFFFF9800)
                        DayCompletionStatus.NONE -> Color.Transparent
                    }
                    if (statusText.isNotEmpty()) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isExpanded) "-" else "+")
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                day.exercises.forEachIndexed { index, exercise ->
                    val hasManualCompletion = completedExercises.contains(exercise.name)
                    val hasStats = exerciseStats.any { it.exerciseName == exercise.name }
                    val isCompleted = hasManualCompletion || hasStats
                    ExerciseRow(
                        exercise = exercise,
                        isCompleted = isCompleted,
                        isLast = index == day.exercises.lastIndex,
                        onClick = { onExerciseClick(exercise) },
                        onToggle = { completed -> onExerciseToggle(exercise.name, completed) }
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
    onClick: () -> Unit = {},
    onToggle: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current

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
                    IconButton(
                    onClick = { onToggle(!isCompleted) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = if (isCompleted) "Отменить выполнение" else "Отметить как выполненное",
                        tint = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${exercise.sets} сетов", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = exercise.reps, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        exercise.imageRes?.let { resName ->
            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = exercise.name,
                    modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp)
                )
            }
        }
    }

    if (!isLast) {
        Divider(modifier = Modifier.padding(top = 4.dp))
    }
}
