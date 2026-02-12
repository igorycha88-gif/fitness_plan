package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.PlanType
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.WorkoutRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class WorkoutUseCasePlanTypeTest {

    private lateinit var workoutUseCase: WorkoutUseCase
    private lateinit var mockWorkoutRepository: WorkoutRepository
    private lateinit var mockExerciseStatsRepository: ExerciseStatsRepository
    private lateinit var mockExerciseCompletionRepository: ExerciseCompletionRepository
    private lateinit var mockMuscleGroupStatsUseCase: MuscleGroupStatsUseCase

    @Before
    fun setup() {
        mockWorkoutRepository = mockk(relaxed = true)
        mockExerciseStatsRepository = mockk(relaxed = true)
        mockExerciseCompletionRepository = mockk(relaxed = true)
        mockMuscleGroupStatsUseCase = mockk(relaxed = true)

        workoutUseCase = WorkoutUseCase(
            mockWorkoutRepository,
            mockExerciseStatsRepository,
            mockExerciseCompletionRepository,
            mockMuscleGroupStatsUseCase
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `createUserPlan should create plan with PlanType USER`() = runTest {
        val username = "test_user"
        val planName = "My Custom Plan"
        val planDescription = "My custom workout plan"

        coEvery { mockWorkoutRepository.saveUserWorkoutPlan(any(), any()) } just runs

        workoutUseCase.createUserPlan(username, planName, planDescription)

        coVerify {
            mockWorkoutRepository.saveUserWorkoutPlan(
                username,
                match<WorkoutPlan> {
                    it.id == "${username}_user_plan" &&
                    it.name == planName &&
                    it.description == planDescription &&
                    it.planType == PlanType.USER &&
                    it.goal == "Custom" &&
                    it.level == "Custom" &&
                    it.days.isEmpty()
                }
            )
        }
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
            days = emptyList(),
            planType = PlanType.ADMIN
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
            days = emptyList(),
            planType = PlanType.ADMIN
        )

        coEvery { mockWorkoutRepository.getAdminWorkoutPlan() } returns flowOf(adminPlan)

        val result = workoutUseCase.getAdminWorkoutPlan().first()

        assertThat(result).isEqualTo(adminPlan)
        assertThat(result?.planType).isEqualTo(PlanType.ADMIN)
        coVerify { mockWorkoutRepository.getAdminWorkoutPlan() }
    }

    @Test
    fun `getUserWorkoutPlan should return user plan flow`() = runTest {
        val username = "test_user"
        val userPlan = WorkoutPlan(
            id = "${username}_user_plan",
            name = "My Plan",
            description = "My Description",
            muscleGroups = listOf("Legs", "Chest"),
            goal = "Custom",
            level = "Custom",
            days = emptyList(),
            planType = PlanType.USER
        )

        coEvery { mockWorkoutRepository.getUserWorkoutPlan(username) } returns flowOf(userPlan)

        val result = workoutUseCase.getUserWorkoutPlan(username).first()

        assertThat(result).isEqualTo(userPlan)
        assertThat(result?.planType).isEqualTo(PlanType.USER)
        coVerify { mockWorkoutRepository.getUserWorkoutPlan(username) }
    }
}
