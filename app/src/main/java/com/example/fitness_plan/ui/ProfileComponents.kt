package com.example.fitness_plan.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.UserProfile
import java.io.ByteArrayOutputStream

private const val TAG = "ProfileComponents"

@Composable
fun Base64Image(
    base64: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val imageBitmap: ImageBitmap? = remember(base64) {
        if (base64 != null) {
            try {
                val decodedBytes = Base64.decode(base64, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                bitmap?.asImageBitmap()
            } catch (e: Exception) {
                Log.e(TAG, "Error decoding base64 image", e)
                null
            }
        } else {
            null
        }
    }

    if (imageBitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = imageBitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    title: String,
    isEditing: Boolean,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = {
                        Badge { Text("3") }
                    }
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Уведомления")
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isEditing) {
                TextButton(onClick = onCancelClick) {
                    Text("Отмена")
                }
                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Сохранить")
                }
            } else {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Редактировать")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun ProfileCard(
    profile: UserProfile,
    isEditing: Boolean,
    onPhotoClick: () -> Unit,
    onUsernameChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable(enabled = isEditing, onClick = onPhotoClick)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profile.photo != null) {
                    Base64Image(
                        base64 = profile.photo,
                        contentDescription = "Фото профиля",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Добавить фото",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Изменить фото",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = profile.username,
                    onValueChange = onUsernameChange,
                    label = { Text("Имя пользователя") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                Text(
                    profile.username,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean = false,
    onValueChange: (String) -> Unit = {},
    isDropdown: Boolean = false,
    dropdownOptions: List<String> = emptyList(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var expanded by remember { mutableStateOf(false) }
    var tempValue by remember { mutableStateOf(value) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isEditing) {
                if (isDropdown) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = tempValue,
                            onValueChange = { tempValue = it },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            dropdownOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        tempValue = option
                                        onValueChange(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = tempValue,
                        onValueChange = {
                            tempValue = it
                            onValueChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = keyboardOptions,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            } else {
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ProfileInfoSection(
    profile: UserProfile,
    isEditing: Boolean,
    onGoalChange: (String) -> Unit,
    onLevelChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onTargetWeightChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Информация",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            val goalOptions = listOf(
                UserProfile.GOAL_WEIGHT_LOSS,
                UserProfile.GOAL_MUSCLE_GAIN,
                UserProfile.GOAL_MAINTENANCE
            )

            val levelOptions = listOf(
                UserProfile.LEVEL_BEGINNER,
                UserProfile.LEVEL_INTERMEDIATE,
                UserProfile.LEVEL_ADVANCED
            )

            val frequencyOptions = listOf(
                UserProfile.FREQUENCY_1X,
                UserProfile.FREQUENCY_3X,
                UserProfile.FREQUENCY_5X
            )

            val genderOptions = listOf(
                UserProfile.GENDER_MALE,
                UserProfile.GENDER_FEMALE
            )

            ProfileInfoItem(
                icon = Icons.Outlined.Star,
                label = "Цель",
                value = profile.goal,
                isEditing = isEditing,
                onValueChange = onGoalChange,
                isDropdown = true,
                dropdownOptions = goalOptions
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ProfileInfoItem(
                icon = Icons.Outlined.Star,
                label = "Уровень",
                value = profile.level,
                isEditing = isEditing,
                onValueChange = onLevelChange,
                isDropdown = true,
                dropdownOptions = levelOptions
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ProfileInfoItem(
                icon = Icons.Outlined.DateRange,
                label = "Частота тренировок",
                value = profile.frequency,
                isEditing = isEditing,
                onValueChange = onFrequencyChange,
                isDropdown = true,
                dropdownOptions = frequencyOptions
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ProfileInfoItem(
                icon = Icons.Outlined.Info,
                label = "Вес",
                value = "${profile.weight} кг",
                isEditing = isEditing,
                onValueChange = onWeightChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ProfileInfoItem(
                icon = Icons.Outlined.Info,
                label = "Рост",
                value = "${profile.height} см",
                isEditing = isEditing,
                onValueChange = onHeightChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ProfileInfoItem(
                icon = Icons.Outlined.Person,
                label = "Пол",
                value = profile.gender,
                isEditing = isEditing,
                onValueChange = onGenderChange,
                isDropdown = true,
                dropdownOptions = genderOptions
            )

            if (profile.goal == UserProfile.GOAL_WEIGHT_LOSS && (isEditing || profile.targetWeight != null)) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                ProfileInfoItem(
                    icon = Icons.Outlined.Star,
                    label = "Целевой вес",
                    value = if (profile.targetWeight != null) "${profile.targetWeight} кг" else "Не указан",
                    isEditing = isEditing,
                    onValueChange = onTargetWeightChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}

@Composable
fun GoalProgressSection(
    profile: UserProfile,
    currentWeight: Double? = null
) {
    if (profile.goal != UserProfile.GOAL_WEIGHT_LOSS || profile.targetWeight == null) {
        return
    }

    val weight = currentWeight ?: profile.weight
    val progress = calculateWeightProgress(profile.weight, profile.targetWeight, weight)
    val isWeightLoss = profile.weight > profile.targetWeight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Прогресс цели",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    "${progress.toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { (progress / 100.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Начальный вес: ${profile.weight} кг",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Text(
                    "Текущий вес: ${weight} кг",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Text(
                    "Цель: ${profile.targetWeight} кг",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun calculateWeightProgress(initialWeight: Double, targetWeight: Double, currentWeight: Double): Double {
    return if (initialWeight > targetWeight) {
        val totalToLose = initialWeight - targetWeight
        val lost = initialWeight - currentWeight
        if (totalToLose > 0) {
            (lost / totalToLose * 100).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
    } else {
        val totalToGain = targetWeight - initialWeight
        val gained = currentWeight - initialWeight
        if (totalToGain > 0) {
            (gained / totalToGain * 100).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
    }
}

@Composable
fun SettingsSection(
    onSettingsClick: () -> Unit,
    workoutReminderEnabled: Boolean = false,
    onWorkoutReminderToggle: (Boolean) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingsSwitchItem(
                icon = Icons.Outlined.Notifications,
                title = "Напоминание о тренировке",
                description = "За 8 часов до тренировки",
                checked = workoutReminderEnabled,
                onCheckedChange = onWorkoutReminderToggle
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SettingsItem(
                icon = Icons.Outlined.Notifications,
                title = "История уведомлений",
                description = "Все уведомления",
                onClick = onSettingsClick
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SettingsItem(
                icon = Icons.Outlined.Settings,
                title = "Тема приложения",
                description = "Светлая/тёмная тема",
                onClick = onSettingsClick
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "Язык",
                description = "Выбрать язык приложения",
                onClick = onSettingsClick
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SettingsItem(
                icon = Icons.Outlined.Share,
                title = "Экспорт данных",
                description = "Сохранить данные тренировок",
                onClick = onSettingsClick
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SettingsItem(
                icon = Icons.Outlined.Delete,
                title = "Очистить данные",
                description = "Удалить все данные приложения",
                onClick = onSettingsClick
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "О приложении",
                description = "Версия и информация",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun NotificationDialog(
    showNotifications: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Уведомления") },
        text = {
            Column {
                if (showNotifications) {
                    NotificationItem(
                        title = "Напоминание о тренировке",
                        time = "Сегодня, 18:00",
                        isNew = true
                    )
                    NotificationItem(
                        title = "День тренировки",
                        time = "Завтра, 10:00",
                        isNew = false
                    )
                    NotificationItem(
                        title = "Цель достигнута!",
                        time = "Вчера",
                        isNew = false
                    )
                } else {
                    Text(
                        "Нет новых уведомлений",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun NotificationItem(
    title: String,
    time: String,
    isNew: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isNew) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isNew) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ProfileChangeConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Подтверждение изменений") },
        text = {
            Text(
                "Изменение цели, уровня подготовки или частоты тренировок приведёт к перестройке вашего тренировочного плана. Вы уверены?"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Подтвердить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

fun base64ToBitmap(base64: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        Log.e(TAG, "Error decoding base64", e)
        null
    }
}
