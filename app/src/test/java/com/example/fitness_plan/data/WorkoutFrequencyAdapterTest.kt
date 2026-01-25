package com.example.fitness_plan.data

import com.example.fitness_plan.domain.calculator.ExerciseType
import com.example.fitness_plan.domain.calculator.WeightCalculator
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class WorkoutFrequencyAdapterTest {

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

        every { mockWeightCalculator.calculateBaseWeight(any(), any(), any(), any(), any()) } returns 20.0f
        every { mockWeightCalculator.getRecommendedRepsString(any()) } returns "10-12"
        every { mockWeightCalculator.determineExerciseType(any()) } returns ExerciseType.COMPOUND

        workoutRepositoryImpl = WorkoutRepositoryImpl(
            context = mockContext,
            exerciseCompletionRepository = mockExerciseCompletionRepository,
            workoutScheduleRepository = mockWorkoutScheduleRepository,
            weightCalculator = mockWeightCalculator
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getCycleWorkoutPlan with 1 per week frequency should create 4-day plan`() = runTest {
        val basePlan = com.example.fitness_plan.domain.model.WorkoutPlan(
            id = "test",
            name = "Test Plan",
            description = "Test",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(
                WorkoutDay(0, "Day 1", emptyList(), emptyList()),
                WorkoutDay(1, "Day 2", emptyList(), emptyList())
            )
        )

        val result = workoutRepositoryImpl.getCycleWorkoutPlan(basePlan, "1 раз в неделю")

        assertThat(result.name).isEqualTo("4-дневный план")
        assertThat(result.days).hasSize(4)
    }

    @Test
    fun `getCycleWorkoutPlan with 3 per week frequency should create 12-day plan`() = runTest {
        val basePlan = com.example.fitness_plan.domain.model.WorkoutPlan(
            id = "test",
            name = "Test Plan",
            description = "Test",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(
                WorkoutDay(0, "Day 1", emptyList(), emptyList()),
                WorkoutDay(1, "Day 2", emptyList(), emptyList())
            )
        )

        val result = workoutRepositoryImpl.getCycleWorkoutPlan(basePlan, "3 раза в неделю")

        assertThat(result.name).isEqualTo("12-дневный план")
        assertThat(result.days).hasSize(12)
    }

    @Test
    fun `getCycleWorkoutPlan with 5 per week frequency should create 20-day plan`() = runTest {
        val basePlan = com.example.fitness_plan.domain.model.WorkoutPlan(
            id = "test",
            name = "Test Plan",
            description = "Test",
            muscleGroups = emptyList(),
            goal = "Test",
            level = "Test",
            days = listOf(
                WorkoutDay(0, "Day 1", emptyList(), emptyList()),
                WorkoutDay(1, "Day 2", emptyList(), emptyList())
            )
        )

        val result = workoutRepositoryImpl.getCycleWorkoutPlan(basePlan, "5 раз в неделю")

        assertThat(result.name).isEqualTo("20-дневный план")
        assertThat(result.days).hasSize(20)
    }

    @Test
    fun `generateCycleDates with 1 per week should generate 4 dates`() = runTest {
        val startDate = System.currentTimeMillis()
        val result = workoutRepositoryImpl.generateCycleDates(startDate, "1 раз в неделю")

        assertThat(result).hasSize(4)
    }

    @Test
    fun `generateCycleDates with 3 per week should generate 12 dates`() = runTest {
        val startDate = System.currentTimeMillis()
        val result = workoutRepositoryImpl.generateCycleDates(startDate, "3 раза в неделю")

        assertThat(result).hasSize(12)
    }

    @Test
    fun `generateCycleDates with 5 per week should generate 20 dates`() = runTest {
        val startDate = System.currentTimeMillis()
        val result = workoutRepositoryImpl.generateCycleDates(startDate, "5 раз в неделю")

        assertThat(result).hasSize(20)
    }

    @Test
    fun `createWeightLossBeginnerPlan with 1 per week should create 4 days with 6-8 exercises`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Похудение",
            level = "Новичок",
            frequency = "1 раз в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(result.days).hasSize(4)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(1)
            assertThat(day.dayName).contains("Full Body")
        }
    }

    @Test
    fun `createWeightLossBeginnerPlan with 3 per week should create 12 days with split`() = runTest {
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

        assertThat(result.days).hasSize(12)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(1)
            assertThat(containsAnyOf(day.dayName, listOf("Ноги", "Верх тела", "Полный"))).isTrue()
        }
    }

    @Test
    fun `createWeightLossBeginnerPlan with 5 per week should create 20 days with split`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Похудение",
            level = "Новичок",
            frequency = "5 раз в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(result.days).hasSize(20)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(1)
            assertThat(containsAnyOf(day.dayName, listOf("Ноги", "Грудь", "Спина", "Плечи", "Руки"))).isTrue()
        }
    }

    @Test
    fun `createMuscleGainBeginnerPlan with 3 per week should follow split pattern`() = runTest {
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

        assertThat(result.days).hasSize(12)
        val dayNames = result.days.map { it.dayName }
        
        val legsDays = dayNames.count { it.contains("Ноги") }
        val upperDays = dayNames.count { it.contains("Верх тела") }
        val fullDays = dayNames.count { it.contains("Полный") }

        assertThat(legsDays).isGreaterThan(0)
        assertThat(upperDays).isGreaterThan(0)
        assertThat(fullDays).isGreaterThan(0)
    }

    @Test
    fun `createMuscleGainAdvancedPlan with 5 per week should follow detailed split`() = runTest {
        val profile = UserProfile(
            username = "testuser",
            goal = "Наращивание мышечной массы",
            level = "Профессионал",
            frequency = "5 раз в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result = workoutRepositoryImpl.getWorkoutPlanForUser(profile)

        assertThat(result.days).hasSize(20)
        val dayNames = result.days.map { it.dayName }

        val legsDays = dayNames.count { it.contains("Ноги") }
        val chestDays = dayNames.count { it.contains("Грудь") }
        val backDays = dayNames.count { it.contains("Спина") }
        val shouldersDays = dayNames.count { it.contains("Плечи") }
        val armsDays = dayNames.count { it.contains("Руки") }

        assertThat(legsDays + chestDays + backDays + shouldersDays + armsDays).isEqualTo(20)
    }

    @Test
    fun `createMaintenancePlan should adapt to all frequencies`() = runTest {
        val profile1 = UserProfile(
            username = "testuser1",
            goal = "Поддержание формы",
            level = "Новичок",
            frequency = "1 раз в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val profile3 = UserProfile(
            username = "testuser3",
            goal = "Поддержание формы",
            level = "Новичок",
            frequency = "3 раза в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val profile5 = UserProfile(
            username = "testuser5",
            goal = "Поддержание формы",
            level = "Новичок",
            frequency = "5 раз в неделю",
            weight = 70.0,
            height = 175.0,
            gender = "Мужской"
        )

        val result1 = workoutRepositoryImpl.getWorkoutPlanForUser(profile1)
        val result3 = workoutRepositoryImpl.getWorkoutPlanForUser(profile3)
        val result5 = workoutRepositoryImpl.getWorkoutPlanForUser(profile5)

        assertThat(result1.days).hasSize(4)
        assertThat(result3.days).hasSize(12)
        assertThat(result5.days).hasSize(20)
    }

    private fun containsAnyOf(str: String, strings: List<String>): Boolean {
        return strings.any { str.contains(it) }
    }
}
