package com.example.fitness_plan.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.BodyParameterType
import com.example.fitness_plan.presentation.viewmodel.ParameterOption
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterSelectorDialog(
    availableParameters: List<ParameterOption>,
    selectedTypes: Set<BodyParameterType>,
    onDismiss: () -> Unit,
    onConfirm: (Set<BodyParameterType>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedTypes) }

    val canSelectMore = tempSelected.size < 5
    val selectionCount = tempSelected.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Выберите параметры",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Text(
                    text = "Можно выбрать: ${5 - selectionCount} параметров",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (canSelectMore) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableParameters) { option ->
                        val isSelected = option.type in tempSelected
                        val canSelect = canSelectMore || isSelected

                        ParameterSelectorItem(
                            option = option,
                            isSelected = isSelected,
                            canSelect = canSelect,
                            onToggle = {
                                if (isSelected) {
                                    tempSelected -= option.type
                                } else if (canSelectMore) {
                                    tempSelected += option.type
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSelected) },
                enabled = tempSelected.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Добавить ($selectionCount)",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ParameterSelectorItem(
    option: ParameterOption,
    isSelected: Boolean,
    canSelect: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (canSelect) {
                    Modifier.clickable { onToggle() }
                } else {
                    Modifier
                }
            ),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { if (canSelect) onToggle() },
                enabled = canSelect,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = if (canSelect) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (canSelect) {
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Text(
                    text = if (option.lastValue != null) {
                        "последн.: %.1f ${option.type.unit}".format(option.lastValue)
                    } else {
                        "последн.: --"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (option.lastDate != null) {
                val sdf = SimpleDateFormat("d MMM", Locale("ru"))
                Text(
                    text = sdf.format(option.lastDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
