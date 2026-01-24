package com.example.fitness_plan.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsetsSides
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

data class DropdownOption(
    val label: String,
    val icon: ImageVector,
    val description: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileForm(
    viewModel: ProfileViewModel,
    onProfileSaved: () -> Unit,
    onBackClick: () -> Unit = {},
    username: String = ""
) {
    var goal by remember { mutableStateOf<String?>(null) }
    var level by remember { mutableStateOf<String?>(null) }
    var frequency by remember { mutableStateOf<String?>(null) }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }

    var showSlowWeightLossDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    val goalOptions = listOf(
        DropdownOption("Похудение", Icons.Filled.Star, "Снижение веса и жировой массы"),
        DropdownOption("Наращивание мышечной массы", Icons.Filled.Add, "Увеличение мышечной массы"),
        DropdownOption("Поддержание формы", Icons.Filled.Check, "Сохранение текущей формы")
    )

    val levelOptions = listOf(
        DropdownOption("Новичок", Icons.Filled.Close, "Начальный уровень"),
        DropdownOption("Любитель", Icons.Filled.Clear, "Есть базовый опыт"),
        DropdownOption("Профессионал", Icons.Filled.Edit, "Продвинутый уровень")
    )

    val frequencyOptions = listOf(
        DropdownOption("1 раз в неделю", Icons.Filled.Refresh, "Лёгкий режим"),
        DropdownOption("3 раза в неделю", Icons.Filled.Settings, "Стандартный режим"),
        DropdownOption("5 раз в неделю", Icons.Filled.ThumbUp, "Интенсивный режим")
    )

    val genderOptions = listOf(
        DropdownOption("Мужской", Icons.Filled.Person, "Для мужчин"),
        DropdownOption("Женский", Icons.Filled.Person, "Для женщин")
    )

    val showWarning = goal == "Похудение" && frequency == "1 раз в неделю"

    val isValid = goal != null && level != null && frequency != null &&
            weight.isNotEmpty() && height.isNotEmpty() && gender != null

    val weightDouble = weight.toDoubleOrNull()
    val heightDouble = height.toDoubleOrNull()
    val isNumericValid = (weightDouble != null && weightDouble > 0) && (heightDouble != null && heightDouble > 0)

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расскажите о себе") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            var expandedGoal by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedGoal,
                onExpandedChange = { expandedGoal = !expandedGoal }
            ) {
            OutlinedTextField(
                value = goal ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Цель тренировок") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGoal) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )
                ExposedDropdownMenu(
                    expanded = expandedGoal,
                    onDismissRequest = { expandedGoal = false }
                ) {
                    goalOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                goal = option.label
                                expandedGoal = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            var expandedLevel by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedLevel,
                onExpandedChange = { expandedLevel = !expandedLevel }
            ) {
                OutlinedTextField(
                    value = level ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Уровень подготовки") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLevel) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedLevel,
                    onDismissRequest = { expandedLevel = false }
                ) {
                    levelOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                level = option.label
                                expandedLevel = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            var expandedFrequency by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedFrequency,
                onExpandedChange = { expandedFrequency = !expandedFrequency }
            ) {
                OutlinedTextField(
                    value = frequency ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Частота тренировок") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrequency) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedFrequency,
                    onDismissRequest = { expandedFrequency = false }
                ) {
                    frequencyOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                frequency = option.label
                                expandedFrequency = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Вес (кг)") },
                isError = weight.isNotEmpty() && (weightDouble == null || weightDouble <= 0),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Рост (см)") },
                isError = height.isNotEmpty() && (heightDouble == null || heightDouble <= 0),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            var expandedGender by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedGender,
                onExpandedChange = { expandedGender = !expandedGender }
            ) {
                OutlinedTextField(
                    value = gender ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Пол") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedGender,
                    onDismissRequest = { expandedGender = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                gender = option.label
                                expandedGender = false
                            }
                        )
                    }
                }
            }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    "Сохранить профиль",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showScheduleDialog) {
            WorkoutScheduleDialog(
                frequency = frequency ?: "",
                onDismiss = { showScheduleDialog = false },
                onConfirm = { dates ->
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
                    viewModel.saveWorkoutDates(dates)
                    showScheduleDialog = false
                    onProfileSaved()
                }
            )
        }
    }
}

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { onClick() })
        }
    )
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (startDate != null) formatDate(startDate!!) else "Выберите дату начала",
                        style = MaterialTheme.typography.bodyMedium
                    )
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
        },
        containerColor = MaterialTheme.colorScheme.surface
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
            for (i in 0 until totalCount) {
                dates.add(calendar.timeInMillis)
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        "3 раза в неделю" -> {
            var count = 0
            while (dates.size < totalCount) {
                dates.add(calendar.timeInMillis)
                count++
                if (count == 3) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.getInstance().apply {
                        timeInMillis = startDate
                    }.get(Calendar.DAY_OF_WEEK))
                    count = 0
                } else {
                    calendar.add(Calendar.DAY_OF_YEAR, 2)
                }
            }
        }
        "5 раз в неделю" -> {
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
