package com.example.fitness_plan.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ExerciseLibraryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockExerciseLibraryViewModel = mockk<com.example.fitness_plan.presentation.viewmodel.ExerciseLibraryViewModel>()
    private val mockProfileViewModel = mockk<com.example.fitness_plan.presentation.viewmodel.ProfileViewModel>()

    private val sampleExercises = listOf(
        ExerciseLibrary(
            id = "1",
            name = "Приседания",
            description = "Базовое упражнение для ног",
            exerciseType = ExerciseType.STRENGTH,
            equipment = listOf(EquipmentType.BARBELL),
            muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            difficulty = "Начальный",
            stepByStepInstructions = "Встаньте прямо",
            animationUrl = null,
            tipsAndAdvice = null,
            progressionAdvice = null
        ),
        ExerciseLibrary(
            id = "2",
            name = "Жим лёжа",
            description = "Упражнение для груди",
            exerciseType = ExerciseType.STRENGTH,
            equipment = listOf(EquipmentType.BARBELL, EquipmentType.SPECIAL_BENCH),
            muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
            difficulty = "Средний",
            stepByStepInstructions = "Лягте на скамью",
            animationUrl = null,
            tipsAndAdvice = null,
            progressionAdvice = null
        )
    )

    private val exercisesFlow = MutableStateFlow(sampleExercises)
    private val favoritesFlow = MutableStateFlow(setOf("Приседания"))

    @Before
    fun setup() {
        every { mockExerciseLibraryViewModel.exercises } returns exercisesFlow as StateFlow<List<ExerciseLibrary>>
        every { mockExerciseLibraryViewModel.favoriteExercises } returns MutableStateFlow(emptySet<String>()) as StateFlow<Set<String>>
        every { mockExerciseLibraryViewModel.initialize() } returns Unit

        every { mockProfileViewModel?.getFavoriteExercises() } returns favoritesFlow
    }

    @Test
    fun exerciseLibraryScreen_shouldDisplayExerciseCards() {
        lateinit var clickedExercise: ExerciseLibrary

        composeTestRule.setContent {
            ExerciseLibraryScreen(
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel,
                onExerciseClick = { clickedExercise = it },
                onToggleFavorite = {}
            )
        }

        composeTestRule.onNodeWithText("Приседания").assertIsDisplayed()
        composeTestRule.onNodeWithText("Жим лёжа").assertIsDisplayed()
    }

    @Test
    fun exerciseLibraryScreen_shouldDisplayMuscleGroups() {
        composeTestRule.setContent {
            ExerciseLibraryScreen(
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel,
                onExerciseClick = {},
                onToggleFavorite = {}
            )
        }

        composeTestRule.onNodeWithText("Квадрицепсы, Ягодицы").assertIsDisplayed()
        composeTestRule.onNodeWithText("Грудь, Трицепс").assertIsDisplayed()
    }

    @Test
    fun exerciseLibraryScreen_onExerciseClick_shouldTriggerCallback() {
        var clickedExercise: ExerciseLibrary? = null

        composeTestRule.setContent {
            ExerciseLibraryScreen(
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel,
                onExerciseClick = { clickedExercise = it },
                onToggleFavorite = {}
            )
        }

        composeTestRule.onNodeWithText("Приседания").performClick()

        assertEquals("Приседания", clickedExercise?.name)
    }

    @Test
    fun exerciseLibraryScreen_shouldDisplayFavoriteIconForFavoriteExercises() {
        val updatedFavoritesFlow = MutableStateFlow(setOf("Приседания"))
        every { mockProfileViewModel?.getFavoriteExercises() } returns updatedFavoritesFlow

        composeTestRule.setContent {
            ExerciseLibraryScreen(
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel,
                onExerciseClick = {},
                onToggleFavorite = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Убрать из избранного").assertIsDisplayed()
    }

    @Test
    fun exerciseLibraryScreen_shouldDisplayNonFavoriteIconForNonFavoriteExercises() {
        val updatedFavoritesFlow = MutableStateFlow(emptySet<String>())
        every { mockProfileViewModel?.getFavoriteExercises() } returns updatedFavoritesFlow

        composeTestRule.setContent {
            ExerciseLibraryScreen(
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel,
                onExerciseClick = {},
                onToggleFavorite = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Добавить в избранное").assertIsDisplayed()
    }

    @Test
    fun exerciseLibraryScreen_onFavoriteClick_shouldToggleFavorite() {
        var toggledExercise: String? = null

        composeTestRule.setContent {
            ExerciseLibraryScreen(
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel,
                onExerciseClick = {},
                onToggleFavorite = { toggledExercise = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Добавить в избранное").performClick()

        assertEquals("Приседания", toggledExercise)
    }
}
