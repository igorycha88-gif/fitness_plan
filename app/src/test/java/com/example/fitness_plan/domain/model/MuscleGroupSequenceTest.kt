package com.example.fitness_plan.domain.model

import org.junit.Assert.*
import org.junit.Test

class MuscleGroupSequenceTest {

    @Test
    fun `STRENGTH_SEQUENCE contains 4 sequences without CARDIO`() {
        assertEquals(4, MuscleGroupSequence.STRENGTH_SEQUENCE.size)
        assertFalse(MuscleGroupSequence.STRENGTH_SEQUENCE.contains(MuscleGroupSequence.CARDIO))
    }

    @Test
    fun `FULL_SEQUENCE contains 5 sequences including CARDIO`() {
        assertEquals(5, MuscleGroupSequence.FULL_SEQUENCE.size)
        assertTrue(MuscleGroupSequence.FULL_SEQUENCE.contains(MuscleGroupSequence.CARDIO))
    }

    @Test
    fun `FULL_SEQUENCE has correct order - Arms to Legs top-down`() {
        val expectedOrder = listOf(
            MuscleGroupSequence.ARMS,
            MuscleGroupSequence.SHOULDERS,
            MuscleGroupSequence.CHEST_BACK,
            MuscleGroupSequence.LEGS_CORE,
            MuscleGroupSequence.CARDIO
        )
        assertEquals(expectedOrder, MuscleGroupSequence.FULL_SEQUENCE)
    }

    @Test
    fun `STRENGTH_SEQUENCE has correct order - Arms to Legs top-down`() {
        val expectedOrder = listOf(
            MuscleGroupSequence.ARMS,
            MuscleGroupSequence.SHOULDERS,
            MuscleGroupSequence.CHEST_BACK,
            MuscleGroupSequence.LEGS_CORE
        )
        assertEquals(expectedOrder, MuscleGroupSequence.STRENGTH_SEQUENCE)
    }

    @Test
    fun `ARMS contains correct muscle groups`() {
        val expectedGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.FOREARMS)
        assertEquals(expectedGroups, MuscleGroupSequence.ARMS.muscleGroups)
    }

    @Test
    fun `SHOULDERS contains correct muscle groups`() {
        val expectedGroups = listOf(MuscleGroup.SHOULDERS)
        assertEquals(expectedGroups, MuscleGroupSequence.SHOULDERS.muscleGroups)
    }

    @Test
    fun `CHEST_BACK contains correct muscle groups`() {
        val expectedGroups = listOf(MuscleGroup.CHEST, MuscleGroup.LATS, MuscleGroup.TRAPS)
        assertEquals(expectedGroups, MuscleGroupSequence.CHEST_BACK.muscleGroups)
    }

    @Test
    fun `LEGS_CORE contains correct muscle groups`() {
        val expectedGroups = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES, MuscleGroup.ABS)
        assertEquals(expectedGroups, MuscleGroupSequence.LEGS_CORE.muscleGroups)
    }

    @Test
    fun `CARDIO has empty muscle groups`() {
        assertTrue(MuscleGroupSequence.CARDIO.muscleGroups.isEmpty())
    }

    @Test
    fun `exercisesPerDay is 4 for strength sequences`() {
        assertEquals(4, MuscleGroupSequence.ARMS.exercisesPerDay)
        assertEquals(4, MuscleGroupSequence.SHOULDERS.exercisesPerDay)
        assertEquals(4, MuscleGroupSequence.CHEST_BACK.exercisesPerDay)
        assertEquals(4, MuscleGroupSequence.LEGS_CORE.exercisesPerDay)
    }

    @Test
    fun `exercisesPerDay is 2 for CARDIO`() {
        assertEquals(2, MuscleGroupSequence.CARDIO.exercisesPerDay)
    }

    @Test
    fun `displayNames are correct`() {
        assertEquals("Руки", MuscleGroupSequence.ARMS.displayName)
        assertEquals("Плечи", MuscleGroupSequence.SHOULDERS.displayName)
        assertEquals("Грудь и Спина", MuscleGroupSequence.CHEST_BACK.displayName)
        assertEquals("Ноги и Пресс", MuscleGroupSequence.LEGS_CORE.displayName)
        assertEquals("Кардио", MuscleGroupSequence.CARDIO.displayName)
    }

    @Test
    fun `CARDIO is last in FULL_SEQUENCE`() {
        assertEquals(MuscleGroupSequence.CARDIO, MuscleGroupSequence.FULL_SEQUENCE.last())
    }

    @Test
    fun `sequence follows anatomical top-down order`() {
        val sequence = MuscleGroupSequence.STRENGTH_SEQUENCE
        assertEquals(MuscleGroupSequence.ARMS, sequence[0])
        assertEquals(MuscleGroupSequence.SHOULDERS, sequence[1])
        assertEquals(MuscleGroupSequence.CHEST_BACK, sequence[2])
        assertEquals(MuscleGroupSequence.LEGS_CORE, sequence[3])
    }
}
