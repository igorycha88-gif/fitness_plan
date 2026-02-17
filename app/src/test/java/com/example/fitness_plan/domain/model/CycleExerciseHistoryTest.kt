package com.example.fitness_plan.domain.model

import org.junit.Assert.*
import org.junit.Test

class CycleExerciseHistoryTest {

    @Test
    fun `getUsedExercisesForGroup returns correct exercises`() {
        val history = CycleExerciseHistory(
            cycleNumber = 1,
            startDate = System.currentTimeMillis(),
            usedExercises = mapOf(
                "Плечи и Руки" to setOf("Жим сидя", "Бицепс", "Трицепс"),
                "Грудь и Широчайшие" to setOf("Жим лёжа", "Тяга верхнего блока")
            ),
            poolId = "pool_A"
        )

        val shoulderArms = history.getUsedExercisesForGroup("Плечи и Руки")
        assertEquals(3, shoulderArms.size)
        assertTrue(shoulderArms.contains("Жим сидя"))
        assertTrue(shoulderArms.contains("Бицепс"))
        assertTrue(shoulderArms.contains("Трицепс"))

        val chestLats = history.getUsedExercisesForGroup("Грудь и Широчайшие")
        assertEquals(2, chestLats.size)
    }

    @Test
    fun `getUsedExercisesForGroup returns empty set for non-existent group`() {
        val history = CycleExerciseHistory(
            cycleNumber = 1,
            startDate = System.currentTimeMillis(),
            usedExercises = mapOf(
                "Плечи и Руки" to setOf("Жим сидя")
            ),
            poolId = "pool_A"
        )

        val result = history.getUsedExercisesForGroup("Non-existent group")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTotalUniqueExercisesUsed returns correct count`() {
        val history = CycleExerciseHistory(
            cycleNumber = 1,
            startDate = System.currentTimeMillis(),
            usedExercises = mapOf(
                "Плечи и Руки" to setOf("Exercise1", "Exercise2", "Exercise3"),
                "Грудь и Широчайшие" to setOf("Exercise4", "Exercise5")
            ),
            poolId = "pool_A"
        )

        assertEquals(5, history.getTotalUniqueExercisesUsed())
    }

    @Test
    fun `getTotalUniqueExercisesUsed returns 0 for empty history`() {
        val history = CycleExerciseHistory(
            cycleNumber = 1,
            startDate = System.currentTimeMillis(),
            usedExercises = emptyMap(),
            poolId = "pool_A"
        )

        assertEquals(0, history.getTotalUniqueExercisesUsed())
    }

    @Test
    fun `hasUsedExercise returns true for used exercise`() {
        val history = CycleExerciseHistory(
            cycleNumber = 1,
            startDate = System.currentTimeMillis(),
            usedExercises = mapOf(
                "Плечи и Руки" to setOf("Жим сидя", "Бицепс")
            ),
            poolId = "pool_A"
        )

        assertTrue(history.hasUsedExercise("Жим сидя"))
        assertTrue(history.hasUsedExercise("Бицепс"))
    }

    @Test
    fun `hasUsedExercise returns false for unused exercise`() {
        val history = CycleExerciseHistory(
            cycleNumber = 1,
            startDate = System.currentTimeMillis(),
            usedExercises = mapOf(
                "Плечи и Руки" to setOf("Жим сидя", "Бицепс")
            ),
            poolId = "pool_A"
        )

        assertFalse(history.hasUsedExercise("Трицепс"))
        assertFalse(history.hasUsedExercise("Non-existent exercise"))
    }

    @Test
    fun `cycleNumber is correctly stored`() {
        val history = CycleExerciseHistory(
            cycleNumber = 5,
            startDate = System.currentTimeMillis(),
            usedExercises = emptyMap(),
            poolId = "pool_B"
        )

        assertEquals(5, history.cycleNumber)
    }

    @Test
    fun `poolId is correctly stored`() {
        val history = CycleExerciseHistory(
            cycleNumber = 1,
            startDate = System.currentTimeMillis(),
            usedExercises = emptyMap(),
            poolId = "pool_C"
        )

        assertEquals("pool_C", history.poolId)
    }
}
