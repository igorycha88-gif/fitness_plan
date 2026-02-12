package com.example.fitness_plan.presentation.viewmodel

import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.PlanType
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
class WorkoutViewModelPlanTypeTest {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockCycleRepository: CycleRepository
    private lateinit var mockExerciseStatsRepository: ExerciseStatsRepository
    private lateinit var mockCycleUseCase: CycleUseCase
    private lateinit var mockWorkoutUseCase: WorkoutUseCase
    private lateinit var mockExerciseCompletionRepository: ExerciseCompletionRepository
    private lateinit var mockWeightCalculator: com.example.fitness_plan.domain.calculator.WeightCalculator
    private lateinit var mockExerciseLibraryUseCase: ExerciseLibraryUseCase
    private lateinit var mockPlanHistoryUseCase: com.example.fitness_plan.domain.usecase.PlanHistoryUseCase

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
        mockPlanHistoryUseCase = mockk(relaxed = true)

        viewModel = WorkoutViewModel(
            mockk(relaxed = true),
            mockUserRepository,
            mockCycleRepository,
            mockExerciseStatsRepository,
            mockCycleUseCase,
            mockWorkoutUseCase,
            mockWeightCalculator,
            mockExerciseLibraryUseCase,
            mockExerciseCompletionRepository,
            mockPlanHistoryUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `createAdminPlan should create new plan with PlanType ADMIN`() = runTest {
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
                    it.planType == PlanType.ADMIN &&
                    it.days.isEmpty()
                }
            )
        }
    }

    @Test
    fun `createUserPlan should call usecase to create plan with PlanType USER`() = runTest {
        val username = "test_user"
        val planName = "User Plan"
        val planDescription = "User description"

        coEvery { mockWorkoutUseCase.createUserPlan(any(), any(), any()) } just Runs
        coEvery { mockWorkoutUseCase.setSelectedPlanType(any(), any()) } just Runs
        coEvery { mockUserRepository.getUserProfile() } returns flowOf(
            com.example.fitness_plan.domain.model.UserProfile(
                username = username,
                goal = "Test",
                level = "Test",
                frequency = "3 раза в неделю",
                weight = 80.0,
                height = 180.0,
                gender = "Мужской"
            )
        )

        viewModel.setCurrentUsername(username)
        viewModel.createUserPlan(planName, planDescription)

        coVerify {
            mockWorkoutUseCase.createUserPlan(
                username,
                planName,
                planDescription
            )
        }
    }
}
