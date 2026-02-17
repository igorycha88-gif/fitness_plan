package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.EquipmentType
import com.example.fitness_plan.domain.model.ExerciseLibrary
import com.example.fitness_plan.domain.model.ExerciseType
import com.example.fitness_plan.domain.model.MuscleGroup
import com.example.fitness_plan.domain.model.MuscleGroupSequence
import com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ExercisePoolManagerTest {

    private lateinit var exerciseLibraryRepository: ExerciseLibraryRepository
    private lateinit var exercisePoolManager: ExercisePoolManager

    @Before
    fun setup() {
        exerciseLibraryRepository = mock(ExerciseLibraryRepository::class.java)
        exercisePoolManager = ExercisePoolManager(exerciseLibraryRepository)
    }

    @Test
    fun `getExercisesForSequence returns correct muscle groups for ARMS`() = runBlocking {
        val mockExercises = listOf(
            createMockExercise("Бицепс", ExerciseType.STRENGTH, listOf(MuscleGroup.BICEPS)),
            createMockExercise("Трицепс на блоке", ExerciseType.STRENGTH, listOf(MuscleGroup.TRICEPS)),
            createMockExercise("Приседания", ExerciseType.STRENGTH, listOf(MuscleGroup.QUADS)),
            createMockExercise("Бег", ExerciseType.CARDIO, emptyList())
        )

        `when`(exerciseLibraryRepository.getAllExercisesAsList()).thenReturn(mockExercises)

        val result = exercisePoolManager.getExercisesForSequence(
            sequence = MuscleGroupSequence.ARMS,
            excludedExerciseNames = emptySet(),
            count = 3
        )

        assertTrue(result.all { exercise ->
            exercise.muscleGroups.any { it in MuscleGroupSequence.ARMS.muscleGroups }
        })
    }

    @Test
    fun `getExercisesForSequence excludes specified exercises`() = runBlocking {
        val mockExercises = listOf(
            createMockExercise("Бицепс", ExerciseType.STRENGTH, listOf(MuscleGroup.BICEPS)),
            createMockExercise("Трицепс на блоке", ExerciseType.STRENGTH, listOf(MuscleGroup.TRICEPS)),
            createMockExercise("Молотки", ExerciseType.STRENGTH, listOf(MuscleGroup.BICEPS))
        )

        `when`(exerciseLibraryRepository.getAllExercisesAsList()).thenReturn(mockExercises)

        val result = exercisePoolManager.getExercisesForSequence(
            sequence = MuscleGroupSequence.ARMS,
            excludedExerciseNames = setOf("Бицепс"),
            count = 10
        )

        assertFalse(result.any { it.name == "Бицепс" })
    }

    @Test
    fun `getExercisesForSequence returns cardio exercises for CARDIO sequence`() = runBlocking {
        val mockExercises = listOf(
            createMockExercise("Жим гантелей сидя", ExerciseType.STRENGTH, listOf(MuscleGroup.SHOULDERS)),
            createMockExercise("Бег", ExerciseType.CARDIO, emptyList()),
            createMockExercise("Велотренажёр", ExerciseType.CARDIO, emptyList())
        )

        `when`(exerciseLibraryRepository.getAllExercisesAsList()).thenReturn(mockExercises)

        val result = exercisePoolManager.getExercisesForSequence(
            sequence = MuscleGroupSequence.CARDIO,
            excludedExerciseNames = emptySet(),
            count = 10
        )

        assertTrue(result.all { it.exerciseType == ExerciseType.CARDIO })
    }

    @Test
    fun `getExercisesForSequence respects count limit`() = runBlocking {
        val mockExercises = listOf(
            createMockExercise("Exercise 1", ExerciseType.STRENGTH, listOf(MuscleGroup.BICEPS)),
            createMockExercise("Exercise 2", ExerciseType.STRENGTH, listOf(MuscleGroup.BICEPS)),
            createMockExercise("Exercise 3", ExerciseType.STRENGTH, listOf(MuscleGroup.TRICEPS)),
            createMockExercise("Exercise 4", ExerciseType.STRENGTH, listOf(MuscleGroup.TRICEPS)),
            createMockExercise("Exercise 5", ExerciseType.STRENGTH, listOf(MuscleGroup.BICEPS))
        )

        `when`(exerciseLibraryRepository.getAllExercisesAsList()).thenReturn(mockExercises)

        val result = exercisePoolManager.getExercisesForSequence(
            sequence = MuscleGroupSequence.ARMS,
            excludedExerciseNames = emptySet(),
            count = 2
        )

        assertEquals(2, result.size)
    }

    @Test
    fun `determinePoolId rotates through pools A B C`() {
        assertEquals("pool_A", exercisePoolManager.determinePoolId(1))
        assertEquals("pool_B", exercisePoolManager.determinePoolId(2))
        assertEquals("pool_C", exercisePoolManager.determinePoolId(3))
        assertEquals("pool_A", exercisePoolManager.determinePoolId(4))
        assertEquals("pool_B", exercisePoolManager.determinePoolId(5))
        assertEquals("pool_C", exercisePoolManager.determinePoolId(6))
    }

    @Test
    fun `calculateExerciseOverlap returns correct percentage`() {
        val history1 = setOf("Exercise1", "Exercise2", "Exercise3", "Exercise4")
        val history2 = setOf("Exercise3", "Exercise4", "Exercise5", "Exercise6")

        val overlap = exercisePoolManager.calculateExerciseOverlap(history1, history2)

        assertEquals(0.5f, overlap, 0.01f)
    }

    @Test
    fun `calculateExerciseOverlap returns zero for empty histories`() {
        assertEquals(0f, exercisePoolManager.calculateExerciseOverlap(emptySet(), setOf("Exercise1")), 0.01f)
        assertEquals(0f, exercisePoolManager.calculateExerciseOverlap(setOf("Exercise1"), emptySet()), 0.01f)
        assertEquals(0f, exercisePoolManager.calculateExerciseOverlap(emptySet(), emptySet()), 0.01f)
    }

    @Test
    fun `getAllStrengthExercises returns only strength exercises`() = runBlocking {
        val mockExercises = listOf(
            createMockExercise("Strength 1", ExerciseType.STRENGTH, listOf(MuscleGroup.CHEST)),
            createMockExercise("Cardio 1", ExerciseType.CARDIO, emptyList()),
            createMockExercise("Strength 2", ExerciseType.STRENGTH, listOf(MuscleGroup.LATS)),
            createMockExercise("Stretching 1", ExerciseType.STRETCHING, emptyList())
        )

        `when`(exerciseLibraryRepository.getAllExercisesAsList()).thenReturn(mockExercises)

        val result = exercisePoolManager.getAllStrengthExercises()

        assertEquals(2, result.size)
        assertTrue(result.all { it.exerciseType == ExerciseType.STRENGTH })
    }

    @Test
    fun `getAllCardioExercises returns only cardio exercises`() = runBlocking {
        val mockExercises = listOf(
            createMockExercise("Strength 1", ExerciseType.STRENGTH, listOf(MuscleGroup.CHEST)),
            createMockExercise("Cardio 1", ExerciseType.CARDIO, emptyList()),
            createMockExercise("Cardio 2", ExerciseType.CARDIO, emptyList())
        )

        `when`(exerciseLibraryRepository.getAllExercisesAsList()).thenReturn(mockExercises)

        val result = exercisePoolManager.getAllCardioExercises()

        assertEquals(2, result.size)
        assertTrue(result.all { it.exerciseType == ExerciseType.CARDIO })
    }

    private fun createMockExercise(
        name: String,
        exerciseType: ExerciseType,
        muscleGroups: List<MuscleGroup>
    ): ExerciseLibrary {
        return ExerciseLibrary(
            id = name.lowercase().replace(" ", "_"),
            name = name,
            description = "Description for $name",
            exerciseType = exerciseType,
            equipment = listOf(EquipmentType.DUMBBELLS),
            muscleGroups = muscleGroups,
            difficulty = "Начальный",
            stepByStepInstructions = "Instructions",
            imageUrl = null,
            tipsAndAdvice = "Tips",
            progressionAdvice = "Progression"
        )
    }
}
