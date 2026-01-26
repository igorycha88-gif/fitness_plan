package com.example.fitness_plan.presentation.viewmodel

import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ExerciseLibraryViewModelTest {

    private lateinit var viewModel: ExerciseLibraryViewModel
    private val mockExerciseLibraryUseCase = mockk<com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase>(relaxed = true)

    private val sampleExercises = listOf(
        ExerciseLibrary(
            id = "1",
            name = "Приседания",
            description = "Базовое упражнение для ног",
            exerciseType = ExerciseType.STRENGTH,
            equipment = listOf(EquipmentType.BARBELL),
            muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            difficulty = "Начальный",
            stepByStepInstructions = "Встаньте прямо, поставьте ноги на ширину плеч\nПриседайте, держа спину прямой\nВернитесь в исходное положение",
            animationUrl = null,
            tipsAndAdvice = null,
            progressionAdvice = null
        ),
        ExerciseLibrary(
            id = "2",
            name = "Жим лёжа",
            description = "Упражнение для груди",
            exerciseType = ExerciseType.STRENGTH,
            equipment = listOf(EquipmentType.BARBELL, EquipmentType.SPECIAL_BENCH),
            muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
            difficulty = "Средний",
            stepByStepInstructions = "Лягте на скамью\nОпустите штангу к груди\nВыжмите штангу вверх",
            animationUrl = null,
            tipsAndAdvice = "Следите за хватом",
            progressionAdvice = "Увеличивайте вес постепенно"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        every { mockExerciseLibraryUseCase.getAllExercises() } returns flowOf(sampleExercises)
        viewModel = ExerciseLibraryViewModel(mockExerciseLibraryUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty exercises list`() = runTest {
        assertEquals(emptyList<ExerciseLibrary>(), viewModel.exercises.value)
    }

    @Test
    fun `initial state should have empty favorite exercises`() = runTest {
        assertEquals(emptySet<String>(), viewModel.favoriteExercises.value)
    }

    @Test
    fun `initialize should populate exercises from use case`() = runTest {
        viewModel.initialize()

        val exercises = viewModel.exercises.first()
        assertEquals(2, exercises.size)
        assertEquals("Приседания", exercises[0].name)
        assertEquals("Жим лёжа", exercises[1].name)

        coVerify { mockExerciseLibraryUseCase.initializeDefaultExercises() }
    }

    @Test
    fun `setFavoriteExercises should update favorite exercises set`() = runTest {
        val favorites = setOf("Приседания", "Жим лёжа")

        viewModel.setFavoriteExercises(favorites)

        assertEquals(favorites, viewModel.favoriteExercises.value)
    }

    @Test
    fun `getExerciseById should return correct exercise`() = runTest {
        coEvery { mockExerciseLibraryUseCase.getExerciseById("1") } returns sampleExercises[0]
        coEvery { mockExerciseLibraryUseCase.getExerciseById("999") } returns null

        val found = viewModel.getExerciseById("1")
        val notFound = viewModel.getExerciseById("999")

        assertEquals("Приседания", found?.name)
        assertNull(notFound)
    }

    @Test
    fun `getFilteredAndSortedExercises with empty filters should return all exercises`() = runTest {
        viewModel.initialize()

        val result = viewModel.getFilteredAndSortedExercises()

        assertEquals(2, result.size)
    }

    @Test
    fun `getFilteredAndSortedExercises with search query should filter by name or description`() = runTest {
        viewModel.initialize()

        val result = viewModel.getFilteredAndSortedExercises(searchQuery = "приседания")

        assertEquals(1, result.size)
        assertEquals("Приседания", result[0].name)
    }

    @Test
    fun `getFilteredAndSortedExercises with type filter should filter by exercise type`() = runTest {
        viewModel.initialize()

        val result = viewModel.getFilteredAndSortedExercises(typeFilter = ExerciseType.STRENGTH)

        assertEquals(2, result.size)
    }

    @Test
    fun `getFilteredAndSortedExercises with muscle filter should filter by muscle group`() = runTest {
        viewModel.initialize()

        val result = viewModel.getFilteredAndSortedExercises(
            muscleFilter = listOf(MuscleGroup.QUADS)
        )

        assertEquals(1, result.size)
        assertEquals("Приседания", result[0].name)
    }

    @Test
    fun `getFilteredAndSortedExercises with equipment filter should filter by equipment`() = runTest {
        viewModel.initialize()

        val result = viewModel.getFilteredAndSortedExercises(
            equipmentFilter = listOf(EquipmentType.BARBELL)
        )

        assertEquals(2, result.size)
    }

    @Test
    fun `getFilteredAndSortedExercises with favorites should sort favorites first`() = runTest {
        viewModel.initialize()
        viewModel.setFavoriteExercises(setOf("Жим лёжа"))

        val result = viewModel.getFilteredAndSortedExercises(
            favorites = viewModel.favoriteExercises.value
        )

        assertEquals("Жим лёжа", result[0].name)
    }

    @Test
    fun `getFilteredAndSortedExercises should apply all filters together`() = runTest {
        viewModel.initialize()

        val result = viewModel.getFilteredAndSortedExercises(
            searchQuery = "для",
            typeFilter = ExerciseType.STRENGTH,
            muscleFilter = listOf(MuscleGroup.CHEST)
        )

        assertEquals(1, result.size)
        assertEquals("Жим лёжа", result[0].name)
    }

    @Test
    fun `getAllExercises should return flow from use case`() = runTest {
        viewModel.initialize()

        val flow = viewModel.getAllExercises()
        val exercises = flow.first()

        assertEquals(2, exercises.size)
    }

    @Test
    fun `setTypeFilter should update selected type`() = runTest {
        viewModel.initialize()
        viewModel.setTypeFilter(ExerciseType.STRENGTH)

        assertEquals(ExerciseType.STRENGTH, viewModel.selectedType.value)
    }

    @Test
    fun `setEquipmentFilter should update selected equipment`() = runTest {
        viewModel.initialize()
        viewModel.setEquipmentFilter(listOf(EquipmentType.BARBELL))

        assertEquals(listOf(EquipmentType.BARBELL), viewModel.selectedEquipment.value)
    }

    @Test
    fun `toggleMuscleFilter should add muscle if not selected`() = runTest {
        viewModel.initialize()
        viewModel.toggleMuscleFilter(MuscleGroup.QUADS)

        assertEquals(listOf(MuscleGroup.QUADS), viewModel.selectedMuscles.value)
    }

    @Test
    fun `toggleMuscleFilter should remove muscle if selected`() = runTest {
        viewModel.initialize()
        viewModel.toggleMuscleFilter(MuscleGroup.QUADS)
        viewModel.toggleMuscleFilter(MuscleGroup.QUADS)

        assertEquals(emptyList<MuscleGroup>(), viewModel.selectedMuscles.value)
    }

    @Test
    fun `resetFilters should clear all filters`() = runTest {
        viewModel.initialize()
        viewModel.setTypeFilter(ExerciseType.STRENGTH)
        viewModel.setEquipmentFilter(listOf(EquipmentType.BARBELL))
        viewModel.toggleMuscleFilter(MuscleGroup.QUADS)

        viewModel.resetFilters()

        assertNull(viewModel.selectedType.value)
        assertEquals(emptyList<EquipmentType>(), viewModel.selectedEquipment.value)
        assertEquals(emptyList<MuscleGroup>(), viewModel.selectedMuscles.value)
    }

    @Test
    fun `filteredExercises should update when type filter changes`() = runTest {
        viewModel.initialize()
        advanceUntilIdle()

        viewModel.setTypeFilter(ExerciseType.STRENGTH)
        advanceUntilIdle()

        val filtered = viewModel.filteredExercises.first()
        assertEquals(2, filtered.size)
    }

    @Test
    fun `filteredExercises should update when muscle filter changes`() = runTest {
        viewModel.initialize()
        advanceUntilIdle()

        viewModel.toggleMuscleFilter(MuscleGroup.QUADS)
        advanceUntilIdle()

        val filtered = viewModel.filteredExercises.first()
        assertEquals(1, filtered.size)
        assertEquals("Приседания", filtered[0].name)
    }

    @Test
    fun `filteredExercises should show only exercises matching all filters`() = runTest {
        viewModel.initialize()
        advanceUntilIdle()

        viewModel.setTypeFilter(ExerciseType.STRENGTH)
        viewModel.toggleMuscleFilter(MuscleGroup.CHEST)
        advanceUntilIdle()

        val filtered = viewModel.filteredExercises.first()
        assertEquals(1, filtered.size)
        assertEquals("Жим лёжа", filtered[0].name)
    }

    @Test
    fun `filteredExercises should be empty when no exercises match filters`() = runTest {
        viewModel.initialize()
        advanceUntilIdle()

        viewModel.setTypeFilter(ExerciseType.CARDIO)
        advanceUntilIdle()

        val filtered = viewModel.filteredExercises.first()
        assertEquals(0, filtered.size)
    }

    @Test
    fun `filteredExercises with multiple muscles should show exercises matching any muscle`() = runTest {
        viewModel.initialize()
        advanceUntilIdle()

        viewModel.toggleMuscleFilter(MuscleGroup.QUADS)
        viewModel.toggleMuscleFilter(MuscleGroup.CHEST)
        advanceUntilIdle()

        val filtered = viewModel.filteredExercises.first()
        assertEquals(2, filtered.size)
    }
}
