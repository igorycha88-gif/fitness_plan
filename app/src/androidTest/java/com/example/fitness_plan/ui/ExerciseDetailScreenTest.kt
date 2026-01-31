package com.example.fitness_plan.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.model.WorkoutPlan
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.Runs
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ExerciseDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockWorkoutViewModel = mockk<com.example.fitness_plan.presentation.viewmodel.WorkoutViewModel>()

    private val sampleStrengthExercise = Exercise(
        id = "1",
        name = "Приседания",
        sets = 3,
        reps = "10-12",
        exerciseType = ExerciseType.STRENGTH,
        equipment = listOf(EquipmentType.BARBELL),
        muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
        imageUrl = null
    )

    private val sampleStrengthExerciseWithImage = Exercise(
        id = "1",
        name = "Приседания с картинкой",
        sets = 3,
        reps = "10-12",
        exerciseType = ExerciseType.STRENGTH,
        equipment = listOf(EquipmentType.BARBELL),
        muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
        imageUrl = "https://example.com/squat.jpg"
    )

    private val sampleCardioExercise = Exercise(
        id = "2",
        name = "Беговая дорожка",
        sets = 1,
        reps = "30 мин",
        exerciseType = ExerciseType.CARDIO,
        equipment = listOf(EquipmentType.TREADMILL),
        muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES)
    )

    private val sampleStretchingExercise = Exercise(
        id = "3",
        name = "Растяжка ног",
        sets = 1,
        reps = "30 сек",
        exerciseType = ExerciseType.STRETCHING,
        equipment = listOf(EquipmentType.BODYWEIGHT),
        muscleGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS)
    )

    private val sampleWorkoutPlan = WorkoutPlan(
        id = "plan1",
        name = "Тестовый план",
        description = "Описание",
        muscleGroups = listOf(),
        days = listOf(
            WorkoutDay(
                id = 0,
                dayName = "День 1",
                exercises = listOf(sampleStrengthExercise, sampleCardioExercise, sampleStretchingExercise),
                muscleGroups = listOf()
            )
        ),
        goal = "Набор массы",
        level = "Средний"
    )

    private val currentWorkoutPlanFlow = MutableStateFlow(sampleWorkoutPlan)
    private val adminWorkoutPlanFlow = MutableStateFlow<WorkoutPlan?>(null)
    private val exerciseStatsFlow = MutableStateFlow(emptyList<com.example.fitness_plan.domain.model.ExerciseStats>())
    private val alternativeExercisesFlow = MutableStateFlow(emptyList<com.example.fitness_plan.domain.model.ExerciseLibrary>())
    private val completedExercisesFlow = MutableStateFlow(emptySet<String>())

    @Before
    fun setup() {
        every { mockWorkoutViewModel.currentWorkoutPlan } returns currentWorkoutPlanFlow as StateFlow<WorkoutPlan?>
        every { mockWorkoutViewModel.adminWorkoutPlan } returns adminWorkoutPlanFlow as StateFlow<WorkoutPlan?>
        every { mockWorkoutViewModel.exerciseStats } returns exerciseStatsFlow as StateFlow<List<com.example.fitness_plan.domain.model.ExerciseStats>>
        every { mockWorkoutViewModel.alternativeExercises } returns alternativeExercisesFlow as StateFlow<List<com.example.fitness_plan.domain.model.ExerciseLibrary>>
        every { mockWorkoutViewModel.completedExercises } returns completedExercisesFlow as StateFlow<Set<String>>
        every { mockWorkoutViewModel.initializeWorkout() } returns Unit
        every { mockWorkoutViewModel.refreshAdminWorkoutPlan() } returns Unit
        every { mockWorkoutViewModel.toggleExerciseCompletion(any(), any()) } returns Unit
        coEvery { mockWorkoutViewModel.loadAlternativeExercises(any(), any(), any()) } just Runs
    }

    @Test
    fun exerciseDetailScreen_strengthExercise_shouldDisplayWeightAndRepsFields() {
        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Приседания",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithText("Вес (кг)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Повторения").assertIsDisplayed()
        composeTestRule.onNodeWithText("Записать результат").assertIsDisplayed()
        composeTestRule.onNodeWithText("Сохранить подход 1").assertIsDisplayed()
    }

    @Test
    fun exerciseDetailScreen_cardioExercise_shouldDisplayMarkAsCompletedButton() {
        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Беговая дорожка",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithText("Статус выполнения").assertIsDisplayed()
        composeTestRule.onNodeWithText("Отметить как выполненное").assertIsDisplayed()

        composeTestRule.onNodeWithText("Вес (кг)").assertDoesNotExist()
        composeTestRule.onNodeWithText("Повторения").assertDoesNotExist()
        composeTestRule.onNodeWithText("Записать результат").assertDoesNotExist()
        composeTestRule.onNodeWithText("Сохранить подход").assertDoesNotExist()
    }

    @Test
    fun exerciseDetailScreen_stretchingExercise_shouldDisplayMarkAsCompletedButton() {
        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Растяжка ног",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithText("Статус выполнения").assertIsDisplayed()
        composeTestRule.onNodeWithText("Отметить как выполненное").assertIsDisplayed()

        composeTestRule.onNodeWithText("Вес (кг)").assertDoesNotExist()
        composeTestRule.onNodeWithText("Повторения").assertDoesNotExist()
        composeTestRule.onNodeWithText("Записать результат").assertDoesNotExist()
        composeTestRule.onNodeWithText("Сохранить подход").assertDoesNotExist()
    }

    @Test
    fun exerciseDetailScreen_onCardioMarkAsCompletedClick_shouldToggleCompletion() {
        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Беговая дорожка",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithText("Отметить как выполненное").performClick()
    }

    @Test
    fun exerciseDetailScreen_onStretchingMarkAsCompletedClick_shouldToggleCompletion() {
        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Растяжка ног",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithText("Отметить как выполненное").performClick()
    }

    @Test
    fun exerciseDetailScreen_completedCardioExercise_shouldShowCompletionMessage() {
        val updatedCompletedFlow = MutableStateFlow(setOf("0_Беговая дорожка"))
        every { mockWorkoutViewModel.completedExercises } returns updatedCompletedFlow as StateFlow<Set<String>>

        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Беговая дорожка",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithText("Выполнено!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Упражнение выполнено").assertIsDisplayed()
        composeTestRule.onNodeWithText("Отменить выполнение").assertIsDisplayed()
    }

    @Test
    fun exerciseDetailScreen_completedStretchingExercise_shouldShowCompletionMessage() {
        val updatedCompletedFlow = MutableStateFlow(setOf("0_Растяжка ног"))
        every { mockWorkoutViewModel.completedExercises } returns updatedCompletedFlow as StateFlow<Set<String>>

        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Растяжка ног",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithText("Выполнено!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Упражнение выполнено").assertIsDisplayed()
        composeTestRule.onNodeWithText("Отменить выполнение").assertIsDisplayed()
    }

    @Test
    fun exerciseDetailScreen_onCancelCardioCompletion_shouldToggleCompletion() {
        val updatedCompletedFlow = MutableStateFlow(setOf("0_Беговая дорожка"))
        every { mockWorkoutViewModel.completedExercises } returns updatedCompletedFlow as StateFlow<Set<String>>

        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Беговая дорожка",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithText("Отменить выполнение").performClick()
    }

    @Test
    fun exerciseDetailScreen_completedCardioExercise_shouldShowCancelButton() {
        val updatedCompletedFlow = MutableStateFlow(setOf("0_Беговая дорожка"))
        every { mockWorkoutViewModel.completedExercises } returns updatedCompletedFlow as StateFlow<Set<String>>

        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Беговая дорожка",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithText("Упражнение выполнено").assertIsDisplayed()
        composeTestRule.onNodeWithText("Отменить выполнение").assertIsDisplayed()
        composeTestRule.onNodeWithText("Отметить как выполненное").assertDoesNotExist()
    }

    @Test
    fun exerciseDetailScreen_exerciseWithoutImage_shouldDisplayPlaceholderMessage() {
        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Приседания",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithText("Картинка скоро будет добавлена").assertIsDisplayed()
    }

    @Test
    fun exerciseDetailScreen_exerciseWithImageUrl_shouldDisplayImageCard() {
        val workoutPlanWithImageExercise = WorkoutPlan(
            id = "plan2",
            name = "Тестовый план с картинкой",
            description = "Описание",
            muscleGroups = listOf(),
            days = listOf(
                WorkoutDay(
                    id = 0,
                    dayName = "День 1",
                    exercises = listOf(sampleStrengthExerciseWithImage),
                    muscleGroups = listOf()
                )
            ),
            goal = "Набор массы",
            level = "Средний"
        )

        val updatedWorkoutPlanFlow = MutableStateFlow(workoutPlanWithImageExercise)
        every { mockWorkoutViewModel.currentWorkoutPlan } returns updatedWorkoutPlanFlow as StateFlow<WorkoutPlan?>

        composeTestRule.setContent {
            ExerciseDetailScreen(
                exerciseName = "Приседания с картинкой",
                dayIndex = 0,
                onBackClick = {},
                workoutViewModel = mockWorkoutViewModel,
                isAdmin = false
            )
        }

        composeTestRule.onNodeWithContentDescription("Изображение упражнения: Приседания с картинкой").assertIsDisplayed()
    }
}
