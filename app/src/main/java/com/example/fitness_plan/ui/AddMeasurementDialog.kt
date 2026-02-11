package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.BodyParameterType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeasurementDialog(
    onDismiss: () -> Unit,
    onSave: (Map<BodyParameterType, Double?>, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(Date()) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    val mainParameters = remember {
        mutableStateMapOf(
            BodyParameterType.WEIGHT to "",
            BodyParameterType.HEIGHT to ""
        )
    }
    
    val circumferenceParameters = remember {
        mutableStateMapOf(
            BodyParameterType.CHEST to "",
            BodyParameterType.WAIST to "",
            BodyParameterType.HIPS to "",
            BodyParameterType.NECK to "",
            BodyParameterType.BICEPS to "",
            BodyParameterType.SHOULDERS to "",
            BodyParameterType.THIGH to "",
            BodyParameterType.CALF to ""
        )
    }
    
    var calculateAuto by remember { mutableStateOf(true) }
    
    val hasAnyData = remember {
        derivedStateOf {
            mainParameters.values.any { it.isNotEmpty() } ||
            circumferenceParameters.values.any { it.isNotEmpty() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        title = {
            Text(
                text = "Добавить измерение",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = dateFormat.format(selectedDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Дата") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Выбрать дату"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader(title = "Основные параметры")
                Spacer(modifier = Modifier.height(8.dp))

                mainParameters.forEach { (type, value) ->
                    MeasurementInputField(
                        label = type.displayName,
                        value = value,
                        onValueChange = { mainParameters[type] = it },
                        unit = type.unit,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader(title = "Окружности тела (по желанию)")
                Spacer(modifier = Modifier.height(8.dp))

                circumferenceParameters.forEach { (type, value) ->
                    MeasurementInputField(
                        label = type.displayName,
                        value = value,
                        onValueChange = { circumferenceParameters[type] = it },
                        unit = type.unit,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = calculateAuto,
                        onCheckedChange = { calculateAuto = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Рассчитать автоматически",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Жир в организме, мышечная масса, ИМТ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val allParameters = mutableMapOf<BodyParameterType, Double?>()
                    
                    mainParameters.forEach { (type, value) ->
                        allParameters[type] = value.toDoubleOrNull()
                    }
                    
                    circumferenceParameters.forEach { (type, value) ->
                        allParameters[type] = value.toDoubleOrNull()
                    }
                    
                    onSave(allParameters, calculateAuto)
                },
                enabled = hasAnyData.value,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Сохранить",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}
