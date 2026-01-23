package com.example.fitness_plan.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel
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
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AdminScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: WorkoutViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private var exerciseClickCalled = false
    private var clickedExercise: Exercise? = null

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockViewModel = mockk(relaxed = true)
        exerciseClickCalled = false
        clickedExercise = null

        coEvery { mockWorkoutUseCase.getAdminWorkoutPlan() } returns flowOf(null)
        every { mockViewModel.adminWorkoutPlan } returns flowOf(null)
        every { mockViewModel.isLoading } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `AdminScreen should display admin title`() {
        every { mockViewModel.adminWorkoutPlan } returns flowOf(null)
        every { mockViewModel.isLoading } returns flowOf(false)

        composeTestRule.setContent {
            AdminScreen(
                viewModel = mockViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("Администратор").assertExists()
    }

    @Test
    fun `AdminScreen should show plan not created message when adminPlan is null`() {
        every { mockViewModel.adminWorkoutPlan } returns flowOf(null)
        every { mockViewModel.isLoading } returns flowOf(false)

        composeTestRule.setContent {
            AdminScreen(
                viewModel = mockViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("Управление планами тренировок").assertExists()
        composeTestRule.onNodeWithText("План не создан").assertExists()
        composeTestRule.onNodeWithText("Создать план").assertExists()
    }

    @Test
    fun `AdminScreen should show create plan dialog when create plan button is clicked`() {
        every { mockViewModel.adminWorkoutPlan } returns flowOf(null)
        every { mockViewModel.isLoading } returns flowOf(false)
        coEvery { mockViewModel.createAdminPlan(any(), any()) } just Runs

        composeTestRule.setContent {
            AdminScreen(
                viewModel = mockViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("Создать план").performClick()
        composeTestRule.onNodeWithText("Создать план").assertExists()
    }

    @Test
    fun `AdminScreen should create plan when dialog is confirmed`() {
        every { mockViewModel.adminWorkoutPlan } returns flowOf(null)
        every { mockViewModel.isLoading } returns flowOf(false)
        coEvery { mockViewModel.createAdminPlan(any(), any()) } just Runs
        coEvery { mockViewModel.adminWorkoutPlan } returns flowOf(
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
            AdminScreen(
                viewModel = mockViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("Создать план").performClick()
        composeTestRule.onNodeWithText("Название плана").performTextInput("Test Plan")
        composeTestRule.onNodeWithText("Описание плана").performTextInput("Test Description")
        composeTestRule.onNodeWithText("Создать").performClick()

        verify { mockViewModel.createAdminPlan("Test Plan", "Test Description") }
    }

    @Test
    fun `AdminScreen should show plan name when plan exists`() {
        val adminPlan = WorkoutPlan(
            id = "admin_plan",
            name = "My Admin Plan",
            description = "Admin Description",
            muscleGroups = emptyList(),
            goal = "Admin",
            level = "Admin",
            days = listOf(
                WorkoutDay(
                    id = 0,
                    dayName = "Leg Day",
                    exercises = emptyList(),
                    muscleGroups = emptyList()
                )
            )
        )

        every { mockViewModel.adminWorkoutPlan } returns flowOf(adminPlan)
        every { mockViewModel.isLoading } returns flowOf(false)

        composeTestRule.setContent {
            AdminScreen(
                viewModel = mockViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("My Admin Plan").assertExists()
    }

    @Test
    fun `AdminScreen should show workout day card`() {
        val adminPlan = WorkoutPlan(
            id = "admin_plan",
            name = "My Admin Plan",
            description = "Admin Description",
            muscleGroups = emptyList(),
            goal = "Admin",
            level = "Admin",
            days = listOf(
                WorkoutDay(
                    id = 0,
                    dayName = "Leg Day",
                    exercises = emptyList(),
                    muscleGroups = emptyList()
                )
            )
        )

        every { mockViewModel.adminWorkoutPlan } returns flowOf(adminPlan)
        every { mockViewModel.isLoading } returns flowOf(false)

        composeTestRule.setContent {
            AdminScreen(
                viewModel = mockViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("Leg Day").assertExists()
        composeTestRule.onNodeWithText("0 упражнений").assertExists()
    }

    @Test
    fun `AdminScreen should add day to plan`() {
        val adminPlan = WorkoutPlan(
            id = "admin_plan",
            name = "My Admin Plan",
            description = "Admin Description",
            muscleGroups = emptyList(),
            goal = "Admin",
            level = "Admin",
            days = emptyList()
        )

        every { mockViewModel.adminWorkoutPlan } returns flowOf(adminPlan)
        every { mockViewModel.isLoading } returns flowOf(false)
        coEvery { mockViewModel.addDayToAdminPlan(any()) } just Runs

        composeTestRule.setContent {
            AdminScreen(
                viewModel = mockViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithContentDescription("Добавить день").performClick()
        composeTestRule.onNodeWithText("Название дня").performTextInput("Leg Day")
        composeTestRule.onNodeWithText("Добавить").performClick()

        verify { mockViewModel.addDayToAdminPlan("Leg Day") }
    }

    @Test
    fun `AdminScreen should call onExerciseClick when exercise is clicked`() {
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

        val adminPlan = WorkoutPlan(
            id = "admin_plan",
            name = "My Admin Plan",
            description = "Admin Description",
            muscleGroups = emptyList(),
            goal = "Admin",
            level = "Admin",
            days = listOf(
                WorkoutDay(
                    id = 0,
                    dayName = "Leg Day",
                    exercises = listOf(exercise1),
                    muscleGroups = emptyList()
                )
            )
        )

        every { mockViewModel.adminWorkoutPlan } returns flowOf(adminPlan)
        every { mockViewModel.isLoading } returns flowOf(false)

        composeTestRule.setContent {
            AdminScreen(
                viewModel = mockViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("• Squats (3×12-15)").performClick()

        assertThat(exerciseClickCalled).isTrue()
        assertThat(clickedExercise?.name).isEqualTo("Squats")
    }

    @Test
    fun `AdminScreen should show add exercise button in workout day`() {
        val adminPlan = WorkoutPlan(
            id = "admin_plan",
            name = "My Admin Plan",
            description = "Admin Description",
            muscleGroups = emptyList(),
            goal = "Admin",
            level = "Admin",
            days = listOf(
                WorkoutDay(
                    id = 0,
                    dayName = "Leg Day",
                    exercises = emptyList(),
                    muscleGroups = emptyList()
                )
            )
        )

        every { mockViewModel.adminWorkoutPlan } returns flowOf(adminPlan)
        every { mockViewModel.isLoading } returns flowOf(false)

        composeTestRule.setContent {
            AdminScreen(
                viewModel = mockViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithText("Добавить упражнение").assertExists()
    }

    @Test
    fun `AdminScreen should show delete button in workout day`() {
        val adminPlan = WorkoutPlan(
            id = "admin_plan",
            name = "My Admin Plan",
            description = "Admin Description",
            muscleGroups = emptyList(),
            goal = "Admin",
            level = "Admin",
            days = listOf(
                WorkoutDay(
                    id = 0,
                    dayName = "Leg Day",
                    exercises = emptyList(),
                    muscleGroups = emptyList()
                )
            )
        )

        every { mockViewModel.adminWorkoutPlan } returns flowOf(adminPlan)
        every { mockViewModel.isLoading } returns flowOf(false)

        composeTestRule.setContent {
            AdminScreen(
                viewModel = mockViewModel,
                onExerciseClick = { exercise ->
                    exerciseClickCalled = true
                    clickedExercise = exercise
                }
            )
        }

        composeTestRule.onNodeWithContentDescription("Удалить день").assertExists()
    }
}
