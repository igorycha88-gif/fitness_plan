package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleHistoryEntry
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.repository.WorkoutRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class CycleUseCaseTest {

    private lateinit var cycleUseCase: CycleUseCase
    private lateinit var mockCycleRepository: CycleRepository
    private lateinit var mockWorkoutRepository: WorkoutRepository
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockExerciseCompletionRepository: ExerciseCompletionRepository
    private lateinit var mockWeightProgressionUseCase: WeightProgressionUseCase
    private lateinit var mockExercisePoolManager: ExercisePoolManager

    private val testUsername = "testuser"
    private val testProfile = UserProfile(
        username = testUsername,
        goal = "Наращивание мышечной массы",
        level = "Любитель",
        gender = "Мужской",
        weight = 75.0,
        height = 180.0,
        frequency = "3 раза в неделю"
    )

    private val existingCycle = Cycle(
        cycleNumber = 1,
        startDate = System.currentTimeMillis() - 100000,
        daysCompleted = 5,
        totalDays = Cycle.DAYS_IN_CYCLE,
        completedMicrocycles = 0
    )

    @Before
    fun setup() {
        mockCycleRepository = mockk(relaxed = true)
        mockWorkoutRepository = mockk(relaxed = true)
        mockUserRepository = mockk(relaxed = true)
        mockExerciseCompletionRepository = mockk(relaxed = true)
        mockWeightProgressionUseCase = mockk(relaxed = true)
        mockExercisePoolManager = mockk(relaxed = true)

        cycleUseCase = CycleUseCase(
            mockCycleRepository,
            mockWorkoutRepository,
            mockUserRepository,
            mockExerciseCompletionRepository,
            mockWeightProgressionUseCase,
            mockExercisePoolManager
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initializeCycleForUser loads saved plan when cycle exists`() = runTest {
        val savedPlan = createTestPlan()
        
        coEvery { mockCycleRepository.getCompletedDate(testUsername) } returns null
        coEvery { mockCycleRepository.getCurrentCycleSync(testUsername) } returns existingCycle
        every { mockWorkoutRepository.getWorkoutPlan(testUsername) } returns flowOf(savedPlan)
        coEvery { mockCycleRepository.getCycleHistory(testUsername) } returns flowOf(emptyList())

        val result = cycleUseCase.initializeCycleForUser(testUsername, testProfile)

        assertThat(result.workoutPlan).isEqualTo(savedPlan)
        coVerify(exactly = 0) { mockWorkoutRepository.getWorkoutPlanForUser(any()) }
        coVerify(exactly = 0) { mockWorkoutRepository.saveWorkoutPlan(any(), any()) }
    }

    @Test
    fun `initializeCycleForUser creates and saves new plan when no cycle exists`() = runTest {
        val newPlan = createTestPlan()
        
        coEvery { mockCycleRepository.getCompletedDate(testUsername) } returns null
        coEvery { mockCycleRepository.getCurrentCycleSync(testUsername) } returns null
        every { mockWorkoutRepository.getWorkoutPlan(testUsername) } returns flowOf(null)
        coEvery { mockCycleRepository.startNewCycle(any(), any()) } returns existingCycle
        coEvery { mockWorkoutRepository.getWorkoutPlanWithSequence(testProfile, any()) } returns newPlan
        coEvery { mockWorkoutRepository.generateCycleDates(any(), any()) } returns listOf(System.currentTimeMillis())
        coEvery { mockWorkoutRepository.getWorkoutPlanWithDates(any(), any()) } returns newPlan
        coEvery { mockCycleRepository.getCycleHistory(testUsername) } returns flowOf(emptyList())
        coEvery { mockCycleRepository.getExerciseHistory(testUsername) } returns flowOf(emptyList())
        coEvery { mockCycleRepository.saveExerciseHistory(any(), any()) } just Runs

        val result = cycleUseCase.initializeCycleForUser(testUsername, testProfile)

        assertThat(result.workoutPlan).isNotNull()
        coVerify { mockWorkoutRepository.saveWorkoutPlan(testUsername, result.workoutPlan!!) }
    }

    @Test
    fun `initializeCycleForUser creates new plan after cycle completion`() = runTest {
        val oldPlan = createTestPlan()
        val newPlan = createTestPlan().copy(name = "New Plan")
        
        coEvery { mockCycleRepository.getCompletedDate(testUsername) } returns System.currentTimeMillis()
        coEvery { mockCycleRepository.getCurrentCycleSync(testUsername) } returns existingCycle
        every { mockWorkoutRepository.getWorkoutPlan(testUsername) } returns flowOf(oldPlan)
        coEvery { mockCycleRepository.resetCycle(testUsername) } just Runs
        coEvery { mockExerciseCompletionRepository.clearCompletion(testUsername) } just Runs
        coEvery { mockCycleRepository.startNewCycle(any(), any()) } returns existingCycle.copy(cycleNumber = 2)
        coEvery { mockWorkoutRepository.getWorkoutPlanWithSequence(testProfile, any()) } returns newPlan
        coEvery { mockWorkoutRepository.generateCycleDates(any(), any()) } returns listOf(System.currentTimeMillis())
        coEvery { mockWorkoutRepository.getWorkoutPlanWithDates(any(), any()) } returns newPlan
        coEvery { mockCycleRepository.getCycleHistory(testUsername) } returns flowOf(emptyList())
        coEvery { mockCycleRepository.getExerciseHistory(testUsername) } returns flowOf(emptyList())
        coEvery { mockCycleRepository.saveExerciseHistory(any(), any()) } just Runs

        val result = cycleUseCase.initializeCycleForUser(testUsername, testProfile)

        assertThat(result.workoutPlan).isNotNull()
        coVerify { mockWorkoutRepository.saveWorkoutPlan(testUsername, result.workoutPlan!!) }
        coVerify { mockCycleRepository.resetCycle(testUsername) }
        coVerify { mockExerciseCompletionRepository.clearCompletion(testUsername) }
    }

    @Test
    fun `initializeCycleForUser creates new plan when saved plan is null`() = runTest {
        val newPlan = createTestPlan()
        
        coEvery { mockCycleRepository.getCompletedDate(testUsername) } returns null
        coEvery { mockCycleRepository.getCurrentCycleSync(testUsername) } returns existingCycle
        every { mockWorkoutRepository.getWorkoutPlan(testUsername) } returns flowOf(null)
        coEvery { mockWorkoutRepository.getWorkoutPlanWithSequence(testProfile, any()) } returns newPlan
        coEvery { mockWorkoutRepository.generateCycleDates(any(), any()) } returns listOf(System.currentTimeMillis())
        coEvery { mockWorkoutRepository.getWorkoutPlanWithDates(any(), any()) } returns newPlan
        coEvery { mockCycleRepository.getCycleHistory(testUsername) } returns flowOf(emptyList())
        coEvery { mockCycleRepository.getExerciseHistory(testUsername) } returns flowOf(emptyList())
        coEvery { mockCycleRepository.saveExerciseHistory(any(), any()) } just Runs

        val result = cycleUseCase.initializeCycleForUser(testUsername, testProfile)

        assertThat(result.workoutPlan).isNotNull()
        coVerify { mockWorkoutRepository.saveWorkoutPlan(testUsername, result.workoutPlan!!) }
    }

    @Test
    fun `initializeCycleForUser creates new plan when saved plan has no days`() = runTest {
        val emptyPlan = createTestPlan().copy(days = emptyList())
        val newPlan = createTestPlan()
        
        coEvery { mockCycleRepository.getCompletedDate(testUsername) } returns null
        coEvery { mockCycleRepository.getCurrentCycleSync(testUsername) } returns existingCycle
        every { mockWorkoutRepository.getWorkoutPlan(testUsername) } returns flowOf(emptyPlan)
        coEvery { mockWorkoutRepository.getWorkoutPlanWithSequence(testProfile, any()) } returns newPlan
        coEvery { mockWorkoutRepository.generateCycleDates(any(), any()) } returns listOf(System.currentTimeMillis())
        coEvery { mockWorkoutRepository.getWorkoutPlanWithDates(any(), any()) } returns newPlan
        coEvery { mockCycleRepository.getCycleHistory(testUsername) } returns flowOf(emptyList())
        coEvery { mockCycleRepository.getExerciseHistory(testUsername) } returns flowOf(emptyList())
        coEvery { mockCycleRepository.saveExerciseHistory(any(), any()) } just Runs

        val result = cycleUseCase.initializeCycleForUser(testUsername, testProfile)

        assertThat(result.workoutPlan).isNotNull()
        coVerify { mockWorkoutRepository.saveWorkoutPlan(testUsername, result.workoutPlan!!) }
    }

    @Test
    fun `initializeCycleForUser migrates old cycle format correctly`() = runTest {
        val oldFormatCycle = existingCycle.copy(totalDays = 10)
        val newPlan = createTestPlan()
        
        coEvery { mockCycleRepository.getCompletedDate(testUsername) } returns null
        coEvery { mockCycleRepository.getCurrentCycleSync(testUsername) } returns oldFormatCycle
        coEvery { mockCycleRepository.markCycleCompleted(any(), any()) } just Runs
        coEvery { mockCycleRepository.resetCycle(testUsername) } just Runs
        coEvery { mockExerciseCompletionRepository.clearCompletion(testUsername) } just Runs
        coEvery { mockCycleRepository.startNewCycle(any(), any()) } returns existingCycle
        every { mockWorkoutRepository.getWorkoutPlan(testUsername) } returns flowOf(null)
        coEvery { mockWorkoutRepository.getWorkoutPlanWithSequence(testProfile, any()) } returns newPlan
        coEvery { mockWorkoutRepository.generateCycleDates(any(), any()) } returns listOf(System.currentTimeMillis())
        coEvery { mockWorkoutRepository.getWorkoutPlanWithDates(any(), any()) } returns newPlan
        coEvery { mockCycleRepository.getCycleHistory(testUsername) } returns flowOf(emptyList())
        coEvery { mockCycleRepository.getExerciseHistory(testUsername) } returns flowOf(emptyList())
        coEvery { mockCycleRepository.saveExerciseHistory(any(), any()) } just Runs

        val result = cycleUseCase.initializeCycleForUser(testUsername, testProfile)

        assertThat(result.workoutPlan).isNotNull()
        coVerify { mockCycleRepository.markCycleCompleted(any(), any()) }
        coVerify { mockCycleRepository.resetCycle(testUsername) }
        coVerify { mockExerciseCompletionRepository.clearCompletion(testUsername) }
    }

    private fun createTestPlan(): WorkoutPlan {
        return WorkoutPlan(
            id = "test_plan",
            name = "Test Plan",
            description = "Test Description",
            muscleGroups = listOf("Ноги", "Грудь"),
            goal = "Наращивание мышечной массы",
            level = "Любитель",
            days = listOf(
                WorkoutDay(
                    id = 0,
                    dayName = "День 1",
                    exercises = listOf(
                        Exercise(
                            id = "0_Приседания",
                            name = "Приседания",
                            sets = 3,
                            reps = "10-12",
                            recommendedWeight = 20.0f,
                            recommendedRepsPerSet = "10,11,12"
                        ),
                        Exercise(
                            id = "0_Жим лёжа",
                            name = "Жим лёжа",
                            sets = 3,
                            reps = "10-12",
                            recommendedWeight = 30.0f,
                            recommendedRepsPerSet = "10,11,12"
                        )
                    ),
                    muscleGroups = listOf("Ноги", "Грудь")
                )
            )
        )
    }
}
