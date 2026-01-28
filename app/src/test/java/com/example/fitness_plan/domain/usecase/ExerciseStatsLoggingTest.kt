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
import com.google.common.truth.Truth.assertThat

class ExerciseStatsLoggingTest {

    private lateinit var workoutUseCase: WorkoutUseCase
    private lateinit var mockWorkoutRepository: WorkoutRepository
    private lateinit var mockExerciseStatsRepository: ExerciseStatsRepository
    private lateinit var mockExerciseCompletionRepository: ExerciseCompletionRepository

    @org.junit.Before
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

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun `saveExerciseStats should create correct ExerciseStats with volume calculated`() = runTest {
        val username = "testuser"
        val exerciseName = "Squats"
        val weight = 80.0
        val reps = 12
        val setNumber = 1
        val sets = 3

        val expectedVolume = (weight * reps).toLong()

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
                    it.sets == sets &&
                    it.volume == expectedVolume
                }
            )
        }
    }

    @org.junit.Test
    fun `saveExerciseStats should calculate volume correctly for different weights and reps`() = runTest {
        val username = "testuser"
        val testData = listOf(
            Triple(100.0, 10, 1000L),
            Triple(50.0, 12, 600L),
            Triple(20.0, 15, 300L),
            Triple(80.5, 8, 644L)
        )

        testData.forEach { (weight, reps, expectedVolume) ->
            coEvery { mockExerciseStatsRepository.saveExerciseStats(any(), any()) } just runs

            workoutUseCase.saveExerciseStats(username, "TestExercise", weight, reps, 1, 3)

            coVerify {
                mockExerciseStatsRepository.saveExerciseStats(
                    username,
                    match<ExerciseStats> { it.volume == expectedVolume }
                )
            }

            clearMocks(mockExerciseStatsRepository)
        }
    }

    @org.junit.Test
    fun `saveExerciseStats should handle multiple sets correctly`() = runTest {
        val username = "testuser"
        val exerciseName = "BenchPress"
        val weight = 60.0
        val reps = 10
        val setNumber = 2
        val sets = 4

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

    @org.junit.Test
    fun `saveExerciseStats should be called once per invocation`() = runTest {
        val username = "testuser"

        coEvery { mockExerciseStatsRepository.saveExerciseStats(any(), any()) } just runs

        workoutUseCase.saveExerciseStats(username, "Squats", 80.0, 12, 1, 3)
        workoutUseCase.saveExerciseStats(username, "BenchPress", 60.0, 10, 1, 3)
        workoutUseCase.saveExerciseStats(username, "Deadlift", 100.0, 5, 1, 3)

        coVerify(exactly = 3) { mockExerciseStatsRepository.saveExerciseStats(any(), any()) }
    }

    @org.junit.Test
    fun `saveExerciseStats should handle zero weight and reps gracefully`() = runTest {
        val username = "testuser"

        coEvery { mockExerciseStatsRepository.saveExerciseStats(any(), any()) } just runs

        workoutUseCase.saveExerciseStats(username, "LightExercise", 0.0, 0, 1, 1)

        coVerify {
            mockExerciseStatsRepository.saveExerciseStats(
                username,
                match<ExerciseStats> {
                    it.weight == 0.0 &&
                    it.reps == 0 &&
                    it.volume == 0L
                }
            )
        }
    }

    @org.junit.Test
    fun `saveExerciseStats should set current timestamp`() = runTest {
        val username = "testuser"
        val beforeSave = System.currentTimeMillis()

        coEvery { mockExerciseStatsRepository.saveExerciseStats(any(), any()) } just runs

        workoutUseCase.saveExerciseStats(username, "Squats", 80.0, 12, 1, 3)

        val afterSave = System.currentTimeMillis()

        coVerify {
            mockExerciseStatsRepository.saveExerciseStats(
                username,
                match<ExerciseStats> {
                    it.date in beforeSave..afterSave
                }
            )
        }
    }

    @org.junit.Test
    fun `saveExerciseStats should preserve setNumber across multiple calls`() = runTest {
        val username = "testuser"

        coEvery { mockExerciseStatsRepository.saveExerciseStats(any(), any()) } just runs

        workoutUseCase.saveExerciseStats(username, "Squats", 80.0, 12, 1, 3)
        workoutUseCase.saveExerciseStats(username, "Squats", 80.0, 12, 2, 3)
        workoutUseCase.saveExerciseStats(username, "Squats", 80.0, 12, 3, 3)

        val slot = mutableListOf<ExerciseStats>()
        coEvery { mockExerciseStatsRepository.saveExerciseStats(any(), capture(slot)) } just runs

        workoutUseCase.saveExerciseStats(username, "Squats", 80.0, 12, 1, 3)
        workoutUseCase.saveExerciseStats(username, "Squats", 80.0, 12, 2, 3)
        workoutUseCase.saveExerciseStats(username, "Squats", 80.0, 12, 3, 3)

        assertThat(slot).hasSize(3)
        assertThat(slot[0].setNumber).isEqualTo(1)
        assertThat(slot[1].setNumber).isEqualTo(2)
        assertThat(slot[2].setNumber).isEqualTo(3)
    }

    @org.junit.Test
    fun `saveExerciseStats should handle decimal weights`() = runTest {
        val username = "testuser"
        val weight = 77.5
        val reps = 10
        val expectedVolume = 775L

        coEvery { mockExerciseStatsRepository.saveExerciseStats(any(), any()) } just runs

        workoutUseCase.saveExerciseStats(username, "Squats", weight, reps, 1, 3)

        coVerify {
            mockExerciseStatsRepository.saveExerciseStats(
                username,
                match<ExerciseStats> {
                    it.weight == weight &&
                    it.volume == expectedVolume
                }
            )
        }
    }
}
