package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.calculator.WeightCalculator
import com.example.fitness_plan.domain.calculator.WorkoutDateCalculator
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

private val Context.workoutDataStoreTest: DataStore<Preferences> by preferencesDataStore(name = "workout_plans_test")

class WorkoutRepositoryImplNewLogicTest {

    private lateinit var repository: WorkoutRepositoryImpl
    private lateinit var context: Context
    private lateinit var mockExerciseCompletionRepository: ExerciseCompletionRepository
    private lateinit var mockWorkoutScheduleRepository: WorkoutScheduleRepository
    private lateinit var mockWeightCalculator: WeightCalculator
    private lateinit var mockWorkoutDateCalculator: WorkoutDateCalculator
    private lateinit var mockExerciseLibraryRepository: ExerciseLibraryRepository

    @Before
    fun setup() {
        context = mockk<Context>(relaxed = true)
        mockExerciseCompletionRepository = mockk(relaxed = true)
        mockWorkoutScheduleRepository = mockk(relaxed = true)
        mockWeightCalculator = mockk(relaxed = true)
        mockWorkoutDateCalculator = mockk(relaxed = true)
        mockExerciseLibraryRepository = mockk(relaxed = true)

        every { context.dataDir } returns File(System.getProperty("java.io.tmpdir"))
        every { mockWeightCalculator.determineExerciseType(any()) } returns com.example.fitness_plan.domain.calculator.ExerciseType.COMPOUND
        every { mockWeightCalculator.calculateBaseWeight(any(), any(), any(), any(), any()) } returns 20.0f
        every { mockWeightCalculator.getRecommendedRepsString(any()) } returns "10-12"
        coEvery { mockExerciseLibraryRepository.getAllExercisesAsList() } returns emptyList()
        every { mockWorkoutDateCalculator.generateDates(any(), any(), any()) } returns listOf(1L, 2L, 3L, 4L, 5L)

        repository = WorkoutRepositoryImpl(
            context,
            mockExerciseCompletionRepository,
            mockWorkoutScheduleRepository,
            mockWeightCalculator,
            mockWorkoutDateCalculator,
            mockExerciseLibraryRepository
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `createWorkoutPlanWithNewLogic should not repeat strength exercises`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Похудение",
            level = "Любитель",
            frequency = "3 раза в неделю",
            weight = 75.0,
            height = 175.0,
            gender = "Мужской"
        )

        val plan = repository.getWorkoutPlanForUser(profile)

        val allStrengthExerciseNames = plan.days
            .filter { day ->
                day.exercises.any { it.exerciseType == ExerciseType.STRENGTH }
            }
            .flatMap { day ->
                day.exercises.filter { it.exerciseType == ExerciseType.STRENGTH }.map { it.name }
            }

        val uniqueStrengthExercises = allStrengthExerciseNames.toSet()

        assertThat(uniqueStrengthExercises.size).isEqualTo(allStrengthExerciseNames.size)
    }

    @Test
    fun `createWorkoutPlanWithNewLogic should have cardio day after 3 strength days`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Похудение",
            level = "Любитель",
            frequency = "3 раза в неделю",
            weight = 75.0,
            height = 175.0,
            gender = "Мужской"
        )

        val plan = repository.getWorkoutPlanForUser(profile)

        val cardioDayIndices = plan.days
            .mapIndexedNotNull { index, day ->
                if (day.exercises.any { it.exerciseType == ExerciseType.CARDIO }) index else null
            }

        assertThat(cardioDayIndices).containsExactly(3, 8)
    }

    @Test
    fun `createWorkoutPlanWithNewLogic should follow muscle group order`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Похудение",
            level = "Любитель",
            frequency = "3 раза в неделю",
            weight = 75.0,
            height = 175.0,
            gender = "Мужской"
        )

        val plan = repository.getWorkoutPlanForUser(profile)

        val expectedMuscleGroups = listOf("Руки", "Плечи", "Грудь", "Кардио", "Спина", "Ноги", "Руки", "Плечи", "Кардио", "Грудь")
        val actualMuscleGroups = plan.days.map { 
            it.dayName.split(": ").last()
        }

        assertThat(actualMuscleGroups).isEqualTo(expectedMuscleGroups)
    }

    @Test
    fun `createWorkoutPlanWithNewLogic should have 5-6 exercises in strength days`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Похудение",
            level = "Любитель",
            frequency = "3 раза в неделю",
            weight = 75.0,
            height = 175.0,
            gender = "Мужской"
        )

        val plan = repository.getWorkoutPlanForUser(profile)

        val strengthDays = plan.days.filter { day ->
            day.exercises.any { it.exerciseType == ExerciseType.STRENGTH }
        }

        strengthDays.forEach { day ->
            val strengthExerciseCount = day.exercises.count { it.exerciseType == ExerciseType.STRENGTH }
            assertThat(strengthExerciseCount).isAtLeast(5)
            assertThat(strengthExerciseCount).isAtMost(6)
        }
    }

    @Test
    fun `createWorkoutPlanWithNewLogic should have 1 exercise in cardio days`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Похудение",
            level = "Любитель",
            frequency = "3 раза в неделю",
            weight = 75.0,
            height = 175.0,
            gender = "Мужской"
        )

        val plan = repository.getWorkoutPlanForUser(profile)

        val cardioDays = plan.days.filter { day ->
            day.exercises.any { it.exerciseType == ExerciseType.CARDIO }
        }

        cardioDays.forEach { day ->
            assertThat(day.exercises.size).isEqualTo(1)
            assertThat(day.exercises.first().exerciseType).isEqualTo(ExerciseType.CARDIO)
        }
    }

    @Test
    fun `createWorkoutPlanWithNewLogic should adapt cardio duration by level`() = runTest {
        val levels = listOf("Новичок", "Любитель", "Профессионал")
        val expectedDurations = listOf("40 мин", "50 мин", "60 мин")

        levels.forEachIndexed { index, level ->
            val profile = UserProfile(
                username = "test_user",
                goal = "Похудение",
                level = level,
                frequency = "3 раза в неделю",
                weight = 75.0,
                height = 175.0,
                gender = "Мужской"
            )

            val plan = repository.getWorkoutPlanForUser(profile)

            val cardioDays = plan.days.filter { day ->
                day.exercises.any { it.exerciseType == ExerciseType.CARDIO }
            }

            cardioDays.forEach { day ->
                val cardioExercise = day.exercises.first { it.exerciseType == ExerciseType.CARDIO }
                assertThat(cardioExercise.reps).isEqualTo(expectedDurations[index])
            }
        }
    }

    @Test
    fun `createWorkoutPlanWithNewLogic should work for all goals`() = runTest {
        val goals = listOf("Похудение", "Наращивание мышечной массы", "Поддержание формы")

        goals.forEach { goal ->
            val profile = UserProfile(
                username = "test_user",
                goal = goal,
                level = "Любитель",
                frequency = "3 раза в неделю",
                weight = 75.0,
                height = 175.0,
                gender = "Мужской"
            )

            val plan = repository.getWorkoutPlanForUser(profile)

            assertThat(plan.days.size).isEqualTo(10)
            assertThat(plan.goal).isEqualTo(goal)

            val cardioDayIndices = plan.days
                .mapIndexedNotNull { index, day ->
                    if (day.exercises.any { it.exerciseType == ExerciseType.CARDIO }) index else null
                }

            assertThat(cardioDayIndices).containsExactly(3, 8)
        }
    }
}
