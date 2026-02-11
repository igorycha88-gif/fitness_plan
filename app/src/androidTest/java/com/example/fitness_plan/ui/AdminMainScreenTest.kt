package com.example.fitness_plan.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AdminMainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockProfileViewModel = mockk<com.example.fitness_plan.presentation.viewmodel.ProfileViewModel>()
    private val mockWorkoutViewModel = mockk<com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel>()

    private val isAdminFlow = MutableStateFlow(true)
    private val currentUsernameFlow = MutableStateFlow("admin")

    private val sampleExercises = listOf(
        ExerciseLibrary(
            id = "1",
            name = "Приседания",
            description = "Базовое упражнение",
            exerciseType = ExerciseType.STRENGTH,
            equipment = listOf(EquipmentType.BARBELL),
            muscleGroups = listOf(MuscleGroup.QUADS),
            difficulty = "Начальный",
            stepByStepInstructions = "Встаньте прямо",
            animationUrl = null,
            tipsAndAdvice = null,
            progressionAdvice = null
        )
    )

    @Before
    fun setup() {
        every { mockProfileViewModel.isAdmin } returns isAdminFlow as StateFlow<Boolean>
        every { mockProfileViewModel.currentUsername } returns currentUsernameFlow as StateFlow<String>
        every { mockProfileViewModel.logout() } returns Unit

        every { mockWorkoutViewModel.adminWorkoutPlan } returns MutableStateFlow(null) as StateFlow<com.example.fitness_plan.domain.model.WorkoutPlan?>
    }

    @Test
    fun adminMainScreen_shouldDisplayAllNavigationItems() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            AdminMainScreen(
                mainNavController = navController,
                profileViewModel = mockProfileViewModel,
                workoutViewModel = mockWorkoutViewModel
            )
        }

        composeTestRule.onNodeWithText("Главная").assertIsDisplayed()
        composeTestRule.onNodeWithText("Упражнения").assertIsDisplayed()
        composeTestRule.onNodeWithText("Профиль").assertIsDisplayed()
    }

    @Test
    fun adminMainScreen_exercisesTab_shouldNavigateToExerciseLibrary() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            AdminMainScreen(
                mainNavController = navController,
                profileViewModel = mockProfileViewModel,
                workoutViewModel = mockWorkoutViewModel
            )
        }

        composeTestRule.onNodeWithText("Упражнения").assertExists()
    }

    @Test
    fun adminMainScreen_homeTab_shouldDisplayAdminHomeScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            AdminMainScreen(
                mainNavController = navController,
                profileViewModel = mockProfileViewModel,
                workoutViewModel = mockWorkoutViewModel
            )
        }

        composeTestRule.onNodeWithText("Управление тренировочными планами").assertIsDisplayed()
    }
}
