package com.example.fitness_plan.data

import com.example.fitness_plan.domain.calculator.ExerciseType
import com.example.fitness_plan.domain.calculator.WeightCalculator
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.model.UserProfile
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class WorkoutRepositoryFavoriteExercisesTest {

    private lateinit var workoutRepositoryImpl: WorkoutRepositoryImpl
    private lateinit var mockContext: android.content.Context
    private lateinit var mockExerciseCompletionRepository: com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
    private lateinit var mockWorkoutScheduleRepository: WorkoutScheduleRepository
    private lateinit var mockWeightCalculator: WeightCalculator
    private lateinit var mockExerciseLibraryRepository: com.example.fitness_plan.domain.repository.ExerciseLibraryRepository

    private val exerciseLibrary = listOf(
        ExerciseLibrary(
            id = "bench_press",
            name = "Жим лёжа",
            description = "Базовое упражнение для груди",
            exerciseType = com.example.fitness_plan.domain.model.ExerciseType.STRENGTH,
            equipment = listOf(EquipmentType.SPECIAL_BENCH),
            muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
            difficulty = "Начальный",
            stepByStepInstructions = "Жми"
        ),
        ExerciseLibrary(
            id = "incline_bench_press",
            name = "Жим на наклонной скамье",
            description = "Упражнение для верхней части груди",
            exerciseType = com.example.fitness_plan.domain.model.ExerciseType.STRENGTH,
            equipment = listOf(EquipmentType.SPECIAL_BENCH),
            muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
            difficulty = "Средний",
            stepByStepInstructions = "Жми на наклонной скамье"
        )
    )

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockExerciseCompletionRepository = mockk(relaxed = true)
        mockWorkoutScheduleRepository = mockk(relaxed = true)
        mockWeightCalculator = mockk(relaxed = true)
        mockExerciseLibraryRepository = mockk(relaxed = true)
        val workoutDateCalculator = com.example.fitness_plan.domain.calculator.WorkoutDateCalculator()

        every { mockWeightCalculator.calculateBaseWeight(any(), any(), any(), any(), any()) } returns 20.0f
        every { mockWeightCalculator.getRecommendedRepsString(any()) } returns "10-12"
        every { mockWeightCalculator.determineExerciseType(any()) } returns ExerciseType.COMPOUND
        every { mockExerciseLibraryRepository.getAllExercises() } returns flowOf(exerciseLibrary)
        coEvery { mockExerciseLibraryRepository.getAllExercisesAsList() } returns exerciseLibrary

        workoutRepositoryImpl = WorkoutRepositoryImpl(
            context = mockContext,
            exerciseCompletionRepository = mockExerciseCompletionRepository,
            workoutScheduleRepository = mockWorkoutScheduleRepository,
            weightCalculator = mockWeightCalculator,
            workoutDateCalculator = workoutDateCalculator,
            exerciseLibraryRepository = mockExerciseLibraryRepository
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when user has favorite exercises, first 50% exercises are replaced`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Наращивание мышечной массы",
            level = "Новичок",
            frequency = "1 раз в неделю",
            weight = 80.0,
            height = 180.0,
            gender = "Мужской",
            favoriteExercises = setOf("Жим на наклонной скамье")
        )

        val plan = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(plan.days).isNotEmpty()

        val firstDay = plan.days.first()
        val substitutedExercises = firstDay.exercises.filter { it.isFavoriteSubstitution }
        assertThat(substitutedExercises.size).isAtLeast(1)
    }

    @Test
    fun `favorite exercise with matching muscle groups replaces original exercise`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Наращивание мышечной массы",
            level = "Новичок",
            frequency = "1 раз в неделю",
            weight = 80.0,
            height = 180.0,
            gender = "Мужской",
            favoriteExercises = setOf("Жим на наклонной скамье")
        )

        val plan = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        val firstDay = plan.days.first()
        val substitutedExercises = firstDay.exercises.filter { it.isFavoriteSubstitution }
        assertThat(substitutedExercises).isNotEmpty()

        val substitutedExercise = substitutedExercises.first()
        assertThat(substitutedExercise.name).isEqualTo("Жим на наклонной скамье")
    }

    @Test
    fun `when no favorite exercises match, original exercises are kept`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Наращивание мышечной массы",
            level = "Новичок",
            frequency = "1 раз в неделю",
            weight = 80.0,
            height = 180.0,
            gender = "Мужской",
            favoriteExercises = emptySet()
        )

        val plan = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        val allExercises = plan.days.flatMap { it.exercises }
        val substitutedExercises = allExercises.filter { it.isFavoriteSubstitution }
        assertThat(substitutedExercises).isEmpty()
    }

    @Test
    fun `only first 50% of exercises are considered for substitution`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Наращивание мышечной массы",
            level = "Новичок",
            frequency = "1 раз в неделю",
            weight = 80.0,
            height = 180.0,
            gender = "Мужской",
            favoriteExercises = setOf("Жим лёжа")
        )

        val plan = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        val firstDay = plan.days.first()
        val totalExercises = firstDay.exercises.size
        val substitutionThreshold = totalExercises / 2

        firstDay.exercises.forEachIndexed { index, exercise ->
            if (index >= substitutionThreshold) {
                assertThat(exercise.isFavoriteSubstitution).isFalse()
            }
        }
    }
}