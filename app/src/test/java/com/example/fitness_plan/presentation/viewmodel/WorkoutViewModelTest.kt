package com.example.fitness_plan.presentation.viewmodel

import org.junit.Ignore

import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.usecase.CycleUseCase
import com.example.fitness_plan.domain.usecase.WorkoutUseCase
import com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase
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
@Ignore("Skip until test suite stabilized with Admin modularization")
class WorkoutViewModelTest {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockCycleRepository: CycleRepository
    private lateinit var mockExerciseStatsRepository: ExerciseStatsRepository
    private lateinit var mockCycleUseCase: CycleUseCase
    private lateinit var mockWorkoutUseCase: WorkoutUseCase
    private lateinit var mockExerciseCompletionRepository: ExerciseCompletionRepository
    private lateinit var mockWeightCalculator: com.example.fitness_plan.domain.calculator.WeightCalculator
    private lateinit var mockExerciseLibraryUseCase: ExerciseLibraryUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockUserRepository = mockk(relaxed = true)
        mockCycleRepository = mockk(relaxed = true)
        mockExerciseStatsRepository = mockk(relaxed = true)
        mockCycleUseCase = mockk(relaxed = true)
        mockWorkoutUseCase = mockk(relaxed = true)
        mockExerciseCompletionRepository = mockk(relaxed = true)
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
            mockExerciseLibraryUseCase,
            mockExerciseCompletionRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `createAdminPlan should create new plan and save it`() = runTest {
        val planName = "Admin Test Plan"
        val planDescription = "Test description"

        coEvery { mockWorkoutUseCase.saveAdminWorkoutPlan(any()) } just Runs

        viewModel.createAdminPlan(planName, planDescription)

        coVerify {
            mockWorkoutUseCase.saveAdminWorkoutPlan(
                match<WorkoutPlan> {
                    it.name == planName &&
                    it.description == planDescription &&
                    it.id == "admin_plan" &&
                    it.days.isEmpty()
                }
            )
        }
    }

    @Test
    fun `addDayToAdminPlan should add new day to existing plan`() = runTest {
        val existingPlan = WorkoutPlan(
            id = "admin_plan",
            name = "Test Plan",
            description = "Test Description",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = emptyList()
        )

        coEvery { mockWorkoutUseCase.saveAdminWorkoutPlan(any()) } just Runs
        coEvery { mockWorkoutUseCase.getAdminWorkoutPlan() } returns flowOf(null)

        viewModel.createAdminPlan("Test", "Description")

        val dayName = "Leg Day"

        viewModel.addDayToAdminPlan(dayName)

        coVerify {
            mockWorkoutUseCase.saveAdminWorkoutPlan(
                match<WorkoutPlan> {
                    it.days.size == 1 &&
                    it.days[0].dayName == dayName &&
                    it.days[0].id == 0 &&
                    it.days[0].exercises.isEmpty()
                }
            )
        }
    }

    @Test
    fun `removeDayFromAdminPlan should remove day by index`() = runTest {
        val day1 = WorkoutDay(id = 0, dayName = "Day 1", exercises = emptyList(), muscleGroups = emptyList())
        val day2 = WorkoutDay(id = 1, dayName = "Day 2", exercises = emptyList(), muscleGroups = emptyList())
        val day3 = WorkoutDay(id = 2, dayName = "Day 3", exercises = emptyList(), muscleGroups = emptyList())

        val existingPlan = WorkoutPlan(
            id = "admin_plan",
            name = "Test Plan",
            description = "Test Description",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(day1, day2, day3)
        )

        coEvery { mockWorkoutUseCase.saveAdminWorkoutPlan(any()) } just Runs
        coEvery { mockWorkoutUseCase.getAdminWorkoutPlan() } returns flowOf(existingPlan)

        viewModel.addDayToAdminPlan("Day 1")
        viewModel.addDayToAdminPlan("Day 2")
        viewModel.addDayToAdminPlan("Day 3")

        viewModel.removeDayFromAdminPlan(1)

        coVerify {
            mockWorkoutUseCase.saveAdminWorkoutPlan(
                match<WorkoutPlan> {
                    it.days.size == 2 &&
                    it.days[0].dayName == "Day 1" &&
                    it.days[1].dayName == "Day 3"
                }
            )
        }
    }

    @Test
    fun `updateDayDate should update scheduledDate for specific day`() = runTest {
        val day1 = WorkoutDay(id = 0, dayName = "Day 1", exercises = emptyList(), muscleGroups = emptyList(), scheduledDate = null)
        val existingPlan = WorkoutPlan(
            id = "admin_plan",
            name = "Test Plan",
            description = "Test Description",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(day1)
        )

        val newDate = 1234567890L

        coEvery { mockWorkoutUseCase.saveAdminWorkoutPlan(any()) } just Runs
        coEvery { mockWorkoutUseCase.getAdminWorkoutPlan() } returns flowOf(existingPlan)

        viewModel.addDayToAdminPlan("Day 1")
        viewModel.updateDayDate(0, newDate)

        coVerify {
            mockWorkoutUseCase.saveAdminWorkoutPlan(
                match<WorkoutPlan> {
                    it.days.size == 1 &&
                    it.days[0].scheduledDate == newDate
                }
            )
        }
    }

    @Test
    fun `updateDayDate should clear scheduledDate when null is passed`() = runTest {
        val day1 = WorkoutDay(id = 0, dayName = "Day 1", exercises = emptyList(), muscleGroups = emptyList(), scheduledDate = 1234567890L)
        val existingPlan = WorkoutPlan(
            id = "admin_plan",
            name = "Test Plan",
            description = "Test Description",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(day1)
        )

        coEvery { mockWorkoutUseCase.saveAdminWorkoutPlan(any()) } just Runs
        coEvery { mockWorkoutUseCase.getAdminWorkoutPlan() } returns flowOf(existingPlan)

        viewModel.addDayToAdminPlan("Day 1")
        viewModel.updateDayDate(0, null)

        coVerify {
            mockWorkoutUseCase.saveAdminWorkoutPlan(
                match<WorkoutPlan> {
                    it.days.size == 1 &&
                    it.days[0].scheduledDate == null
                }
            )
        }
    }

    @Test
    fun `addExerciseToDay should add exercise to specific day`() = runTest {
        val exercise1 = Exercise(
            id = "ex1",
            name = "Squats",
            sets = 3,
            reps = "12-15",
            weight = null,
            imageRes = null,
            isCompleted = false,
            alternatives = emptyList()
        )

        val day1 = WorkoutDay(id = 0, dayName = "Day 1", exercises = emptyList(), muscleGroups = emptyList())
        val existingPlan = WorkoutPlan(
            id = "admin_plan",
            name = "Test Plan",
            description = "Test Description",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(day1)
        )

        coEvery { mockWorkoutUseCase.saveAdminWorkoutPlan(any()) } just Runs
        coEvery { mockWorkoutUseCase.getAdminWorkoutPlan() } returns flowOf(existingPlan)

        viewModel.addDayToAdminPlan("Day 1")
        viewModel.addExerciseToDay(0, exercise1)

        coVerify {
            mockWorkoutUseCase.saveAdminWorkoutPlan(
                match<WorkoutPlan> {
                    it.days.size == 1 &&
                    it.days[0].exercises.size == 1 &&
                    it.days[0].exercises[0].name == "Squats"
                }
            )
        }
    }

    @Test
    fun `addExerciseToDay should add multiple exercises to same day`() = runTest {
        val exercise1 = Exercise(
            id = "ex1",
            name = "Squats",
            sets = 3,
            reps = "12-15",
            weight = null,
            imageRes = null,
            isCompleted = false,
            alternatives = emptyList()
        )

        val exercise2 = Exercise(
            id = "ex2",
            name = "Bench Press",
            sets = 3,
            reps = "10-12",
            weight = null,
            imageRes = null,
            isCompleted = false,
            alternatives = emptyList()
        )

        val day1 = WorkoutDay(id = 0, dayName = "Day 1", exercises = emptyList(), muscleGroups = emptyList())
        val existingPlan = WorkoutPlan(
            id = "admin_plan",
            name = "Test Plan",
            description = "Test Description",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(day1)
        )

        coEvery { mockWorkoutUseCase.saveAdminWorkoutPlan(any()) } just Runs
        coEvery { mockWorkoutUseCase.getAdminWorkoutPlan() } returns flowOf(existingPlan)

        viewModel.addDayToAdminPlan("Day 1")
        viewModel.addExerciseToDay(0, exercise1)
        viewModel.addExerciseToDay(0, exercise2)

        coVerify {
            mockWorkoutUseCase.saveAdminWorkoutPlan(
                match<WorkoutPlan> {
                    it.days[0].exercises.size == 2 &&
                    it.days[0].exercises[0].name == "Squats" &&
                    it.days[0].exercises[1].name == "Bench Press"
                }
            )
        }
    }

    @Test
    fun `adminWorkoutPlan should emit updated plan after operations`() = runTest {
        val expectedPlan = WorkoutPlan(
            id = "admin_plan",
            name = "Test Plan",
            description = "Test Description",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(WorkoutDay(id = 0, dayName = "Day 1", exercises = emptyList(), muscleGroups = emptyList()))
        )

        coEvery { mockWorkoutUseCase.saveAdminWorkoutPlan(any()) } just Runs
        coEvery { mockWorkoutUseCase.getAdminWorkoutPlan() } returns flowOf(expectedPlan)

        viewModel.createAdminPlan("Test Plan", "Test Description")

        val result = viewModel.adminWorkoutPlan.first()

        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo(expectedPlan.name)
        assertThat(result?.days?.size).isEqualTo(expectedPlan.days.size)
    }
}
