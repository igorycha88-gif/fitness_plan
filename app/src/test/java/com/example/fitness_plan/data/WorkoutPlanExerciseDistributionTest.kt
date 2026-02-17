package com.example.fitness_plan.data

import com.example.fitness_plan.domain.calculator.ExerciseType
import com.example.fitness_plan.domain.calculator.WeightCalculator
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class WorkoutPlanExerciseDistributionTest {

    private lateinit var workoutRepositoryImpl: WorkoutRepositoryImpl
    private lateinit var mockContext: android.content.Context
    private lateinit var mockExerciseCompletionRepository: ExerciseCompletionRepository
    private lateinit var mockWorkoutScheduleRepository: WorkoutScheduleRepository
    private lateinit var mockWeightCalculator: WeightCalculator

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockExerciseCompletionRepository = mockk(relaxed = true)
        mockWorkoutScheduleRepository = mockk(relaxed = true)
        mockWeightCalculator = mockk(relaxed = true)
        val mockExerciseLibraryRepository = mockk<com.example.fitness_plan.domain.repository.ExerciseLibraryRepository>(relaxed = true)
        val workoutDateCalculator = com.example.fitness_plan.domain.calculator.WorkoutDateCalculator()
        val mockExercisePoolManager = mockk<com.example.fitness_plan.domain.usecase.ExercisePoolManager>(relaxed = true)

        every { mockWeightCalculator.calculateBaseWeight(any(), any(), any(), any(), any()) } returns 20.0f
        every { mockWeightCalculator.getRecommendedRepsString(any()) } returns "10-12"
        every { mockWeightCalculator.determineExerciseType(any()) } returns ExerciseType.COMPOUND
        coEvery { mockExerciseLibraryRepository.getAllExercisesAsList() } returns emptyList()

        workoutRepositoryImpl = WorkoutRepositoryImpl(
            context = mockContext,
            exerciseCompletionRepository = mockExerciseCompletionRepository,
            workoutScheduleRepository = mockWorkoutScheduleRepository,
            weightCalculator = mockWeightCalculator,
            workoutDateCalculator = workoutDateCalculator,
            exerciseLibraryRepository = mockExerciseLibraryRepository,
            exercisePoolManager = mockExercisePoolManager
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Weight Loss Beginner should have split days with cardio`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Похудение",
            level = "Новичок",
            frequency = "3 раза в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(result.days).hasSize(10)
        assertThat(result.name).contains("Похудение: Новичок")

        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
            assertThat(day.dayName).isNotEmpty()
        }

        val legsDays = result.days.filter { it.dayName.contains("Ноги") }
        val chestDays = result.days.filter { it.dayName.contains("Грудь") }
        val backDays = result.days.filter { it.dayName.contains("Спина") }
        val shouldersDays = result.days.filter { it.dayName.contains("Плечи") }
        val armsDays = result.days.filter { it.dayName.contains("Руки") }

        assertThat(legsDays.size).isEqualTo(2)
        assertThat(chestDays.size).isEqualTo(2)
        assertThat(backDays.size).isEqualTo(2)
        assertThat(shouldersDays.size).isEqualTo(2)
        assertThat(armsDays.size).isEqualTo(2)
    }

    @Test
    fun `Weight Loss Intermediate should have split days with cardio`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Похудение",
            level = "Любитель",
            frequency = "3 раза в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(result.days).hasSize(10)
        assertThat(result.name).contains("Похудение: Любитель")

        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
        }

        val legsDays = result.days.filter { it.dayName.contains("Ноги") }
        val chestDays = result.days.filter { it.dayName.contains("Грудь") }
        val backDays = result.days.filter { it.dayName.contains("Спина") }
        val shouldersDays = result.days.filter { it.dayName.contains("Плечи") }
        val armsDays = result.days.filter { it.dayName.contains("Руки") }

        assertThat(legsDays.size).isEqualTo(2)
        assertThat(chestDays.size).isEqualTo(2)
        assertThat(backDays.size).isEqualTo(2)
        assertThat(shouldersDays.size).isEqualTo(2)
        assertThat(armsDays.size).isEqualTo(2)
    }

    @Test
    fun `Weight Loss Advanced should have split days with cardio`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Похудение",
            level = "Профессионал",
            frequency = "3 раза в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(result.days).hasSize(10)
        assertThat(result.name).contains("Похудение: Профессионал")

        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
        }

        val legsDays = result.days.filter { it.dayName.contains("Ноги") }
        val chestDays = result.days.filter { it.dayName.contains("Грудь") }
        val backDays = result.days.filter { it.dayName.contains("Спина") }
        val shouldersDays = result.days.filter { it.dayName.contains("Плечи") }
        val armsDays = result.days.filter { it.dayName.contains("Руки") }

        assertThat(legsDays.size).isEqualTo(2)
        assertThat(chestDays.size).isEqualTo(2)
        assertThat(backDays.size).isEqualTo(2)
        assertThat(shouldersDays.size).isEqualTo(2)
        assertThat(armsDays.size).isEqualTo(2)
    }

    @Test
    fun `Weight Loss plan should have unique exercises across 10 days`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Похудение",
            level = "Новичок",
            frequency = "3 раза в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        val allExerciseNames = result.days.flatMap { day ->
            day.exercises.map { it.name }
        }.filter { !it.contains("Бег") && !it.contains("Велотренажёр") && !it.contains("Эллипсоид") && !it.contains("Гребной") && !it.contains("HIIT") }

        val exerciseCountMap = allExerciseNames.groupingBy { it }.eachCount()

        exerciseCountMap.forEach { (exerciseName, count) ->
            assertThat(count).isAtMost(2)
        }
    }

    @Test
    fun `Weight Loss plan should have cardio exercises in each day`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Похудение",
            level = "Новичок",
            frequency = "3 раза в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        result.days.forEach { day ->
            val cardioExercises = day.exercises.filter {
                it.name.contains("Бег") ||
                it.name.contains("Велотренажёр") ||
                it.name.contains("Эллипсоид") ||
                it.name.contains("Гребной") ||
                it.name.contains("HIIT")
            }
            assertThat(cardioExercises.size).isAtLeast(1)
        }
    }

    @Test
    fun `Muscle Gain Beginner 3x per week should have minimum 5 exercises per day`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Наращивание мышечной массы",
            level = "Новичок",
            frequency = "3 раза в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(result.days).hasSize(10)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
            assertThat(day.exercises.size).isAtMost(8)
            assertThat(day.dayName).contains("Full Body")
        }
    }

    @Test
    fun `Muscle Gain Intermediate 3x per week should have minimum 5 exercises per day`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Наращивание мышечной массы",
            level = "Любитель",
            frequency = "3 раза в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(result.days).hasSize(10)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
            assertThat(day.exercises.size).isAtMost(8)
            assertThat(day.dayName).contains("Full Body")
        }
    }

    @Test
    fun `Muscle Gain Advanced 3x per week should have minimum 5 exercises per day`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Наращивание мышечной массы",
            level = "Профессионал",
            frequency = "3 раза в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(result.days).hasSize(10)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
            assertThat(day.exercises.size).isAtMost(8)
            assertThat(day.dayName).contains("Full Body")
        }
    }

    @Test
    fun `Maintenance 3x per week should have minimum5 exercises per day`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Поддержание формы",
            level = "Новичок",
            frequency = "3 раза в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(result.days).hasSize(10)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
            assertThat(day.exercises.size).isAtMost(8)
            assertThat(day.dayName).contains("Full Body")
        }
    }
}
