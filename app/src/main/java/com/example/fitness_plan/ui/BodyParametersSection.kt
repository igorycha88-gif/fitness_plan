package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.BodyParameterType
import com.example.fitness_plan.presentation.viewmodel.BodyParametersViewModel

@Composable
fun BodyParametersSection(
    viewModel: BodyParametersViewModel,
    modifier: Modifier = Modifier
) {
    val latestMeasurements by viewModel.latestMeasurements.collectAsState(initial = emptyMap())
    val showAddDialog by viewModel.showAddDialog.collectAsState(initial = false)
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
    val successMessage by viewModel.successMessage.collectAsState(initial = null)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Параметры тела",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CurrentParametersCard(
            latestMeasurements = latestMeasurements,
            viewModel = viewModel
        )

        if (showAddDialog) {
            AddMeasurementDialog(
                onDismiss = {
                    viewModel.setShowAddDialog(false)
                    viewModel.clearMessages()
                },
                onSave = { parameters, calculateAuto ->
                    val input = com.example.fitness_plan.domain.model.MeasurementInput(
                        date = System.currentTimeMillis(),
                        parameters = parameters
                    )
                    viewModel.saveMeasurement(input, calculateAuto)
                    viewModel.clearMessages()
                }
            )
        }

        errorMessage?.let { message ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearMessages() }) {
                        Text("OK", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            ) {
                Text(text = message)
            }
        }

        successMessage?.let { message ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                action = {
                    TextButton(
                        onClick = { viewModel.clearMessages() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("OK", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
