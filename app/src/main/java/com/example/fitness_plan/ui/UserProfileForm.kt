package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.fitness_plan.data.UserProfile
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileForm(
    viewModel: UserProfileViewModel,
    onProfileSaved: () -> Unit
) {
    var goal by remember { mutableStateOf<String?>(null) }
    var level by remember { mutableStateOf<String?>(null) }
    var frequency by remember { mutableStateOf<String?>(null) }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }

    var showSlowWeightLossDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    val goals = listOf("Похудение", "Наращивание мышечной массы", "Поддержание формы")
    val levels = listOf("Новичок", "Любитель", "Профессионал")
    val frequencies = listOf("1 раз в неделю", "3 раза в неделю", "5 раз в неделю")
    val genders = listOf("Мужской", "Женский")

    val showWarning = goal == "Похудение" && frequency == "1 раз в неделю"

    val isValid = goal != null && level != null && frequency != null &&
            weight.isNotEmpty() && height.isNotEmpty() && gender != null

    val weightDouble = weight.toDoubleOrNull()
    val heightDouble = height.toDoubleOrNull()
    val isNumericValid = (weightDouble != null && weightDouble > 0) && (heightDouble != null && heightDouble > 0)

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Расскажи о себе",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            DropdownField(
                label = "Цель тренировок",
                options = goals,
                selectedOption = goal,
                onOptionSelected = { goal = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            DropdownField(
                label = "Уровень подготовки",
                options = levels,
                selectedOption = level,
                onOptionSelected = { level = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            DropdownField(
                label = "Частота тренировок",
                options = frequencies,
                selectedOption = frequency,
                onOptionSelected = { frequency = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Вес (кг)") },
                isError = weight.isNotEmpty() && (weightDouble == null || weightDouble <= 0),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Рост (см)") },
                isError = height.isNotEmpty() && (heightDouble == null || heightDouble <= 0),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            DropdownField(
                label = "Пол",
                options = genders,
                selectedOption = gender,
                onOptionSelected = { gender = it }
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isValid && isNumericValid) {
                        if (showWarning) {
                            showSlowWeightLossDialog = true
                        } else {
                            showScheduleDialog = true
                        }
                    }
                },
                enabled = isValid && isNumericValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить профиль")
            }
        }

        if (showSlowWeightLossDialog) {
            AlertDialog(
                onDismissRequest = { showSlowWeightLossDialog = false },
                title = { Text("Внимание!") },
                text = { Text("Похудение на режиме \"не спеша\". Ты точно этого хотел?") },
                confirmButton = {
                    TextButton(onClick = {
                        showSlowWeightLossDialog = false
                        showScheduleDialog = true
                    }) {
                        Text("Да, продолжить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSlowWeightLossDialog = false }) {
                        Text("Нет, изменить")
                    }
                }
            )
        }

        if (showScheduleDialog) {
            WorkoutScheduleDialog(
                frequency = frequency!!,
                onDismiss = { showScheduleDialog = false },
                onConfirm = { dates ->
                    val username = viewModel.currentUsername.value
                    val profile = UserProfile(
                        username = username,
                        goal = goal!!,
                        level = level!!,
                        frequency = frequency!!,
                        weight = weightDouble!!,
                        height = heightDouble!!,
                        gender = gender!!
                    )
                    viewModel.saveUserProfile(profile)
                    viewModel.saveWorkoutSchedule(dates)
                    onProfileSaved()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScheduleDialog(
    frequency: String,
    onDismiss: () -> Unit,
    onConfirm: (List<Long>) -> Unit
) {
    val trainingCount = when (frequency) {
        "1 раз в неделю" -> 4
        "3 раза в неделю" -> 12
        "5 раз в неделю" -> 20
        else -> 4
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }

    val dates = remember(startDate, frequency) {
        if (startDate != null) {
            calculateWorkoutDates(startDate!!, frequency, trainingCount)
        } else {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Расписание тренировок") },
        text = {
            Column {
                Text(
                    "Выберите дату начала тренировок. Даты будут рассчитаны автоматически.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (startDate != null) formatDate(startDate!!) else "Выберите дату начала"
                    )
                }

                if (dates.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Будет создано $trainingCount тренировок на 4 недели:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dates.take(4).joinToString("\n") { "Неделя 1: ${formatDate(it)}" },
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (dates.size > 4) {
                        Text(
                            "... и ещё ${dates.size - 4} дат",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(dates) },
                enabled = dates.isNotEmpty()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date ->
                        startDate = date
                    }
                    showStartDatePicker = false
                }) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun calculateWorkoutDates(startDate: Long, frequency: String, totalCount: Int): List<Long> {
    val dates = mutableListOf<Long>()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = startDate

    when (frequency) {
        "1 раз в неделю" -> {
            // Same weekday each week
            for (i in 0 until totalCount) {
                dates.add(calendar.timeInMillis)
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        "3 раза в неделю" -> {
            // Start date, then every other day (3 days in first week)
            var count = 0
            while (dates.size < totalCount) {
                dates.add(calendar.timeInMillis)
                count++
                if (count == 3) {
                    // Move to next week, same starting day
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.getInstance().apply {
                        timeInMillis = startDate
                    }.get(Calendar.DAY_OF_WEEK))
                    count = 0
                } else {
                    calendar.add(Calendar.DAY_OF_YEAR, 2) // Every other day
                }
            }
        }
        "5 раз в неделю" -> {
            // 5 consecutive days, then next week
            var weekStart = calendar.clone() as Calendar
            while (dates.size < totalCount) {
                for (dayOffset in 0 until 5) {
                    if (dates.size >= totalCount) break
                    val dayCalendar = weekStart.clone() as Calendar
                    dayCalendar.add(Calendar.DAY_OF_YEAR, dayOffset)
                    dates.add(dayCalendar.timeInMillis)
                }
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                weekStart = calendar.clone() as Calendar
            }
        }
    }

    return dates
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
