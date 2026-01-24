package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.presentation.viewmodel.ExerciseLibraryViewModel
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    viewModel: ExerciseLibraryViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel? = null,
    onExerciseClick: (ExerciseLibrary) -> Unit = {},
    onToggleFavorite: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<ExerciseType?>(null) }
    var selectedEquipment by remember { mutableStateOf<List<EquipmentType>>(emptyList()) }
    var selectedMuscles by remember { mutableStateOf<List<MuscleGroup>>(emptyList()) }
    var showEquipmentDropdown by remember { mutableStateOf(false) }
    var showMusclesDropdown by remember { mutableStateOf(false) }

    val exercises by viewModel.exercises.collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteExercises by profileViewModel?.getFavoriteExercises()
        ?.collectAsStateWithLifecycle(initialValue = emptySet()) ?: remember { mutableStateOf(emptySet()) }

    LaunchedEffect(favoriteExercises) {
        viewModel.setFavoriteExercises(favoriteExercises)
    }

    val filteredExercises by remember(searchQuery, selectedType, selectedEquipment, selectedMuscles, favoriteExercises) {
        derivedStateOf {
            viewModel.getFilteredAndSortedExercises(
                searchQuery = searchQuery,
                typeFilter = selectedType,
                equipmentFilter = selectedEquipment,
                muscleFilter = selectedMuscles,
                favorites = favoriteExercises
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Поиск упражнений") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Поиск")
            },
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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedType != null,
                onClick = {
                    selectedType = if (selectedType == null) ExerciseType.STRENGTH else null
                },
                label = { Text(selectedType?.displayName ?: "Тип") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            ExposedDropdownMenuBox(
                expanded = showEquipmentDropdown,
                onExpandedChange = { showEquipmentDropdown = it }
            ) {
                FilterChip(
                    selected = selectedEquipment.isNotEmpty(),
                    onClick = { showEquipmentDropdown = true },
                    label = { Text("Оборудование") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = showEquipmentDropdown,
                    onDismissRequest = { showEquipmentDropdown = false }
                ) {
                    EquipmentType.values().forEach { equipment ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedEquipment.contains(equipment),
                                        onCheckedChange = {
                                            selectedEquipment = if (selectedEquipment.contains(equipment)) {
                                                selectedEquipment - equipment
                                            } else {
                                                selectedEquipment + equipment
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(equipment.displayName)
                                }
                            },
                            onClick = { }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = showMusclesDropdown,
                onExpandedChange = { showMusclesDropdown = it }
            ) {
                FilterChip(
                    selected = selectedMuscles.isNotEmpty(),
                    onClick = { showMusclesDropdown = true },
                    label = { Text("Мышцы") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = showMusclesDropdown,
                    onDismissRequest = { showMusclesDropdown = false }
                ) {
                    MuscleGroup.values().forEach { muscle ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedMuscles.contains(muscle),
                                        onCheckedChange = {
                                            selectedMuscles = if (selectedMuscles.contains(muscle)) {
                                                selectedMuscles - muscle
                                            } else {
                                                selectedMuscles + muscle
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(muscle.displayName)
                                }
                            },
                            onClick = { }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredExercises.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Упражнения не найдены",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredExercises.value) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onClick = { onExerciseClick(exercise) },
                        isFavorite = favoriteExercises.contains(exercise.name),
                        onToggleFavorite = { onToggleFavorite(exercise.name) }
                    )
                }
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
