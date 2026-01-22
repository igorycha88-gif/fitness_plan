package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel
import com.example.fitness_plan.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseName: String,
    onBackClick: () -> Unit,
    workoutViewModel: WorkoutViewModel
) {
    val currentWorkoutPlan by workoutViewModel.currentWorkoutPlan.collectAsState()
    val exerciseStats by workoutViewModel.exerciseStats.collectAsState()

    var exercise by remember { mutableStateOf<Exercise?>(null) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var alternatives by remember { mutableStateOf<List<Exercise>>(emptyList()) }

    val decodedName = remember(exerciseName) {
        try {
            URLDecoder.decode(exerciseName, "UTF-8")
        } catch (e: Exception) {
            exerciseName
        }
    }

    LaunchedEffect(decodedName) {
        workoutViewModel.initializeWorkout()
    }

    LaunchedEffect(decodedName, currentWorkoutPlan) {
        if (currentWorkoutPlan != null) {
            exercise = findExerciseByName(currentWorkoutPlan!!, decodedName)
            selectedExercise = exercise
            alternatives = exercise?.alternatives ?: emptyList()
        } else {
            val basicExercise = findExerciseInAllAlternatives(decodedName)
            if (basicExercise != null) {
                exercise = basicExercise
                selectedExercise = basicExercise
                alternatives = basicExercise.alternatives
            } else {
                alternatives = emptyList()
            }
        }
    }

    val currentExerciseName = selectedExercise?.name ?: decodedName
    val totalSets = selectedExercise?.sets ?: 3

    val completedSets = remember(currentExerciseName, exerciseStats) {
        exerciseStats
            .filter { it.exerciseName == currentExerciseName && it.weight > 0 && it.reps > 0 }
            .sortedBy { it.date }
    }

    val allSetsCompleted = completedSets.size >= totalSets
    val currentSetNumber = completedSets.size + 1

    var isTimerRunning by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(0) }
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            timerSeconds++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (allSetsCompleted) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Все подходы выполнены",
                            tint = SuccessGreen,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (selectedExercise == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (allSetsCompleted) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = SuccessGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Все подходы выполнены!",
                                color = SuccessGreen,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Описание",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedExercise?.let { "Упражнение для тренировки ${it.name.lowercase()}" } ?: "Описание отсутствует",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoChip(label = "Подходы", value = "$totalSets")
                    InfoChip(label = "Повторения", value = selectedExercise?.reps ?: "10-12")
                    InfoChip(label = "Отдых", value = "60-90 сек")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (alternatives.isNotEmpty()) {
                    Text(
                        text = "Выберите альтернативное упражнение",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = currentExerciseName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Упражнение") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(type = MenuAnchorType.Primary, enabled = true).fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(exercise?.name ?: "")
                                        Text(
                                            text = "Оригинальное упражнение",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedExercise = exercise
                                    expanded = false
                                }
                            )

                            HorizontalDivider()

                            alternatives.forEach { alt ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(alt.name)
                                            Text(
                                                text = "${alt.sets} подходов, ${alt.reps} повторений",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedExercise = alt
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = "Таймер отдыха",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${timerSeconds / 60}:${(timerSeconds % 60).toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.displayMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = { isTimerRunning = !isTimerRunning },
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(if (isTimerRunning) "Пауза" else "Старт")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            timerSeconds = 0
                            isTimerRunning = false
                        }
                    ) {
                        Text("Сброс")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Записать результат",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = "Подход $currentSetNumber из $totalSets${if (allSetsCompleted) " ✓" else ""}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("№ Подхода") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (allSetsCompleted) SuccessGreen else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (allSetsCompleted) SuccessGreen else MaterialTheme.colorScheme.outline,
                        focusedLabelColor = if (allSetsCompleted) SuccessGreen else MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Вес (кг)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Повторения") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        selectedExercise?.let {
                            workoutViewModel.saveExerciseStats(
                                exerciseName = currentExerciseName,
                                weight = weight.toDoubleOrNull() ?: 0.0,
                                reps = reps.toIntOrNull() ?: 0,
                                setNumber = currentSetNumber,
                                sets = totalSets
                            )

                            if (completedSets.size + 1 >= totalSets) {
                                workoutViewModel.toggleExerciseCompletion(currentExerciseName, true)
                            }

                            timerSeconds = 0
                            isTimerRunning = false
                            weight = ""
                            reps = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !allSetsCompleted
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (allSetsCompleted) "Все подходы выполнены" else "Сохранить подход $currentSetNumber")
                }

                if (!allSetsCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            selectedExercise?.let {
                                workoutViewModel.toggleExerciseCompletion(currentExerciseName, true)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Пропустить все оставшиеся подходы")
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            workoutViewModel.toggleExerciseCompletion(currentExerciseName, false)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Отменить выполнение")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun findExerciseByName(plan: WorkoutPlan, name: String): Exercise? {
    for (day in plan.days) {
        for (exercise in day.exercises) {
            if (exercise.name == name) {
                return exercise
            }
        }
    }
    return null
}

private fun findExerciseInAllAlternatives(name: String): Exercise? {
    val allExercises = listOf(
        createExerciseWithAlternatives("1", "Приседания", 3, "12-15"),
        createExerciseWithAlternatives("2", "Жим лёжа", 3, "10-12"),
        createExerciseWithAlternatives("3", "Тяга штанги в наклоне", 3, "10-12"),
        createExerciseWithAlternatives("4", "Пресс", 3, "15-20"),
        createExerciseWithAlternatives("5", "Выпады", 3, "12-15"),
        createExerciseWithAlternatives("6", "Жим гантелей сидя", 3, "10-12"),
        createExerciseWithAlternatives("7", "Подтягивания", 3, "макс"),
        createExerciseWithAlternatives("8", "Становая тяга", 3, "8-10"),
        createExerciseWithAlternatives("9", "Отжимания", 3, "10-15"),
        createExerciseWithAlternatives("10", "Тяга верхнего блока", 3, "10-12")
    )

    return allExercises.find { it.name == name }
}

private fun createExerciseWithAlternatives(id: String, name: String, sets: Int, reps: String): Exercise {
    val alternatives = when (name) {
        "Приседания" -> listOf(
            Exercise(id = "alt1_1", name = "Приседания с гантелями", sets = 3, reps = "12-15", isCompleted = false, alternatives = emptyList()),
            Exercise(id = "alt1_2", name = "Гакк-приседания", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList())
        )
        "Жим лёжа" -> listOf(
            Exercise(id = "alt3_1", name = "Жим на наклонной скамье", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList()),
            Exercise(id = "alt3_2", name = "Жим гантелей лёжа", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList())
        )
        "Тяга штанги в наклоне" -> listOf(
            Exercise(id = "alt5_1", name = "Тяга гантели одной рукой", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList()),
            Exercise(id = "alt5_2", name = "Тяга верхнего блока", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList())
        )
        "Жим гантелей сидя" -> listOf(
            Exercise(id = "alt4_1", name = "Жим Арнольда", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList()),
            Exercise(id = "alt4_2", name = "Подъём гантелей через стороны", sets = 3, reps = "12-15", isCompleted = false, alternatives = emptyList())
        )
        "Тяга верхнего блока" -> listOf(
            Exercise(id = "alt8_1", name = "Тяга верхнего блока", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList()),
            Exercise(id = "alt8_2", name = "Подтягивания с assistance", sets = 3, reps = "макс", isCompleted = false, alternatives = emptyList())
        )
        "Становая тяга" -> listOf(
            Exercise(id = "alt7_1", name = "Румынская тяга", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList()),
            Exercise(id = "alt7_2", name = "Тяга с плинтов", sets = 3, reps = "6-8", isCompleted = false, alternatives = emptyList())
        )
        "Подтягивания" -> listOf(
            Exercise(id = "alt6_1", name = "Тяга штанги в наклоне", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList()),
            Exercise(id = "alt6_2", name = "Подтягивания", sets = 3, reps = "макс", isCompleted = false, alternatives = emptyList())
        )
        "Отжимания" -> listOf(
            Exercise(id = "alt9_1", name = "Отжимания на брусьях", sets = 3, reps = "10-15", isCompleted = false, alternatives = emptyList()),
            Exercise(id = "alt9_2", name = "Жим лёжа", sets = 3, reps = "10-12", isCompleted = false, alternatives = emptyList())
        )
        "Выпады" -> listOf(
            Exercise(id = "alt10_1", name = "Выпады с гантелями", sets = 3, reps = "12-15", isCompleted = false, alternatives = emptyList()),
            Exercise(id = "alt10_2", name = "Сjump squat", sets = 3, reps = "15-20", isCompleted = false, alternatives = emptyList())
        )
        else -> emptyList()
    }
    return Exercise(id = id, name = name, sets = sets, reps = reps, isCompleted = false, alternatives = alternatives)
}
