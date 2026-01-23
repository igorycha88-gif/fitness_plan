package com.example.fitness_plan

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitness_plan.data.CredentialsRepository
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel
import com.example.fitness_plan.ui.LoginScreen
import com.example.fitness_plan.ui.MainScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
@HiltAndroidTest
class AdminIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val testDispatcherRule = MainDispatcherRule()

    private lateinit var mockCredentialsRepository: CredentialsRepository
    private lateinit var mockProfileViewModel: ProfileViewModel
    private lateinit var mockWorkoutViewModel: WorkoutViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private var loginSuccessCalled = false
    private var exerciseClickCalled = false
    private var clickedExercise: Exercise? = null

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockCredentialsRepository = mockk(relaxed = true)
        mockProfileViewModel = mockk(relaxed = true)
        mockWorkoutViewModel = mockk(relaxed = true)

        loginSuccessCalled = false
        exerciseClickCalled = false
        clickedExercise = null

        every { mockProfileViewModel.isAdmin } returns flowOf(false)
        every { mockProfileViewModel.getCredentials() } returns null
        every { mockWorkoutViewModel.adminWorkoutPlan } returns flowOf(null)
        every { mockWorkoutViewModel.isLoading } returns flowOf(false)
        every { mockWorkoutViewModel.currentWorkoutPlan } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Complete admin flow should work from login to plan creation`() {
        every { mockCredentialsRepository.verifyAdminPassword(any(), any()) } returns true
        every { mockProfileViewModel.verifyAdminPassword(any(), any()) } returns true
        coEvery { mockProfileViewModel.setIsAdmin(any()) } just Runs
        coEvery { mockProfileViewModel.setCurrentUsername(any()) } just Runs
        coEvery { mockWorkoutViewModel.createAdminPlan(any(), any()) } just Runs
        coEvery { mockWorkoutViewModel.addDayToAdminPlan(any()) } just Runs
        coEvery { mockWorkoutViewModel.addExerciseToDay(any(), any()) } just Runs
        coEvery { mockWorkoutViewModel.adminWorkoutPlan } returns flowOf(
            WorkoutPlan(
                id = "admin_plan",
                name = "Test Plan",
                description = "Test Description",
                muscleGroups = emptyList(),
                goal = "Admin",
                level = "Admin",
                days = emptyList()
            )
        )

        var isAdminFlow: Boolean? = null
        every { mockProfileViewModel.isAdmin } returns flowOf(true) andThen flowOf(isAdminFlow!!)

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Логин").performTextInput("admin")
        composeTestRule.onNodeWithText("Пароль").performTextInput("admin123")
        composeTestRule.onNodeWithText("Войти как администратор").performClick()
        composeTestRule.onNodeWithText("Войти").performClick()

        assertThat(loginSuccessCalled).isTrue()
        verify { mockProfileViewModel.verifyAdminPassword("admin", "admin123") }
        verify { mockProfileViewModel.setIsAdmin(true) }
    }

    @Test
    fun `Admin should be able to create complete workout plan`() {
        coEvery { mockWorkoutViewModel.createAdminPlan(any(), any()) } just Runs
        coEvery { mockWorkoutViewModel.addDayToAdminPlan(any()) } just Runs
        coEvery { mockWorkoutViewModel.addExerciseToDay(any(), any()) } just Runs
        coEvery { mockWorkoutViewModel.adminWorkoutPlan } returns flowOf(
            WorkoutPlan(
                id = "admin_plan",
                name = "Test Plan",
                description = "Test Description",
                muscleGroups = emptyList(),
                goal = "Admin",
                level = "Admin",
                days = emptyList()
            )
        )

        composeTestRule.setContent {
            com.example.fitness_plan.ui.AdminScreen(
                viewModel = mockWorkoutViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("Создать план").performClick()
        composeTestRule.onNodeWithText("Название плана").performTextInput("My Workout Plan")
        composeTestRule.onNodeWithText("Описание плана").performTextInput("Comprehensive plan for users")
        composeTestRule.onNodeWithText("Создать").performClick()

        verify { mockWorkoutViewModel.createAdminPlan("My Workout Plan", "Comprehensive plan for users") }

        val plan = mockWorkoutViewModel.adminWorkoutPlan.value
        assertThat(plan).isNotNull()
        assertThat(plan?.name).isEqualTo("My Workout Plan")
    }

    @Test
    fun `Admin should be able to add multiple workout days`() {
        val exercise1 = Exercise(
            id = "ex1",
            name = "Squats",
            sets = 3,
            reps = "12-15",
            weight = null,
            imageRes = null,
            isCompleted = false,
            alternatives = emptyList()
        )

        val day1 = WorkoutDay(
            id = 0,
            dayName = "Leg Day",
            exercises = listOf(exercise1),
            muscleGroups = emptyList()
        )

        coEvery { mockWorkoutViewModel.addDayToAdminPlan(any()) } just Runs
        coEvery { mockWorkoutViewModel.adminWorkoutPlan } returns flowOf(
            WorkoutPlan(
                id = "admin_plan",
                name = "Test Plan",
                description = "Test Description",
                muscleGroups = emptyList(),
                goal = "Admin",
                level = "Admin",
                days = listOf(day1)
            )
        )

        composeTestRule.setContent {
            com.example.fitness_plan.ui.AdminScreen(
                viewModel = mockWorkoutViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithContentDescription("Добавить день").performClick()
        composeTestRule.onNodeWithText("Название дня").performTextInput("Push Day")
        composeTestRule.onNodeWithText("Добавить").performClick()

        verify { mockWorkoutViewModel.addDayToAdminPlan("Push Day") }

        val plan = mockWorkoutViewModel.adminWorkoutPlan.value
        assertThat(plan?.days?.size).isEqualTo(2)
    }

    @Test
    fun `Admin should be able to add exercises to workout day`() {
        val exercise1 = Exercise(
            id = "ex1",
            name = "Squats",
            sets = 3,
            reps = "12-15",
            weight = null,
            imageRes = null,
            isCompleted = false,
            alternatives = emptyList()
        )

        val exercise2 = Exercise(
            id = "ex2",
            name = "Bench Press",
            sets = 3,
            reps = "10-12",
            weight = null,
            imageRes = null,
            isCompleted = false,
            alternatives = emptyList()
        )

        val day1 = WorkoutDay(
            id = 0,
            dayName = "Leg Day",
            exercises = listOf(exercise1),
            muscleGroups = emptyList()
        )

        coEvery { mockWorkoutViewModel.addExerciseToDay(any(), any()) } just Runs
        coEvery { mockWorkoutViewModel.adminWorkoutPlan } returns flowOf(
            WorkoutPlan(
                id = "admin_plan",
                name = "Test Plan",
                description = "Test Description",
                muscleGroups = emptyList(),
                goal = "Admin",
                level = "Admin",
                days = listOf(day1)
            )
        )

        composeTestRule.setContent {
            com.example.fitness_plan.ui.AdminScreen(
                viewModel = mockWorkoutViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("Добавить упражнение").performClick()
        composeTestRule.onNodeWithText("Bench Press (3×10-12)").performClick()

        verify { mockWorkoutViewModel.addExerciseToDay(0, exercise2) }

        val plan = mockWorkoutViewModel.adminWorkoutPlan.value
        assertThat(plan?.days?.get(0)?.exercises?.size).isEqualTo(2)
    }

    @Test
    fun `Admin should be able to navigate to exercise details`() {
        val exercise1 = Exercise(
            id = "ex1",
            name = "Squats",
            sets = 3,
            reps = "12-15",
            weight = null,
            imageRes = null,
            isCompleted = false,
            alternatives = emptyList()
        )

        val day1 = WorkoutDay(
            id = 0,
            dayName = "Leg Day",
            exercises = listOf(exercise1),
            muscleGroups = emptyList()
        )

        coEvery { mockWorkoutViewModel.adminWorkoutPlan } returns flowOf(
            WorkoutPlan(
                id = "admin_plan",
                name = "Test Plan",
                description = "Test Description",
                muscleGroups = emptyList(),
                goal = "Admin",
                level = "Admin",
                days = listOf(day1)
            )
        )

        composeTestRule.setContent {
            com.example.fitness_plan.ui.AdminScreen(
                viewModel = mockWorkoutViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("• Squats (3×12-15)").performClick()

        assertThat(exerciseClickCalled).isTrue()
        assertThat(clickedExercise?.name).isEqualTo("Squats")
        assertThat(clickedExercise?.sets).isEqualTo(3)
        assertThat(clickedExercise?.reps).isEqualTo("12-15")
    }

    @Test
    fun `Admin should be able to delete workout day`() {
        val day1 = WorkoutDay(
            id = 0,
            dayName = "Leg Day",
            exercises = emptyList(),
            muscleGroups = emptyList()
        )

        val day2 = WorkoutDay(
            id = 1,
            dayName = "Push Day",
            exercises = emptyList(),
            muscleGroups = emptyList()
        )

        coEvery { mockWorkoutViewModel.removeDayFromAdminPlan(any()) } just Runs
        coEvery { mockWorkoutViewModel.adminWorkoutPlan } returns flowOf(
            WorkoutPlan(
                id = "admin_plan",
                name = "Test Plan",
                description = "Test Description",
                muscleGroups = emptyList(),
                goal = "Admin",
                level = "Admin",
                days = listOf(day1, day2)
            )
        )

        composeTestRule.setContent {
            com.example.fitness_plan.ui.AdminScreen(
                viewModel = mockWorkoutViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithContentDescription("Удалить день").performClick()

        verify { mockWorkoutViewModel.removeDayFromAdminPlan(0) }

        val plan = mockWorkoutViewModel.adminWorkoutPlan.value
        assertThat(plan?.days?.size).isEqualTo(1)
        assertThat(plan?.days?.get(0)?.dayName).isEqualTo("Push Day")
    }
}

class MainDispatcherRule : androidx.compose.ui.test.junit4.MainDispatcherRule() {
    override val testDispatcher = UnconfinedTestDispatcher()
}
