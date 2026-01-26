package com.example.fitness_plan.domain.calculator

import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.WeightChangeType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeightCalculatorTest {

    private val calculator = WeightCalculator()

    @Test
    fun `calculateWeightProgression with no history should return NO_HISTORY`() {
        val history = emptyList<ExerciseStats>()
        val result = calculator.calculateWeightProgression(
            exerciseName = "Приседания",
            currentWeight = 20f,
            history = history,
            baseReps = listOf(12, 13, 14)
        )

        assertThat(result.changeType).isEqualTo(WeightChangeType.NO_HISTORY)
        assertThat(result.reason).contains("Недостаточно данных")
    }

    @Test
    fun `calculateWeightProgression with single history entry should return NO_HISTORY`() {
        val history = listOf(
            ExerciseStats("Приседания", System.currentTimeMillis(), 20.0, 12)
        )
        val result = calculator.calculateWeightProgression(
            exerciseName = "Приседания",
            currentWeight = 20f,
            history = history,
            baseReps = listOf(12, 13, 14)
        )

        assertThat(result.changeType).isEqualTo(WeightChangeType.NO_HISTORY)
    }

    @Test
    fun `calculateWeightProgression with avgReps above target by 1 should INCREASE weight`() {
        val history = listOf(
            ExerciseStats("Приседания", System.currentTimeMillis() - 1000, 20.0, 14),
            ExerciseStats("Приседания", System.currentTimeMillis(), 20.0, 15)
        )
        val result = calculator.calculateWeightProgression(
            exerciseName = "Приседания",
            currentWeight = 20f,
            history = history,
            baseReps = listOf(12, 13, 14)
        )

        assertThat(result.changeType).isEqualTo(WeightChangeType.INCREASED)
        assertThat(result.newWeight).isGreaterThan(result.oldWeight)
        assertThat(result.newWeight).isEqualTo(22.5f)
        assertThat(result.reason).contains("выше целевого")
    }

    @Test
    fun `calculateWeightProgression with avgReps below target by 3 should DECREASE weight`() {
        val history = listOf(
            ExerciseStats("Приседания", System.currentTimeMillis() - 1000, 20.0, 8),
            ExerciseStats("Приседания", System.currentTimeMillis(), 20.0, 9)
        )
        val result = calculator.calculateWeightProgression(
            exerciseName = "Приседания",
            currentWeight = 20f,
            history = history,
            baseReps = listOf(12, 13, 14)
        )

        assertThat(result.changeType).isEqualTo(WeightChangeType.DECREASED)
        assertThat(result.newWeight).isLessThan(result.oldWeight)
        assertThat(result.newWeight).isEqualTo(17.5f)
        assertThat(result.reason).contains("ниже целевого")
    }

    @Test
    fun `calculateWeightProgression with avgReps in target range should UNCHANGED weight`() {
        val history = listOf(
            ExerciseStats("Приседания", System.currentTimeMillis() - 1000, 20.0, 12),
            ExerciseStats("Приседания", System.currentTimeMillis(), 20.0, 13)
        )
        val result = calculator.calculateWeightProgression(
            exerciseName = "Приседания",
            currentWeight = 20f,
            history = history,
            baseReps = listOf(12, 13, 14)
        )

        assertThat(result.changeType).isEqualTo(WeightChangeType.UNCHANGED)
        assertThat(result.newWeight).isEqualTo(result.oldWeight)
        assertThat(result.reason).contains("в целевом диапазоне")
    }

    @Test
    fun `calculateWeightProgression should round to standard weight`() {
        val history = listOf(
            ExerciseStats("Приседания", System.currentTimeMillis() - 1000, 20.0, 15),
            ExerciseStats("Приседания", System.currentTimeMillis(), 20.0, 16)
        )
        val result = calculator.calculateWeightProgression(
            exerciseName = "Приседания",
            currentWeight = 20.0f,
            history = history,
            baseReps = listOf(12, 13, 14)
        )

        assertThat(result.newWeight).isIn(WeightCalculator.STANDARD_DUMBBELL_WEIGHTS)
    }

    @Test
    fun `calculateWeightProgression should not decrease below minimum weight`() {
        val history = listOf(
            ExerciseStats("Приседания", System.currentTimeMillis() - 1000, 2.0, 5),
            ExerciseStats("Приседания", System.currentTimeMillis(), 2.0, 6)
        )
        val result = calculator.calculateWeightProgression(
            exerciseName = "Приседания",
            currentWeight = 2.0f,
            history = history,
            baseReps = listOf(12, 13, 14)
        )

        assertThat(result.changeType).isEqualTo(WeightChangeType.DECREASED)
        assertThat(result.newWeight).isAtLeast(1.0f)
    }

    @Test
    fun `calculateAdaptiveWeight should return null when history is empty`() {
        val result = calculator.calculateAdaptiveWeight(
            exerciseName = "Приседания",
            history = emptyList(),
            baseReps = listOf(12, 13, 14)
        )

        assertThat(result).isNull()
    }

    @Test
    fun `calculateAdaptiveWeight should return null when avgReps is below target`() {
        val history = listOf(
            ExerciseStats("Приседания", System.currentTimeMillis() - 1000, 20.0, 10),
            ExerciseStats("Приседания", System.currentTimeMillis(), 20.0, 11)
        )
        val result = calculator.calculateAdaptiveWeight(
            exerciseName = "Приседания",
            history = history,
            baseReps = listOf(12, 13, 14)
        )

        assertThat(result).isNull()
    }

    @Test
    fun `calculateAdaptiveWeight should return increased weight when avgReps is above target by 1`() {
        val history = listOf(
            ExerciseStats("Приседания", System.currentTimeMillis() - 1000, 20.0, 14),
            ExerciseStats("Приседания", System.currentTimeMillis(), 20.0, 15)
        )
        val result = calculator.calculateAdaptiveWeight(
            exerciseName = "Приседания",
            history = history,
            baseReps = listOf(12, 13, 14)
        )

        assertThat(result).isNotNull()
        assertThat(result).isGreaterThan(20f)
    }
}