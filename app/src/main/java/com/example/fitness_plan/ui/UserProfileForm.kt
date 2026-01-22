package com.example.fitness_plan.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 48.dp) // Фиксированный отступ для status bar
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            }

            Text(
                text = "Расскажи о себе",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            ModernDropdown(
                label = "Цель тренировок",
                options = goalOptions,
                selectedOption = goal,
                onOptionSelected = { goal = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            ModernDropdown(
                label = "Уровень подготовки",
                options = levelOptions,
                selectedOption = level,
                onOptionSelected = { level = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            ModernDropdown(
                label = "Частота тренировок",
                options = frequencyOptions,
                selectedOption = frequency,
                onOptionSelected = { frequency = it }
            )
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
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
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
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            ModernDropdown(
                label = "Пол",
                options = genderOptions,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Сохранить профиль",
                    style = MaterialTheme.typography.titleMedium
                )
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
                    val profileUsername = username.ifEmpty { viewModel.currentUsername.value }

                    val profile = UserProfile(
                        username = profileUsername,
                        goal = goal!!,
                        level = level!!,
                        frequency = frequency!!,
                        weight = weightDouble!!,
                        height = heightDouble!!,
                        gender = gender!!
                    )

                    Log.d("UserProfileForm", "onConfirm: Saving profile with username=${profile.username}")
                    viewModel.saveUserProfile(profile)
                    viewModel.setCurrentUsername(profileUsername)

                    onProfileSaved()
                }
            )
        }
    }
}

@Composable
fun ModernDropdown(
    label: String,
    options: List<DropdownOption>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.6f,
        animationSpec = tween(200),
        label = "chevronAlpha"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box {
            OutlinedTextField(
                value = selectedOption ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        "Выберите...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (expanded) 8.dp else 0.dp,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (expanded) 2.dp else 1.dp,
                        color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .noRippleClickable { expanded = !expanded },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .noRippleClickable { expanded = !expanded }
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ) + fadeIn(),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    options.forEachIndexed { index, option ->
                        val isSelected = selectedOption == option.label

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .noRippleClickable {
                                    onOptionSelected(option.label)
                                    expanded = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                if (option.description.isNotEmpty()) {
                                    Text(
                                        text = option.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Выбрано",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (index < options.lastIndex) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
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
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (startDate != null) formatDate(startDate!!) else "Выберите дату начала"
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
