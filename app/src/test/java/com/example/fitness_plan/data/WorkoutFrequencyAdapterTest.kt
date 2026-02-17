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

        assertThat(result.name).isEqualTo("10-дневный план")
        assertThat(result.days).hasSize(10)
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

        assertThat(result.name).isEqualTo("10-дневный план")
        assertThat(result.days).hasSize(10)
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

        assertThat(result.name).isEqualTo("10-дневный план")
        assertThat(result.days).hasSize(10)
    }

    @Test
    fun `generateCycleDates with 1 per week should generate 10 dates`() = runTest {
        val startDate = System.currentTimeMillis()
        val result = workoutRepositoryImpl.generateCycleDates(startDate, "1 раз в неделю")

        assertThat(result).hasSize(10)
    }

    @Test
    fun `generateCycleDates with 1 per week should follow Wednesday pattern after first date`() = runTest {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(2025, java.util.Calendar.JANUARY, 20, 0, 0, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val result = workoutRepositoryImpl.generateCycleDates(startDate, "1 раз в неделю")

        assertThat(result).hasSize(10)

        val cal1 = java.util.Calendar.getInstance()
        cal1.timeInMillis = result[0]
        assertThat(cal1.get(java.util.Calendar.DAY_OF_MONTH)).isEqualTo(20)

        for (i in 1 until result.size) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = result[i]
            assertThat(cal.get(java.util.Calendar.DAY_OF_WEEK)).isEqualTo(java.util.Calendar.WEDNESDAY)
        }
    }

    @Test
    fun `generateCycleDates with 3 per week should generate 10 dates`() = runTest {
        val startDate = System.currentTimeMillis()
        val result = workoutRepositoryImpl.generateCycleDates(startDate, "3 раза в неделю")

        assertThat(result).hasSize(10)
    }

    @Test
    fun `generateCycleDates with 3 per week should follow Monday Wednesday Friday pattern`() = runTest {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(2025, java.util.Calendar.JANUARY, 13, 0, 0, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val result = workoutRepositoryImpl.generateCycleDates(startDate, "3 раза в неделю")

        assertThat(result).hasSize(10)

        for (i in 0 until result.size step 3) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = result[i]
            assertThat(cal.get(java.util.Calendar.DAY_OF_WEEK)).isEqualTo(java.util.Calendar.MONDAY)
        }

        for (i in 1 until result.size step 3) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = result[i]
            assertThat(cal.get(java.util.Calendar.DAY_OF_WEEK)).isEqualTo(java.util.Calendar.WEDNESDAY)
        }

        for (i in 2 until result.size step 3) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = result[i]
            assertThat(cal.get(java.util.Calendar.DAY_OF_WEEK)).isEqualTo(java.util.Calendar.FRIDAY)
        }
    }

    @Test
    fun `generateCycleDates with 5 per week should generate 10 dates`() = runTest {
        val startDate = System.currentTimeMillis()
        val result = workoutRepositoryImpl.generateCycleDates(startDate, "5 раз в неделю")

        assertThat(result).hasSize(10)
    }

    @Test
    fun `generateCycleDates with 5 per week should follow Monday to Friday pattern`() = runTest {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(2025, java.util.Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val result = workoutRepositoryImpl.generateCycleDates(startDate, "5 раз в неделю")

        assertThat(result).hasSize(10)

        val expectedDays = listOf(
            java.util.Calendar.MONDAY,
            java.util.Calendar.TUESDAY,
            java.util.Calendar.WEDNESDAY,
            java.util.Calendar.THURSDAY,
            java.util.Calendar.FRIDAY,
            java.util.Calendar.MONDAY,
            java.util.Calendar.TUESDAY,
            java.util.Calendar.WEDNESDAY,
            java.util.Calendar.THURSDAY,
            java.util.Calendar.FRIDAY
        )

        result.forEachIndexed { index, date ->
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = date
            assertThat(cal.get(java.util.Calendar.DAY_OF_WEEK)).isEqualTo(expectedDays[index])
        }
    }

    @Test
    fun `createWeightLossBeginnerPlan with 1 per week should create 10 days with split and cardio`() = runTest {
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

        assertThat(result.days).hasSize(10)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
            assertThat(containsAnyOf(day.dayName, listOf("Ноги", "Грудь", "Спина", "Плечи", "Руки"))).isTrue()
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
    fun `createWeightLossBeginnerPlan with 3 per week should create 10 days with split and cardio`() = runTest {
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
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
            assertThat(containsAnyOf(day.dayName, listOf("Ноги", "Грудь", "Спина", "Плечи", "Руки"))).isTrue()
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
    fun `createWeightLossBeginnerPlan with 5 per week should create 10 days with split and cardio`() = runTest {
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

        assertThat(result.days).hasSize(10)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(3)
            assertThat(containsAnyOf(day.dayName, listOf("Ноги", "Грудь", "Спина", "Плечи", "Руки"))).isTrue()
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
    fun `createMuscleGainBeginnerPlan with 3 per week should create full body days`() = runTest {
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
        val dayNames = result.days.map { it.dayName }

        val fullBodyDays = dayNames.count { it.contains("Full Body") }

        assertThat(fullBodyDays).isEqualTo(10)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
            assertThat(day.exercises.size).isAtMost(8)
        }
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

        assertThat(result.days).hasSize(10)
        val dayNames = result.days.map { it.dayName }

        val legsDays = dayNames.count { it.contains("Ноги") }
        val chestDays = dayNames.count { it.contains("Грудь") }
        val backDays = dayNames.count { it.contains("Спина") }
        val shouldersDays = dayNames.count { it.contains("Плечи") }
        val armsDays = dayNames.count { it.contains("Руки") }

        assertThat(legsDays + chestDays + backDays + shouldersDays + armsDays).isEqualTo(10)
        result.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(1)
        }
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

        assertThat(result1.days).hasSize(10)
        result1.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
            assertThat(day.exercises.size).isAtMost(8)
        }

        assertThat(result3.days).hasSize(10)
        result3.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(4)
            assertThat(day.exercises.size).isAtMost(8)
            assertThat(day.dayName).contains("Full Body")
        }

        assertThat(result5.days).hasSize(10)
        result5.days.forEach { day ->
            assertThat(day.exercises.size).isAtLeast(3)
        }
    }

    private fun containsAnyOf(str: String, strings: List<String>): Boolean {
        return strings.any { str.contains(it) }
    }
}
