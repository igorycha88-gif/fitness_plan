package com.example.fitness_plan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel
import java.lang.System
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState

enum class AdminScreen(val route: String, val label: String, val icon: ImageVector) {
    Home("admin_home", "Главная", Icons.Default.Home),
    Profile("admin_profile", "Профиль", Icons.Default.AccountCircle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    mainNavController: NavHostController,
    profileViewModel: ProfileViewModel? = null,
    workoutViewModel: WorkoutViewModel? = null,
    onExerciseClick: ((com.example.fitness_plan.domain.model.Exercise) -> Unit)? = null
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                AdminScreen.values().forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = AdminScreen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AdminScreen.Home.route) {
                val vm = workoutViewModel ?: androidx.hilt.navigation.compose.hiltViewModel()
                AdminHomeScreen(
                    workoutViewModel = vm,
                    onExerciseClick = onExerciseClick
                )
            }
            composable("admin_login") {
                val vm = androidx.hilt.navigation.compose.hiltViewModel<com.example.fitness_plan.presentation.viewmodel.AdminLoginViewModel>()
                AdminLoginScreen(navController = bottomNavController, viewModel = vm)
            }
            composable(AdminScreen.Profile.route) {
                val vm = profileViewModel ?: androidx.hilt.navigation.compose.hiltViewModel()
                AdminProfileScreen(
                    profileViewModel = vm,
                    onLogout = {
                        vm.logout()
                        mainNavController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanDialog(
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
                    modifier = Modifier.fillMaxWidth()
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
                    if (planName.isNotBlank()) {
                        onConfirm(planName, planDescription)
                    }
                }
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
fun AddDayDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var dayName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить день тренировок") },
        text = {
            OutlinedTextField(
                value = dayName,
                onValueChange = { dayName = it },
                label = { Text("Название дня") },
                placeholder = { Text("Например: День ног") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (dayName.isNotBlank()) {
                        onConfirm(dayName)
                    }
                }
            ) {
                Text("Добавить")
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
    onDismiss: () -> Unit,
    onExerciseSelected: (com.example.fitness_plan.domain.model.Exercise) -> Unit
) {
    val allExercises = remember {
        listOf(
            com.example.fitness_plan.domain.model.Exercise(id = "1", name = "Приседания", sets = 3, reps = "12-15"),
            com.example.fitness_plan.domain.model.Exercise(id = "2", name = "Жим лёжа", sets = 3, reps = "10-12"),
            com.example.fitness_plan.domain.model.Exercise(id = "3", name = "Тяга штанги в наклоне", sets = 3, reps = "10-12"),
            com.example.fitness_plan.domain.model.Exercise(id = "4", name = "Пресс", sets = 3, reps = "15-20"),
            com.example.fitness_plan.domain.model.Exercise(id = "5", name = "Выпады", sets = 3, reps = "12-15"),
            com.example.fitness_plan.domain.model.Exercise(id = "6", name = "Жим гантелей сидя", sets = 3, reps = "10-12"),
            com.example.fitness_plan.domain.model.Exercise(id = "7", name = "Подтягивания", sets = 3, reps = "макс"),
            com.example.fitness_plan.domain.model.Exercise(id = "8", name = "Становая тяга", sets = 3, reps = "8-10"),
            com.example.fitness_plan.domain.model.Exercise(id = "9", name = "Отжимания", sets = 3, reps = "10-15"),
            com.example.fitness_plan.domain.model.Exercise(id = "10", name = "Тяга верхнего блока", sets = 3, reps = "10-12")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбрать упражнение") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                allExercises.forEach { exercise ->
                    Text(
                        text = "${exercise.name} (${exercise.sets}×${exercise.reps})",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExerciseSelected(exercise) }
                            .padding(8.dp)
                    )
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    workoutViewModel: WorkoutViewModel = hiltViewModel(),
    onExerciseClick: ((com.example.fitness_plan.domain.model.Exercise) -> Unit)? = null
) {
    val adminPlan by workoutViewModel.adminWorkoutPlan.collectAsState()

    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var showAddDayDialog by remember { mutableStateOf(false) }
    var showExerciseSelector by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableStateOf(0) }
    var datePickerDayIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создание плана тренировок") }
            )
        },
        floatingActionButton = {
            if (adminPlan == null) {
                ExtendedFloatingActionButton(
                    onClick = { showCreatePlanDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Добавить тренировку") },
                    text = { Text("Добавить тренировку") }
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = { showAddDayDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Добавить день") },
                    text = { Text("Добавить день") }
                )
            }
        }
    ) { paddingValues ->
        if (adminPlan == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Управление тренировочными планами",
                    style = MaterialTheme.typography.headlineMedium
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "План тренировок не создан",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Нажмите кнопку 'Добавить тренировку' чтобы создать новый план",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            AdminPlanListScreen(
                plan = adminPlan!!,
                onExerciseClick = onExerciseClick,
                onAddExercise = { dayIndex ->
                    selectedDayIndex = dayIndex
                    showExerciseSelector = true
                },
                onDeleteDay = { workoutViewModel.removeDayFromAdminPlan(it) },
                onChangeDate = { dayIndex ->
                    datePickerDayIndex = dayIndex
                    showDatePicker = true
                },
                modifier = Modifier.padding(paddingValues)
            )
        }

        if (showCreatePlanDialog) {
            CreatePlanDialog(
                onDismiss = { showCreatePlanDialog = false },
                onConfirm = { name, description ->
                    workoutViewModel.createAdminPlan(name, description)
                    showCreatePlanDialog = false
                }
            )
        }

        if (showAddDayDialog) {
            AddDayDialog(
                onDismiss = { showAddDayDialog = false },
                onConfirm = { dayName ->
                    workoutViewModel.addDayToAdminPlan(dayName)
                    showAddDayDialog = false
                }
            )
        }

        if (showExerciseSelector) {
            ExerciseSelectorDialog(
                onDismiss = { showExerciseSelector = false },
                onExerciseSelected = { exercise ->
                    workoutViewModel.addExerciseToDay(selectedDayIndex, exercise)
                    showExerciseSelector = false
                }
            )
        }

        if (showDatePicker) {
            val selectedDay = adminPlan?.days?.getOrNull(datePickerDayIndex)
            selectedDay?.let { day ->
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = day.scheduledDate ?: System.currentTimeMillis()
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { date ->
                                workoutViewModel.updateDayDate(datePickerDayIndex, date)
                            }
                            showDatePicker = false
                        }) {
                            Text("ОК")
                        }
                    },
                    dismissButton = {
                        Row {
                            TextButton(onClick = {
                                workoutViewModel.updateDayDate(datePickerDayIndex, null)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlanListScreen(
    plan: com.example.fitness_plan.domain.model.WorkoutPlan,
    onExerciseClick: ((com.example.fitness_plan.domain.model.Exercise) -> Unit)?,
    onAddExercise: (Int) -> Unit,
    onDeleteDay: (Int) -> Unit,
    onChangeDate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Заголовок плана
        Text(
            text = plan.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Список дней
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(plan.days.size) { index ->
                val day = plan.days[index]
                AdminWorkoutDayCard(
                    day = day,
                    dayIndex = index,
                    onEditDay = { /* TODO: edit day name */ },
                    onDeleteDay = { onDeleteDay(index) },
                    onChangeDate = { onChangeDate(index) },
                    onAddExercise = { onAddExercise(index) },
                    onExerciseClick = { exerciseName ->
                        val exercise = day.exercises.find { it.name == exerciseName }
                        exercise?.let { onExerciseClick?.invoke(it) }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWorkoutDayCard(
    day: WorkoutDay,
    dayIndex: Int,
    onEditDay: () -> Unit,
    onDeleteDay: () -> Unit,
    onChangeDate: () -> Unit,
    onAddExercise: () -> Unit,
    onExerciseClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок дня с кнопками управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day.dayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onChangeDate) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Изменить дату",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEditDay) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Редактировать день"
                        )
                    }
                    IconButton(onClick = onDeleteDay) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить день",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Дата
            day.scheduledDate?.let { date ->
                Text(
                    text = "Дата: ${formatDate(date)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } ?: Text(
                text = "Дата не установлена",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Группы мышц
            if (day.muscleGroups.isNotEmpty()) {
                Text(
                    text = "Группы мышц: ${day.muscleGroups.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Список упражнений
            if (day.exercises.isEmpty()) {
                Text(
                    text = "Упражнения не добавлены",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Упражнения:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                day.exercises.forEach { exercise ->
                    AdminExerciseCard(
                        exercise = exercise,
                        onClick = { onExerciseClick(exercise.name) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка добавления упражнения
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

@Composable
fun AdminExerciseCard(
    exercise: com.example.fitness_plan.domain.model.Exercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${exercise.sets}×${exercise.reps}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Перейти к упражнению",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    profileViewModel: ProfileViewModel?,
    onLogout: () -> Unit = {}
) {
    val currentUsername by profileViewModel?.currentUsername?.collectAsState() ?: remember { mutableStateOf("") }
    val isAdmin by profileViewModel?.isAdmin?.collectAsState() ?: remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль администратора") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Информация об администраторе",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Имя пользователя: $currentUsername",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Роль: Администратор",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Выход")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выйти из системы")
            }
        }
    }
}
