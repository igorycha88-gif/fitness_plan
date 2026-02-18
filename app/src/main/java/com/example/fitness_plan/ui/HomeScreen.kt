package com.example.fitness_plan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel
import com.example.fitness_plan.ui.theme.SuccessGreen
import com.example.fitness_plan.ui.theme.FitnessSecondaryLight
import java.text.SimpleDateFormat
import java.util.*

// Интеллектуальная адаптация размеров верхней и нижней областей
@Composable
fun calculateAdaptiveInsets(
    availableHeight: Dp? = null,
    contentHeight: Dp? = null,
    isScrollable: Boolean = false
): ScreenInsets {
    val configuration = LocalConfiguration.current

    // Базовые размеры системных элементов (приближенные значения)
    val statusBarHeight = 24.dp // Примерное значение status bar
    val navigationBarHeight = 48.dp // Примерное значение navigation bar

    // Размеры экрана
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Интеллектуальные коэффициенты адаптации
    val sizeMultiplier = when {
        screenHeight < 600.dp -> 0.75f  // Очень маленькие экраны (компактные)
        screenHeight < 800.dp -> 0.85f  // Маленькие экраны
        screenHeight > 1000.dp -> 1.15f // Большие экраны
        screenHeight > 1200.dp -> 1.25f // Очень большие экраны
        else -> 1.0f
    }

    val aspectRatioMultiplier = when {
        screenWidth / screenHeight > 0.7f -> 1.2f  // Широкие экраны (планшеты)
        screenWidth / screenHeight > 0.6f -> 1.1f  // Широкие экраны
        screenWidth / screenHeight < 0.45f -> 0.8f  // Узкие экраны (старые телефоны)
        screenWidth / screenHeight < 0.5f -> 0.9f  // Узкие экраны
        else -> 1.0f
    }

    // Динамическая адаптация на основе доступного пространства
    val availableSpaceMultiplier = when {
        availableHeight != null && contentHeight != null -> {
            val availableRatio = availableHeight / screenHeight
            when {
                availableRatio < 0.6f -> 0.8f  // Мало места
                availableRatio > 0.8f -> 1.1f  // Много места
                else -> 1.0f
            }
        }
        else -> 1.0f
    }

    // Учитываем скролл - если контент скроллится, уменьшаем insets
    val scrollMultiplier = if (isScrollable) 0.9f else 1.0f

    val finalMultiplier = sizeMultiplier * aspectRatioMultiplier * availableSpaceMultiplier * scrollMultiplier

    // Расчет верхнего inset (status bar + top content padding)
    val baseTopInset = (statusBarHeight + 32.dp) * finalMultiplier
    val topInset = baseTopInset.coerceIn(24.dp, 120.dp)

    // Расчет нижнего inset (navigation bar + bottom content padding)
    val baseBottomInset = (navigationBarHeight + 48.dp) * finalMultiplier
    val bottomInset = baseBottomInset.coerceIn(32.dp, 140.dp)

    return ScreenInsets(
        topInset = topInset,
        bottomInset = bottomInset
    )
}

// Упрощенная версия для обратной совместимости
@Composable
fun calculateScreenInsets(): ScreenInsets = calculateAdaptiveInsets()

data class ScreenInsets(
    val topInset: Dp,
    val bottomInset: Dp
)

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
    onExerciseClick: (Exercise, Int) -> Unit = { _, _ -> }
) {
    val workoutPlan by viewModel.currentWorkoutPlan.collectAsState()
    val userWorkoutPlan by viewModel.userWorkoutPlan.collectAsState()
    val exerciseStats by viewModel.exerciseStats.collectAsState()
    val completedExercises by viewModel.completedExercises.collectAsState()
    val completedDays by viewModel.completedDays.collectAsState()
    val partiallyCompletedDays by viewModel.partiallyCompletedDays.collectAsState()
    val selectedPlanType by viewModel.selectedPlanType.collectAsState()
    val isAutoPlanExpanded by viewModel.isAutoPlanExpanded.collectAsState()
    val isUserPlanExpanded by viewModel.isUserPlanExpanded.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val exerciseLibraryViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.example.fitness_plan.presentation.viewmodel.ExerciseLibraryViewModel>()
    val exerciseLibrary by exerciseLibraryViewModel.exercises.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeWorkout()
    }

    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var showAddDayDialog by remember { mutableStateOf(false) }
    var showExerciseSelector by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableStateOf(0) }
    var showDeletePlanDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isExpandedScreen = screenWidthDp >= 600

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("План") },
                actions = {
                    IconButton(onClick = { /* TODO: notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Уведомления")
                    }
                    IconButton(onClick = { /* TODO: profile */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Профиль")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading && workoutPlan == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator()
                Text(
                    "Загрузка плана...",
                    modifier = Modifier.padding(top = if (isExpandedScreen) 60.dp else 40.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                PlanTypeSelector(
                    selectedType = selectedPlanType,
                    onTypeSelected = { type ->
                        viewModel.setSelectedPlanType(type)
                    },
                    userPlanExists = userWorkoutPlan != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedPlanType) {
                    com.example.fitness_plan.domain.repository.SelectedPlanType.AUTO -> {
                        if (workoutPlan != null) {
                            AutoPlanSection(
                                plan = workoutPlan!!,
                                exerciseStats = exerciseStats,
                                completedExercises = completedExercises,
                                completedDays = completedDays,
                                partiallyCompletedDays = partiallyCompletedDays,
                                isExpanded = isAutoPlanExpanded,
                                onToggleExpanded = { viewModel.toggleAutoPlanExpanded() },
                                onExerciseClick = onExerciseClick,
                                onExerciseToggle = { name, completed ->
                                    viewModel.toggleExerciseCompletion(name, completed)
                                },
                                onDateChange = { dayIndex, date ->
                                    viewModel.updateWorkoutDayDate(dayIndex, date)
                                },
                                isExpandedScreen = isExpandedScreen
                            )
                        }
                    }
                    com.example.fitness_plan.domain.repository.SelectedPlanType.CUSTOM -> {
                        if (userWorkoutPlan != null) {
                            UserPlanSection(
                                plan = userWorkoutPlan!!,
                                exerciseStats = exerciseStats,
                                completedExercises = completedExercises,
                                completedDays = completedDays,
                                partiallyCompletedDays = partiallyCompletedDays,
                                isExpanded = isUserPlanExpanded,
                                onToggleExpanded = { viewModel.toggleUserPlanExpanded() },
                                onExerciseClick = onExerciseClick,
                                onExerciseToggle = { name, completed ->
                                    viewModel.toggleExerciseCompletion(name, completed)
                                },
                                onAddDay = { showAddDayDialog = true },
                                onDeletePlan = { showDeletePlanDialog = true },
                                onAddExercise = { dayIndex ->
                                    selectedDayIndex = dayIndex
                                    showExerciseSelector = true
                                },
                                onDateChange = { dayIndex, date ->
                                    viewModel.updateUserWorkoutDayDate(dayIndex, date)
                                },
                                isExpandedScreen = isExpandedScreen
                            )
                        } else {
                            CreateUserPlanPrompt(onCreatePlan = { showCreatePlanDialog = true })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showCreatePlanDialog) {
        CreateUserPlanDialog(
            onDismiss = { showCreatePlanDialog = false },
            onConfirm = { name, description ->
                viewModel.createUserPlan(name, description)
                showCreatePlanDialog = false
            }
        )
    }

    if (showAddDayDialog) {
        AddDayDialog(
            onDismiss = { showAddDayDialog = false },
            onConfirm = { dayName ->
                viewModel.addDayToUserPlan(dayName)
                showAddDayDialog = false
            }
        )
    }

    if (showExerciseSelector) {
        ExerciseSelectorDialog(
            exerciseLibrary = exerciseLibrary,
            onDismiss = { showExerciseSelector = false },
            onExerciseSelected = { exercise ->
                viewModel.addExerciseToUserDay(selectedDayIndex, exercise)
                showExerciseSelector = false
            }
        )
    }

    if (showDeletePlanDialog) {
        DeletePlanConfirmationDialog(
            onDismiss = { showDeletePlanDialog = false },
            onConfirm = {
                viewModel.deleteUserPlan()
                showDeletePlanDialog = false
            }
        )
    }
}

@Composable
fun AdaptivePlanDetailsScreen(
    plan: WorkoutPlan,
    exerciseStats: List<ExerciseStats>,
    completedExercises: Set<String>,
    completedDays: Set<Int>,
    partiallyCompletedDays: Set<Int>,
    onExerciseClick: (Exercise, Int) -> Unit,
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
                val isPartiallyCompleted = index in partiallyCompletedDays
                val backgroundColor = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isCompleted -> SuccessGreen.copy(alpha = 0.1f)
                    isPartiallyCompleted -> FitnessSecondaryLight.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surface
                }
                val borderColor = when {
                    isSelected -> null
                    isCompleted -> BorderStroke(1.dp, SuccessGreen)
                    isPartiallyCompleted -> BorderStroke(1.dp, FitnessSecondaryLight)
                    else -> null
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedDayIndex = index },
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    border = borderColor
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
                                text = "✓ Выполнено",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessGreen
                            )
                        } else if (isPartiallyCompleted) {
                            val dayExerciseKeys = day.exercises.map { "${index}_${it.name}" }.toSet()
                            val completedCount = dayExerciseKeys.count { it in completedExercises }
                            Text(
                                text = "○ $completedCount/${day.exercises.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = FitnessSecondaryLight
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
                val isDayCompleted = selectedDayIndex in completedDays
                val isDayPartiallyCompleted = selectedDayIndex in partiallyCompletedDays
                val dayBackgroundColor = when {
                    isDayCompleted -> SuccessGreen.copy(alpha = 0.15f)
                    isDayPartiallyCompleted -> FitnessSecondaryLight.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = dayBackgroundColor)
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
                        if (isDayCompleted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ Выполнено",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SuccessGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else if (isDayPartiallyCompleted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val dayExerciseKeys = day.exercises.map { "${selectedDayIndex}_${it.name}" }.toSet()
                            val completedCount = dayExerciseKeys.count { it in completedExercises }
                            Text(
                                text = "○ $completedCount/${day.exercises.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FitnessSecondaryLight,
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
                            .padding(vertical = 4.dp)
                            .clickable { onExerciseClick(exercise, selectedDayIndex) },
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
    partiallyCompletedDays: Set<Int>,
    onExerciseClick: (Exercise, Int) -> Unit,
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
            partiallyCompletedDays = partiallyCompletedDays,
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
    partiallyCompletedDays: Set<Int>,
    onExerciseClick: (Exercise, Int) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Int, Long?) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        days.forEachIndexed { index, day ->
            val isCompleted = index in completedDays
            val isPartiallyCompleted = index in partiallyCompletedDays
            WorkoutDayCard(
                day = day,
                dayIndex = index,
                isCompleted = isCompleted,
                isPartiallyCompleted = isPartiallyCompleted,
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
    isPartiallyCompleted: Boolean = false,
    completedExercises: Set<String>,
    exerciseStats: List<ExerciseStats>,
    onExerciseClick: (Exercise, Int) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Long?) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isCompleted -> SuccessGreen.copy(alpha = 0.15f)
        isPartiallyCompleted -> FitnessSecondaryLight.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isCompleted -> BorderStroke(2.dp, SuccessGreen)
        isPartiallyCompleted -> BorderStroke(1.dp, FitnessSecondaryLight)
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = borderColor
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
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (isPartiallyCompleted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val dayExerciseKeys = day.exercises.map { "${dayIndex}_${it.name}" }.toSet()
                        val completedCount = dayExerciseKeys.count { it in completedExercises }
                        Text(
                            text = "○ $completedCount/${day.exercises.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FitnessSecondaryLight,
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
                        onClick = onExerciseClick,
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
    onClick: (Exercise, Int) -> Unit = { _, _ -> },
    onToggle: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val isAllSetsDone = isAllSetsCompleted(exercise.name, exercise.sets, exerciseStats)
    val showCheckmark = isAllSetsDone || isCompleted

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onClick(exercise, dayIndex) },
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (showCheckmark) SuccessGreen else MaterialTheme.colorScheme.onSurface
                        )
                        if (exercise.isFavoriteSubstitution) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Избранное упражнение",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
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
    }

    if (!isLast) {
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun PlanTypeSelector(
    selectedType: com.example.fitness_plan.domain.repository.SelectedPlanType,
    onTypeSelected: (com.example.fitness_plan.domain.repository.SelectedPlanType) -> Unit,
    userPlanExists: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PlanTypeButton(
            text = "Автоматический",
            isSelected = selectedType == com.example.fitness_plan.domain.repository.SelectedPlanType.AUTO,
            onClick = { onTypeSelected(com.example.fitness_plan.domain.repository.SelectedPlanType.AUTO) },
            modifier = Modifier.weight(1f)
        )
        PlanTypeButton(
            text = "Мой план",
            isSelected = selectedType == com.example.fitness_plan.domain.repository.SelectedPlanType.CUSTOM,
            onClick = { onTypeSelected(com.example.fitness_plan.domain.repository.SelectedPlanType.CUSTOM) },
            modifier = Modifier.weight(1f),
            enabled = true
        )
    }
}

@Composable
fun PlanTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoPlanSection(
    plan: WorkoutPlan,
    exerciseStats: List<ExerciseStats>,
    completedExercises: Set<String>,
    completedDays: Set<Int>,
    partiallyCompletedDays: Set<Int>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onExerciseClick: (Exercise, Int) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onDateChange: ((Int, Long?) -> Unit)? = null,
    isExpandedScreen: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Автоматический план",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (isExpanded) "-" else "+",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                WorkoutDaysList(
                    days = plan.days,
                    exerciseStats = exerciseStats,
                    completedExercises = completedExercises,
                    completedDays = completedDays,
                    partiallyCompletedDays = partiallyCompletedDays,
                    onExerciseClick = onExerciseClick,
                    onExerciseToggle = onExerciseToggle,
                    onDateChange = onDateChange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPlanSection(
    plan: WorkoutPlan,
    exerciseStats: List<ExerciseStats>,
    completedExercises: Set<String>,
    completedDays: Set<Int>,
    partiallyCompletedDays: Set<Int>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onExerciseClick: (Exercise, Int) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onAddDay: () -> Unit,
    onDeletePlan: () -> Unit,
    onAddExercise: (Int) -> Unit,
    onDateChange: ((Int, Long?) -> Unit)? = null,
    isExpandedScreen: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Мой план",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (plan.description.isNotEmpty()) {
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDeletePlan,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить план",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = if (isExpanded) "-" else "+",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onAddDay,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить день")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (plan.days.isEmpty()) {
                    Text(
                        text = "Дни не добавлены",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    plan.days.forEachIndexed { dayIndex, day ->
                        UserWorkoutDayCard(
                            day = day,
                            dayIndex = dayIndex,
                            isCompleted = dayIndex in completedDays,
                            isPartiallyCompleted = dayIndex in partiallyCompletedDays,
                            completedExercises = completedExercises,
                            exerciseStats = exerciseStats,
                            onExerciseClick = onExerciseClick,
                            onExerciseToggle = onExerciseToggle,
                            onAddExercise = { onAddExercise(dayIndex) },
                            onDateChange = onDateChange?.let { callback -> { date -> callback(dayIndex, date) } }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserWorkoutDayCard(
    day: WorkoutDay,
    dayIndex: Int,
    isCompleted: Boolean,
    isPartiallyCompleted: Boolean,
    completedExercises: Set<String>,
    exerciseStats: List<ExerciseStats>,
    onExerciseClick: (Exercise, Int) -> Unit,
    onExerciseToggle: (String, Boolean) -> Unit,
    onAddExercise: () -> Unit,
    onDateChange: ((Long?) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isCompleted -> SuccessGreen.copy(alpha = 0.15f)
        isPartiallyCompleted -> FitnessSecondaryLight.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
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
                    val muscleGroupsText = day.muscleGroups.takeIf { it.isNotEmpty() }
                        ?.joinToString(", ")
                        ?: "не указаны"
                    Text(
                        text = "Группы мышц: $muscleGroupsText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isCompleted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Выполнено",
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (isPartiallyCompleted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val dayExerciseKeys = day.exercises.map { "${dayIndex}_${it.name}" }.toSet()
                        val completedCount = dayExerciseKeys.count { it in completedExercises }
                        Text(
                            text = "○ $completedCount/${day.exercises.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FitnessSecondaryLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${day.exercises.size} упр.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isExpanded) "-" else "+",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                day.exercises.forEachIndexed { index, exercise ->
                    val exerciseKey = "${dayIndex}_${exercise.name}"
                    val isExerciseCompleted = exerciseKey in completedExercises
                    ExerciseRow(
                        exercise = exercise,
                        isCompleted = isExerciseCompleted,
                        isLast = index == day.exercises.lastIndex,
                        dayIndex = dayIndex,
                        exerciseStats = exerciseStats,
                        onClick = onExerciseClick,
                        onToggle = { completed -> onExerciseToggle(exerciseKey, completed) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onAddExercise,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить упражнение")
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
fun CreateUserPlanPrompt(onCreatePlan: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Мой план пока не создан",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Создайте свой индивидуальный план тренировок",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreatePlan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать свой план")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserPlanDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var planName by remember { mutableStateOf("") }
    var planDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать план тренировок") },
        text = {
            Column {
                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Название плана") },
                    placeholder = { Text("Минимум 3 символа") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (planName.length < 3 && planName.isNotEmpty()) "Минимум 3 символа" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = planDescription,
                    onValueChange = { planDescription = it },
                    label = { Text("Описание плана") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (planName.length >= 3) {
                        onConfirm(planName, planDescription)
                    }
                },
                enabled = planName.length >= 3
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectorDialog(
    exerciseLibrary: List<com.example.fitness_plan.domain.model.ExerciseLibrary>,
    onDismiss: () -> Unit,
    onExerciseSelected: (com.example.fitness_plan.domain.model.Exercise) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    val filteredExercises = remember(searchText, exerciseLibrary) {
        if (searchText.isEmpty()) {
            exerciseLibrary
        } else {
            exerciseLibrary.filter { it.name.contains(searchText, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбрать упражнение") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Поиск") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(filteredExercises) { exercise ->
                        Text(
                            text = exercise.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val exerciseObj = Exercise(
                                        id = exercise.id,
                                        name = exercise.name,
                                        sets = 3,
                                        reps = "10-12",
                                        description = exercise.description,
                                        muscleGroups = exercise.muscleGroups,
                                        equipment = exercise.equipment,
                                        exerciseType = exercise.exerciseType,
                                        stepByStepInstructions = exercise.stepByStepInstructions,
                                        animationUrl = exercise.animationUrl,
                                        imageRes = exercise.imageRes,
                                        imageUrl = exercise.imageUrl
                                    )
                                    onExerciseSelected(exerciseObj)
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun DeletePlanConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить план?") },
        text = {
            Column {
                Text("Вы уверены, что хотите удалить свой план тренировок?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Это действие нельзя отменить. Все добавленные дни и упражнения будут удалены.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
