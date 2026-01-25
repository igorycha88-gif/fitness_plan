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
}
