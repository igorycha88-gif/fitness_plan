package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.calculator.WeightCalculator
import com.example.fitness_plan.domain.calculator.WorkoutDateCalculator
import com.example.fitness_plan.domain.model.PlanType
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import java.io.File

private val Context.workoutDataStoreTest: DataStore<Preferences> by preferencesDataStore(name = "workout_plans_test")

class WorkoutRepositoryPlanTypeTest {

    private lateinit var repository: WorkoutRepositoryImpl
    private lateinit var context: Context
    private lateinit var mockExerciseCompletionRepository: ExerciseCompletionRepository
    private lateinit var mockWorkoutScheduleRepository: com.example.fitness_plan.data.WorkoutScheduleRepository
    private lateinit var mockWeightCalculator: WeightCalculator
    private lateinit var mockWorkoutDateCalculator: WorkoutDateCalculator
    private lateinit var mockExerciseLibraryRepository: ExerciseLibraryRepository

    @Before
    fun setup() {
        context = mockk<Context>(relaxed = true)
        mockExerciseCompletionRepository = mockk(relaxed = true)
        mockWorkoutScheduleRepository = mockk<com.example.fitness_plan.data.WorkoutScheduleRepository>(relaxed = true)
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
    fun `getWorkoutPlanForUser with weight loss beginner should include PlanType AUTO`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Похудение",
            level = "Новичок",
            frequency = "3 раза в неделю",
            weight = 80.0,
            height = 180.0,
            gender = "Мужской"
        )

        val plan = repository.getWorkoutPlanForUser(profile)

        assertThat(plan.planType).isEqualTo(PlanType.AUTO)
    }

    @Test
    fun `getWorkoutPlanForUser with muscle gain beginner should include PlanType AUTO`() = runTest {
        val profile = UserProfile(
            username = "test_user",
            goal = "Наращивание мышечной массы",
            level = "Новичок",
            frequency = "3 раза в неделю",
            weight = 80.0,
            height = 180.0,
            gender = "Мужской"
        )

        val plan = repository.getWorkoutPlanForUser(profile)

        assertThat(plan.planType).isEqualTo(PlanType.AUTO)
    }
}
