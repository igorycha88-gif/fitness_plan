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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ExerciseGuideScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockExerciseLibraryViewModel = mockk<com.example.fitness_plan.presentation.viewmodel.ExerciseLibraryViewModel>()
    private val mockProfileViewModel = mockk<com.example.fitness_plan.presentation.viewmodel.ProfileViewModel>()

    private val sampleExercise = ExerciseLibrary(
        id = "1",
        name = "Приседания",
        description = "Базовое упражнение для ног",
        exerciseType = ExerciseType.STRENGTH,
        equipment = listOf(EquipmentType.BARBELL),
        muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
        difficulty = "Начальный",
        stepByStepInstructions = "Встаньте прямо\nПрисядьте",
        animationUrl = null,
        imageUrl = null,
        imageRes = null,
        tipsAndAdvice = "Советы\nСледите за осанкой",
        progressionAdvice = "Прогрессия\nУвеличивайте вес"
    )

    private val sampleExerciseWithImage = ExerciseLibrary(
        id = "2",
        name = "Жим лёжа",
        description = "Упражнение для груди",
        exerciseType = ExerciseType.STRENGTH,
        equipment = listOf(EquipmentType.BARBELL, EquipmentType.SPECIAL_BENCH),
        muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
        difficulty = "Средний",
        stepByStepInstructions = "Лягте на скамью\nСнимите штангу",
        animationUrl = null,
        imageUrl = "https://example.com/bench_press.jpg",
        imageRes = null,
        tipsAndAdvice = null,
        progressionAdvice = null
    )

    private val sampleExerciseWithLocalImage = ExerciseLibrary(
        id = "3",
        name = "Жим на тренажёре для груди",
        description = "Упражнение для груди с локальной картинкой",
        exerciseType = ExerciseType.STRENGTH,
        equipment = listOf(EquipmentType.LEVER_MACHINE),
        muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
        difficulty = "Начальный",
        stepByStepInstructions = "Сядьте в тренажёр\nВозьмите рукоятки",
        animationUrl = null,
        imageUrl = null,
        imageRes = "chest_press_machine",
        tipsAndAdvice = null,
        progressionAdvice = null
    )

    private val exercisesFlow = MutableStateFlow(listOf(sampleExercise, sampleExerciseWithImage, sampleExerciseWithLocalImage))
    private val favoritesFlow = MutableStateFlow(emptySet<String>())

    @Before
    fun setup() {
        every { mockExerciseLibraryViewModel.exercises } returns exercisesFlow as StateFlow<List<ExerciseLibrary>>
        every { mockExerciseLibraryViewModel.favoriteExercises } returns MutableStateFlow(emptySet<String>()) as StateFlow<Set<String>>
        every { mockProfileViewModel?.getFavoriteExercises() } returns favoritesFlow
    }

    @Test
    fun exerciseGuideScreen_exerciseWithoutImage_shouldDisplayPlaceholderMessage() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "1",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Картинка скоро будет добавлена").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_exerciseWithoutImage_shouldDisplayExerciseName() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "1",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Приседания").assertIsDisplayed()
        composeTestRule.onNodeWithText("Картинка скоро будет добавлена").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_exerciseWithImageUrl_shouldDisplayImage() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "2",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithContentDescription("Изображение упражнения: Жим лёжа").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_exerciseWithLocalImage_shouldDisplayImage() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "3",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithContentDescription("Изображение упражнения: Жим на тренажёре для груди").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_shouldDisplayDescription() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "1",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Описание").assertIsDisplayed()
        composeTestRule.onNodeWithText("Базовое упражнение для ног").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_shouldDisplayInstructions() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "1",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Пошаговая инструкция").assertIsDisplayed()
        composeTestRule.onNodeWithText("Встаньте прямо").assertIsDisplayed()
        composeTestRule.onNodeWithText("Присядьте").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_shouldDisplayDifficultyBadge() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "1",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Сложность").assertIsDisplayed()
        composeTestRule.onNodeWithText("Начальный").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_shouldDisplayTips() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "1",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("💡 Советы и рекомендации").assertIsDisplayed()
        composeTestRule.onNodeWithText("Советы").assertIsDisplayed()
        composeTestRule.onNodeWithText("Следите за осанкой").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_shouldDisplayProgression() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "1",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("📈 Прогрессия").assertIsDisplayed()
        composeTestRule.onNodeWithText("Прогрессия").assertIsDisplayed()
        composeTestRule.onNodeWithText("Увеличивайте вес").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_shouldDisplayMuscleGroups() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "1",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Работающие мышцы").assertIsDisplayed()
        composeTestRule.onNodeWithText("Квадрицепсы").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ягодицы").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_shouldDisplayEquipment() {
        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "1",
                onBackClick = {},
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Необходимое оборудование").assertIsDisplayed()
        composeTestRule.onNodeWithText("Штанга").assertIsDisplayed()
    }

    @Test
    fun exerciseGuideScreen_onBackClick_shouldTriggerCallback() {
        var backClicked = false

        composeTestRule.setContent {
            ExerciseGuideScreen(
                exerciseId = "1",
                onBackClick = { backClicked = true },
                viewModel = mockExerciseLibraryViewModel,
                profileViewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithContentDescription("Назад").performClick()

        assert(backClicked)
    }
}
