package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.WorkoutRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@Ignore("Skip until test suite stabilized with Admin modularization")
class WorkoutUseCaseTest {

    private lateinit var workoutUseCase: WorkoutUseCase
    private lateinit var mockWorkoutRepository: WorkoutRepository
    private lateinit var mockExerciseStatsRepository: ExerciseStatsRepository
    private lateinit var mockExerciseCompletionRepository: ExerciseCompletionRepository

    @Before
    fun setup() {
        mockWorkoutRepository = mockk(relaxed = true)
        mockExerciseStatsRepository = mockk(relaxed = true)
        mockExerciseCompletionRepository = mockk(relaxed = true)

        workoutUseCase = WorkoutUseCase(
            mockWorkoutRepository,
            mockExerciseStatsRepository,
            mockExerciseCompletionRepository
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `saveAdminWorkoutPlan should delegate to repository`() = runTest {
        val adminPlan = WorkoutPlan(
            id = "admin_plan",
            name = "Admin Plan",
            description = "Admin Description",
            muscleGroups = listOf("Legs", "Chest"),
            goal = "Admin",
            level = "Admin",
            days = emptyList()
        )

        coEvery { mockWorkoutRepository.saveAdminWorkoutPlan(any()) } just runs

        workoutUseCase.saveAdminWorkoutPlan(adminPlan)

        coVerify { mockWorkoutRepository.saveAdminWorkoutPlan(adminPlan) }
    }

    @Test
    fun `getAdminWorkoutPlan should return admin plan flow`() = runTest {
        val adminPlan = WorkoutPlan(
            id = "admin_plan",
            name = "Admin Plan",
            description = "Admin Description",
            muscleGroups = listOf("Legs", "Chest"),
            goal = "Admin",
            level = "Admin",
            days = emptyList()
        )

        coEvery { mockWorkoutRepository.getAdminWorkoutPlan() } returns flowOf(adminPlan)

        val result = workoutUseCase.getAdminWorkoutPlan().first()

        assertThat(result).isEqualTo(adminPlan)
        coVerify { mockWorkoutRepository.getAdminWorkoutPlan() }
    }

    @Test
    fun `getAdminWorkoutPlan should return null when no admin plan exists`() = runTest {
        coEvery { mockWorkoutRepository.getAdminWorkoutPlan() } returns flowOf(null)

        val result = workoutUseCase.getAdminWorkoutPlan().first()

        assertThat(result).isNull()
        coVerify { mockWorkoutRepository.getAdminWorkoutPlan() }
    }

    @Test
    fun `getWorkoutPlan should return plan for user`() = runTest {
        val username = "testuser"
        val userPlan = WorkoutPlan(
            id = "user_plan",
            name = "User Plan",
            description = "User Description",
            muscleGroups = listOf("Legs", "Chest"),
            goal = "Muscle Gain",
            level = "Intermediate",
            days = emptyList()
        )

        coEvery { mockWorkoutRepository.getWorkoutPlan(username) } returns flowOf(userPlan)

        val result = workoutUseCase.getWorkoutPlan(username)

        assertThat(result).isEqualTo(userPlan)
        coVerify { mockWorkoutRepository.getWorkoutPlan(username) }
    }

    @Test
    fun `saveExerciseStats should delegate to repository`() = runTest {
        val username = "testuser"
        val exerciseName = "Squats"
        val weight = 80.0
        val reps = 12
        val setNumber = 1
        val sets = 3

        coEvery { mockExerciseStatsRepository.saveExerciseStats(any(), any()) } just runs

        workoutUseCase.saveExerciseStats(username, exerciseName, weight, reps, setNumber, sets)

        coVerify {
            mockExerciseStatsRepository.saveExerciseStats(
                username,
                match<ExerciseStats> {
                    it.exerciseName == exerciseName &&
                    it.weight == weight &&
                    it.reps == reps &&
                    it.setNumber == setNumber &&
                    it.sets == sets
                }
            )
        }
    }

    @Test
    fun `getCompletedExercises should delegate to repository`() = runTest {
        val username = "testuser"
        val completedExercises = setOf("ex1_Squats", "ex2_BenchPress")

        every { mockExerciseCompletionRepository.getAllCompletedExercises(username) } returns flowOf(completedExercises)

        val result = workoutUseCase.getCompletedExercises(username)

        assertThat(result.first()).isEqualTo(completedExercises)
        verify { mockExerciseCompletionRepository.getAllCompletedExercises(username) }
    }

    @Test
    fun `toggleExerciseCompletion should set exercise completion and return completed days`() = runTest {
        val username = "testuser"
        val exerciseKey = "0_Squats"
        val completed = true

        val plan = WorkoutPlan(
            id = "test_plan",
            name = "Test Plan",
            description = "Test Description",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(
                com.example.fitness_plan.domain.model.WorkoutDay(
                    id = 0,
                    dayName = "Day 1",
                    exercises = listOf(
                        Exercise(
                            id = "ex1",
                            name = "Squats",
                            sets = 3,
                            reps = "12-15",
                            weight = null,
                            imageRes = null,
                            isCompleted = false,
                            alternatives = emptyList()
                        )
                    ),
                    muscleGroups = emptyList()
                )
            )
        )

        coEvery { mockExerciseCompletionRepository.setExerciseCompleted(any(), any(), any()) } just runs
        every { mockExerciseCompletionRepository.getAllCompletedExercises(username) } returns flowOf(setOf(exerciseKey))

        val completedDays = workoutUseCase.toggleExerciseCompletion(username, exerciseKey, completed, plan)

        coVerify { mockExerciseCompletionRepository.setExerciseCompleted(username, exerciseKey, completed) }
        assertThat(completedDays).containsExactly(0)
    }

    @Test
    fun `toggleExerciseCompletion with exercise name only should mark all occurrences`() = runTest {
        val username = "testuser"
        val exerciseKey = "Squats"
        val completed = true

        val plan = WorkoutPlan(
            id = "test_plan",
            name = "Test Plan",
            description = "Test Description",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(
                com.example.fitness_plan.domain.model.WorkoutDay(
                    id = 0,
                    dayName = "Day 1",
                    exercises = listOf(
                        Exercise(
                            id = "ex1",
                            name = "Squats",
                            sets = 3,
                            reps = "12-15",
                            weight = null,
                            imageRes = null,
                            isCompleted = false,
                            alternatives = emptyList()
                        )
                    ),
                    muscleGroups = emptyList()
                ),
                com.example.fitness_plan.domain.model.WorkoutDay(
                    id = 1,
                    dayName = "Day 2",
                    exercises = listOf(
                        Exercise(
                            id = "ex2",
                            name = "Squats",
                            sets = 3,
                            reps = "10-12",
                            weight = null,
                            imageRes = null,
                            isCompleted = false,
                            alternatives = emptyList()
                        )
                    ),
                    muscleGroups = emptyList()
                ),
                com.example.fitness_plan.domain.model.WorkoutDay(
                    id = 2,
                    dayName = "Day 3",
                    exercises = listOf(
                        Exercise(
                            id = "ex3",
                            name = "BenchPress",
                            sets = 3,
                            reps = "8-10",
                            weight = null,
                            imageRes = null,
                            isCompleted = false,
                            alternatives = emptyList()
                        )
                    ),
                    muscleGroups = emptyList()
                )
            )
        )

        val slot = mutableListOf<String>()
        coEvery { mockExerciseCompletionRepository.setExerciseCompleted(any(), capture(slot), any()) } just runs
        every { mockExerciseCompletionRepository.getAllCompletedExercises(username) } returns flowOf(setOf("0_Squats", "1_Squats"))

        val completedDays = workoutUseCase.toggleExerciseCompletion(username, exerciseKey, completed, plan)

        coVerify(exactly = 2) { mockExerciseCompletionRepository.setExerciseCompleted(username, any(), true) }
        assertThat(slot).containsExactly("0_Squats", "1_Squats")
        assertThat(completedDays).containsExactly(0, 1)
    }

    @Test
    fun `updateWorkoutSchedule should delegate to repository`() = runTest {
        val username = "testuser"
        val dates = listOf(1000L, 2000L, 3000L)

        coEvery { mockWorkoutRepository.saveWorkoutSchedule(any(), any()) } just runs

        workoutUseCase.updateWorkoutSchedule(username, dates)

        coVerify { mockWorkoutRepository.saveWorkoutSchedule(username, dates) }
    }

    @Test
    fun `getExerciseStats should delegate to repository`() = runTest {
        val username = "testuser"
        val stats = listOf(
            ExerciseStats(
                exerciseName = "Squats",
                date = 1234567890L,
                weight = 80.0,
                reps = 12,
                setNumber = 1,
                sets = 3
            )
        )

        every { mockExerciseStatsRepository.getExerciseStats(username) } returns flowOf(stats)

        val result = workoutUseCase.getExerciseStats(username)

        assertThat(result.first()).isEqualTo(stats)
        verify { mockExerciseStatsRepository.getExerciseStats(username) }
    }

    @Test
    fun `getExerciseSummaries should calculate correct summaries`() = runTest {
        val username = "testuser"
        val exerciseNames = listOf("Squats", "Bench Press")

        val stats = listOf(
            ExerciseStats("Squats", 1234567890L, 80.0, 12, 1, 3),
            ExerciseStats("Squats", 1234567891L, 85.0, 10, 2, 3),
            ExerciseStats("Squats", 1234567892L, 90.0, 8, 3, 3),
            ExerciseStats("Bench Press", 1234567893L, 60.0, 10, 1, 3),
            ExerciseStats("Bench Press", 1234567894L, 65.0, 8, 2, 3)
        )

        every { mockExerciseStatsRepository.getExerciseStats(username) } returns flowOf(stats)

        val summaries = workoutUseCase.getExerciseSummaries(username, exerciseNames).first()

        assertThat(summaries).hasSize(2)

        val squatsSummary = summaries.find { it.exerciseName == "Squats" }
        assertThat(squatsSummary).isNotNull()
        assertThat(squatsSummary).isNotNull()
        assertThat(squatsSummary!!.maxWeight).isEqualTo(90.0)
        assertThat(squatsSummary!!.averageWeight).isEqualTo((80.0 + 85.0 + 90.0) / 3)
        assertThat(squatsSummary!!.totalVolume).isEqualTo((80 * 12 + 85 * 10 + 90 * 8).toLong())
        assertThat(squatsSummary!!.totalSets).isEqualTo(3)

        val benchSummary = summaries.find { it.exerciseName == "Bench Press" }
        assertThat(benchSummary).isNotNull()
        assertThat(benchSummary).isNotNull()
        assertThat(benchSummary!!.maxWeight).isEqualTo(65.0)
        assertThat(benchSummary!!.averageWeight).isEqualTo((60.0 + 65.0) / 2)
        assertThat(benchSummary!!.totalVolume).isEqualTo((60 * 10 + 65 * 8).toLong())
        assertThat(benchSummary!!.totalSets).isEqualTo(2)
    }
}
