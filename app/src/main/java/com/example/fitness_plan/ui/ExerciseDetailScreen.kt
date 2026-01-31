package com.example.fitness_plan.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel
import com.example.fitness_plan.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import java.net.URLDecoder
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseName: String,
    dayIndex: Int = -1,
    onBackClick: () -> Unit,
    workoutViewModel: WorkoutViewModel,
    isAdmin: Boolean = false
) {
    val currentWorkoutPlan by workoutViewModel.currentWorkoutPlan.collectAsState()
    val adminWorkoutPlan by workoutViewModel.adminWorkoutPlan.collectAsState()
    val exerciseStats by workoutViewModel.exerciseStats.collectAsState()
    val alternativeLibraryExercises by workoutViewModel.alternativeExercises.collectAsState()
    val completedExercises by workoutViewModel.completedExercises.collectAsState()

    var exercise by remember { mutableStateOf<Exercise?>(null) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    val decodedName = remember(exerciseName) {
        try {
            URLDecoder.decode(exerciseName, "UTF-8")
        } catch (e: Exception) {
            exerciseName
        }
    }

    LaunchedEffect(decodedName, isAdmin) {
        if (isAdmin) {
            workoutViewModel.refreshAdminWorkoutPlan()
        } else {
            workoutViewModel.initializeWorkout()
        }
    }

    LaunchedEffect(decodedName, currentWorkoutPlan, adminWorkoutPlan, isAdmin) {
        val workoutPlan = if (isAdmin) adminWorkoutPlan else currentWorkoutPlan

        if (workoutPlan != null) {
            exercise = findExerciseByName(workoutPlan, decodedName)
            selectedExercise = exercise
            exercise?.let { ex ->
                if (ex.muscleGroups.isNotEmpty()) {
                    workoutViewModel.loadAlternativeExercises(ex.name, ex.muscleGroups, 3)
                }
            }
        }
    }

    val currentExerciseName = selectedExercise?.name ?: decodedName
    val totalSets = selectedExercise?.sets ?: 3
    val isCardioOrStretching = selectedExercise?.exerciseType == com.example.fitness_plan.domain.model.ExerciseType.CARDIO ||
            selectedExercise?.exerciseType == com.example.fitness_plan.domain.model.ExerciseType.STRETCHING

    val exerciseKey = if (dayIndex >= 0) {
        "${dayIndex}_${currentExerciseName}"
    } else {
        currentExerciseName
    }

    val recommendedReps = selectedExercise?.recommendedRepsPerSet?.split(",")?.mapNotNull { it.toIntOrNull() }
        ?: listOf(12, 13, 14)
    var adaptiveWeight by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(currentExerciseName, recommendedReps) {
        adaptiveWeight = workoutViewModel.getAdaptiveWeightForExercise(
            currentExerciseName,
            recommendedReps
        )
    }

    val completedSets = remember(currentExerciseName, exerciseStats) {
        exerciseStats
            .filter { it.exerciseName == currentExerciseName && it.weight > 0 && it.reps > 0 }
            .sortedBy { it.date }
    }

    val isExerciseCompleted = exerciseKey in completedExercises
    val allSetsCompleted = if (isCardioOrStretching) {
        isExerciseCompleted
    } else {
        completedSets.size >= totalSets
    }
    val currentSetNumber = completedSets.size + 1

    var isTimerRunning by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(0) }
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = if (isCardioOrStretching) "Упражнение выполнено" else "Все подходы выполнены",
                                tint = SuccessGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Выполнено",
                                color = SuccessGreen,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
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
                        colors = CardDefaults.cardColors(
                            containerColor = SuccessGreen.copy(alpha = 0.15f),
                            contentColor = SuccessGreen
                        ),
                        border = BorderStroke(2.dp, SuccessGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Выполнено!",
                                    color = SuccessGreen,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (isCardioOrStretching) "Отличная работа!" else "Все подходы завершены!",
                                    color = SuccessGreen,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
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

                val hasAlternatives = alternativeLibraryExercises.isNotEmpty()
                if (hasAlternatives || exercise != null) {
                    Text(
                        text = "Упражнение",
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
                            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(exercise?.name ?: "")
                                        if (hasAlternatives) {
                                            Text(
                                                text = "Текущее упражнение",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedExercise = exercise
                                    expanded = false
                                }
                            )

                            if (hasAlternatives) {
                                HorizontalDivider()

                                alternativeLibraryExercises.forEach { alt ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(alt.name)
                                                Text(
                                                    text = alt.muscleGroups.joinToString(", ") { it.displayName },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedExercise = exercise?.copy(
                                                name = alt.name,
                                                description = alt.description,
                                                muscleGroups = alt.muscleGroups,
                                                equipment = alt.equipment,
                                                exerciseType = alt.exerciseType,
                                                stepByStepInstructions = alt.stepByStepInstructions,
                                                animationUrl = alt.animationUrl
                                            )
                                            expanded = false
                                        }
                                    )
                                }
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

                if (isCardioOrStretching) {
                    Text(
                        text = "Записать результат",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val durationValue = duration.toIntOrNull()
                    val isDurationValid = durationValue != null && durationValue > 0

                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Длительность (мин)") },
                        placeholder = {
                            Text(
                                "Например: 30",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        isError = duration.isNotEmpty() && !isDurationValid,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Статус выполнения",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            selectedExercise?.let {
                                if (!allSetsCompleted && isDurationValid) {
                                    workoutViewModel.saveExerciseStats(
                                        exerciseName = currentExerciseName,
                                        weight = 0.0,
                                        reps = 0,
                                        setNumber = 1,
                                        sets = 1,
                                        duration = durationValue!!
                                    )
                                    workoutViewModel.toggleExerciseCompletion(exerciseKey, true)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !allSetsCompleted && isDurationValid
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (allSetsCompleted) "Упражнение выполнено" else "Отметить как выполненное")
                    }

                    if (!allSetsCompleted) {
                        if (!isDurationValid) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Введите длительность для сохранения",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                workoutViewModel.toggleExerciseCompletion(exerciseKey, false)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Отменить выполнение")
                        }
                    }
                } else {
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
                        val baseRecommendedWeight = selectedExercise?.recommendedWeight
                        val recommendedReps = selectedExercise?.recommendedRepsPerSet

                        val finalRecommendedWeight = adaptiveWeight ?: baseRecommendedWeight

                        val weightPlaceholder = if (weight.isEmpty() && finalRecommendedWeight != null) {
                            val prefix = if (adaptiveWeight != null) "Адаптивно (история)" else "Рекомендуется"
                            "$prefix: ${String.format("%.1f", finalRecommendedWeight)} кг"
                        } else {
                            null
                        }

                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Вес (кг)") },
                            placeholder = if (weightPlaceholder != null) {
                                {
                                    Text(
                                        weightPlaceholder,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            } else null,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
                        )

                        val currentSetReps = recommendedReps?.split(",")?.getOrNull(currentSetNumber - 1)
                        val repsPlaceholder = if (reps.isEmpty() && recommendedReps != null && currentSetReps != null) {
                            "Рекомендуется: $currentSetReps"
                        } else {
                            null
                        }

                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text("Повторения") },
                            placeholder = if (repsPlaceholder != null) {
                                {
                                    Text(
                                        repsPlaceholder,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            } else null,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
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
                                    workoutViewModel.toggleExerciseCompletion(exerciseKey, true)
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
                                    workoutViewModel.toggleExerciseCompletion(exerciseKey, true)
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
                                workoutViewModel.toggleExerciseCompletion(exerciseKey, false)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Отменить выполнение")
                        }
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
