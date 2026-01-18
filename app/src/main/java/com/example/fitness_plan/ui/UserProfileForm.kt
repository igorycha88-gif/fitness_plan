package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.fitness_plan.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileForm(
    viewModel: UserProfileViewModel, // ДОБАВЛЕНО: Принимаем ViewModel как параметр
    onProfileSaved: () -> Unit // ИЗМЕНЕНО: Теперь просто сигнализируем о завершении, без передачи данных
) {
    var goal by remember { mutableStateOf<String?>(null) }
    var level by remember { mutableStateOf<String?>(null) }
    var frequency by remember { mutableStateOf<String?>(null) }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }

    val goals = listOf("Похудение", "Наращивание мышечной массы", "Поддержание формы")
    val levels = listOf("Новичок", "Любитель", "Профессионал")
    val frequencies = listOf("1 раз в неделю", "3 раза в неделю", "5 раз в неделю")
    val genders = listOf("Мужской", "Женский")

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
                        val profile = UserProfile(
                            goal = goal!!,
                            level = level!!,
                            frequency = frequency!!,
                            weight = weightDouble!!,
                            height = heightDouble!!,
                            gender = gender!!
                        )
                        // ИСПОЛЬЗУЕМ VIEWMODEL для сохранения данных
                        viewModel.saveUserProfile(profile)
                        onProfileSaved()
                    }
                },
                enabled = isValid && isNumericValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить профиль")
            }
        }
    }
}

// Отдельная переиспользуемая Composable-функция для выпадающего списка остается без изменений
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
