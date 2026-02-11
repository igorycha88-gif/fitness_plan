package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.presentation.viewmodel.ExerciseLibraryViewModel
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel

@Composable
fun ExerciseLibraryScreen(
    viewModel: ExerciseLibraryViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel? = null,
    onExerciseClick: (ExerciseLibrary) -> Unit = {},
    onToggleFavorite: (String) -> Unit = {}
) {
    val filteredExercises by viewModel.filteredExercises.collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteExercises by profileViewModel?.getFavoriteExercises()
        ?.collectAsStateWithLifecycle(initialValue = emptySet()) ?: remember { mutableStateOf(emptySet()) }
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val selectedEquipment by viewModel.selectedEquipment.collectAsStateWithLifecycle()
    val selectedMuscles by viewModel.selectedMuscles.collectAsStateWithLifecycle()

    var expandedType by remember { mutableStateOf(false) }
    var expandedEquipment by remember { mutableStateOf(false) }
    var expandedMuscles by remember { mutableStateOf(false) }

    LaunchedEffect(favoriteExercises) {
        viewModel.setFavoriteExercises(favoriteExercises)
    }

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FilterSection(
            selectedType = selectedType,
            selectedEquipment = selectedEquipment,
            selectedMuscles = selectedMuscles,
            expandedType = expandedType,
            expandedEquipment = expandedEquipment,
            expandedMuscles = expandedMuscles,
            onTypeToggle = { expandedType = it },
            onEquipmentToggle = { expandedEquipment = it },
            onMusclesToggle = { expandedMuscles = it },
            onTypeSelect = { viewModel.setTypeFilter(it) },
            onEquipmentSelect = { equipment ->
                val current = selectedEquipment.toMutableList()
                if (current.contains(equipment)) {
                    current.remove(equipment)
                } else {
                    current.add(equipment)
                }
                viewModel.setEquipmentFilter(current)
            },
            onMuscleToggle = { viewModel.toggleMuscleFilter(it) },
            onReset = { viewModel.resetFilters() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredExercises) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) },
                    isFavorite = favoriteExercises.contains(exercise.name),
                    onToggleFavorite = {
                        profileViewModel?.toggleFavoriteExercise(exercise.name)
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: ExerciseLibrary,
    onClick: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = exercise.muscleGroups.joinToString(", ") { it.displayName },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Убрать из избранного" else "Добавить в избранное",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FilterSection(
    selectedType: ExerciseType?,
    selectedEquipment: List<EquipmentType>,
    selectedMuscles: List<MuscleGroup>,
    expandedType: Boolean,
    expandedEquipment: Boolean,
    expandedMuscles: Boolean,
    onTypeToggle: (Boolean) -> Unit,
    onEquipmentToggle: (Boolean) -> Unit,
    onMusclesToggle: (Boolean) -> Unit,
    onTypeSelect: (ExerciseType?) -> Unit,
    onEquipmentSelect: (EquipmentType) -> Unit,
    onMuscleToggle: (MuscleGroup) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Фильтры",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onReset) {
                    Text(
                        text = "Сбросить",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { onTypeToggle(true) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedType != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            contentColor = if (selectedType != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = selectedType?.displayName ?: "Тип",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    DropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { onTypeToggle(false) },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Все типы") },
                            onClick = { onTypeSelect(null); onTypeToggle(false) },
                            leadingIcon = if (selectedType == null) {
                                { Icon(Icons.Filled.Check, contentDescription = null) }
                            } else null
                        )
                        ExerciseType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = { onTypeSelect(type); onTypeToggle(false) },
                                leadingIcon = if (selectedType == type) {
                                    { Icon(Icons.Filled.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { onEquipmentToggle(true) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedEquipment.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            contentColor = if (selectedEquipment.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = if (selectedEquipment.isEmpty()) "Оборудование" else "(${selectedEquipment.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }

                    DropdownMenu(
                        expanded = expandedEquipment,
                        onDismissRequest = { onEquipmentToggle(false) },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 300.dp)
                    ) {
                        EquipmentType.values().forEach { equipment ->
                            DropdownMenuItem(
                                text = { Text(equipment.displayName) },
                                onClick = { onEquipmentSelect(equipment); onEquipmentToggle(false) },
                                leadingIcon = if (selectedEquipment.contains(equipment)) {
                                    { Icon(Icons.Filled.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { onMusclesToggle(true) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedMuscles.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            contentColor = if (selectedMuscles.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = if (selectedMuscles.isEmpty()) "Мышцы" else "(${selectedMuscles.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }

                    DropdownMenu(
                        expanded = expandedMuscles,
                        onDismissRequest = { onMusclesToggle(false) },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 300.dp)
                    ) {
                        MuscleGroup.values().forEach { muscle ->
                            DropdownMenuItem(
                                text = { Text(muscle.displayName) },
                                onClick = { onMuscleToggle(muscle); onMusclesToggle(false) },
                                leadingIcon = if (selectedMuscles.contains(muscle)) {
                                    { Icon(Icons.Filled.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            }

            if (selectedEquipment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FilterChipsRow(
                    items = selectedEquipment.map { it.displayName },
                    onRemove = { index ->
                        onEquipmentSelect(selectedEquipment[index])
                    }
                )
            }

            if (selectedMuscles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FilterChipsRow(
                    items = selectedMuscles.map { it.displayName },
                    onRemove = { index ->
                        onMuscleToggle(selectedMuscles[index])
                    }
                )
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    items: List<String>,
    onRemove: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, item ->
            FilterChip(
                selected = true,
                onClick = { onRemove(index) },
                label = { Text(item) },
                trailingIcon = {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Удалить",
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.height(32.dp)
            )
        }
    }
}
