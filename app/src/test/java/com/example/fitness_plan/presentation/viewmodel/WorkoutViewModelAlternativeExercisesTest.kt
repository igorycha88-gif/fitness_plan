package com.example.fitness_plan.presentation.viewmodel

import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.usecase.CycleUseCase
import com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase
import com.example.fitness_plan.domain.usecase.WorkoutUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelAlternativeExercisesTest {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockCycleRepository: CycleRepository
    private lateinit var mockExerciseStatsRepository: ExerciseStatsRepository
    private lateinit var mockCycleUseCase: CycleUseCase
    private lateinit var mockWorkoutUseCase: WorkoutUseCase
    private lateinit var mockWeightCalculator: com.example.fitness_plan.domain.calculator.WeightCalculator
    private lateinit var mockExerciseLibraryUseCase: ExerciseLibraryUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleAlternatives = listOf(
        ExerciseLibrary(
            id = "leg_press",
            name = "Жим ногами",
            description = "Изолированное упражнение для ног",
            exerciseType = ExerciseType.STRENGTH,
            equipment = listOf(EquipmentType.LEVER_MACHINE),
            muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            difficulty = "Начальный",
            stepByStepInstructions = "Сядьте в тренажер, поставьте ноги на платформу",
            animationUrl = null
        ),
        ExerciseLibrary(
            id = "lunges",
            name = "Выпады",
            description = "Упражнение для ног и ягодиц",
            exerciseType = ExerciseType.STRENGTH,
            equipment = listOf(EquipmentType.BODYWEIGHT),
            muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            difficulty = "Начальный",
            stepByStepInstructions = "Сделайте широкий шаг вперед",
            animationUrl = null
        ),
        ExerciseLibrary(
            id = "split_squat",
            name = "Приседания на одной ноге",
            description = "Балансовое упражнение для ног",
            exerciseType = ExerciseType.STRENGTH,
            equipment = listOf(EquipmentType.BODYWEIGHT),
            muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            difficulty = "Средний",
            stepByStepInstructions = "Встаньте на одной ноге, приседайте",
            animationUrl = null
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockUserRepository = mockk(relaxed = true)
        mockCycleRepository = mockk(relaxed = true)
        mockExerciseStatsRepository = mockk(relaxed = true)
        mockCycleUseCase = mockk(relaxed = true)
        mockWorkoutUseCase = mockk(relaxed = true)
        mockWeightCalculator = mockk<com.example.fitness_plan.domain.calculator.WeightCalculator>(relaxed = true)
        mockExerciseLibraryUseCase = mockk(relaxed = true)

        viewModel = WorkoutViewModel(
            mockk(relaxed = true),
            mockUserRepository,
            mockCycleRepository,
            mockExerciseStatsRepository,
            mockCycleUseCase,
            mockWorkoutUseCase,
            mockWeightCalculator,
            mockExerciseLibraryUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `loadAlternativeExercises should update alternativeExercises state`() = runTest {
        coEvery { mockExerciseLibraryUseCase.getAlternativeExercises(any(), any(), any()) } returns sampleAlternatives

        viewModel.loadAlternativeExercises(
            "Приседания",
            listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            3
        )

        val alternatives = viewModel.alternativeExercises.value
        assertThat(alternatives).isNotNull()
        assertThat(alternatives).hasSize(3)
        assertThat(alternatives[0].name).isEqualTo("Жим ногами")
    }

    @Test
    fun `loadAlternativeExercises should pass correct parameters to use case`() = runTest {
        coEvery { mockExerciseLibraryUseCase.getAlternativeExercises(any(), any(), any()) } returns emptyList()

        val exerciseName = "Приседания"
        val muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES)
        val limit = 2

        viewModel.loadAlternativeExercises(exerciseName, muscleGroups, limit)

        coVerify {
            mockExerciseLibraryUseCase.getAlternativeExercises(
                currentExerciseName = exerciseName,
                currentMuscleGroups = muscleGroups,
                limit = limit
            )
        }
    }

    @Test
    fun `loadAlternativeExercises with limit should respect limit parameter`() = runTest {
        val limitedAlternatives = sampleAlternatives.take(2)
        coEvery { mockExerciseLibraryUseCase.getAlternativeExercises(any(), any(), any()) } returns limitedAlternatives

        viewModel.loadAlternativeExercises(
            "Приседания",
            listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            2
        )

        val alternatives = viewModel.alternativeExercises.value
        assertThat(alternatives).hasSize(2)
    }

    @Test
    fun `loadAlternativeExercises with empty result should set empty state`() = runTest {
        coEvery { mockExerciseLibraryUseCase.getAlternativeExercises(any(), any(), any()) } returns emptyList()

        viewModel.loadAlternativeExercises(
            "Уникальное упражнение",
            listOf(MuscleGroup.ABS),
            3
        )

        val alternatives = viewModel.alternativeExercises.value
        assertThat(alternatives).isEmpty()
    }
}
