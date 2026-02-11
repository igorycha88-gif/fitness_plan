package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.BodyParameter
import com.example.fitness_plan.domain.model.BodyParameterType
import com.example.fitness_plan.domain.model.CalculationMethod
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BodyParameterCalculatorTest {

    private lateinit var calculator: BodyParameterCalculator

    @Before
    fun setup() {
        calculator = BodyParameterCalculator()
    }

    @Test
    fun calculateBMI_returnsCorrectValue() {
        val weightKg = 85.0
        val heightCm = 178.0
        val expectedBMI = 26.83

        val result = calculator.calculateBMI(weightKg, heightCm)

        assertEquals(expectedBMI, result, 0.1)
    }

    @Test
    fun calculateBMI_underweight() {
        val weightKg = 55.0
        val heightCm = 175.0
        val result = calculator.calculateBMI(weightKg, heightCm)

        assertTrue("BMI should indicate underweight", result < 18.5)
    }

    @Test
    fun calculateBMI_normal() {
        val weightKg = 70.0
        val heightCm = 175.0
        val result = calculator.calculateBMI(weightKg, heightCm)

        assertTrue("BMI should indicate normal weight", result >= 18.5 && result < 25)
    }

    @Test
    fun calculateBMI_overweight() {
        val weightKg = 85.0
        val heightCm = 175.0
        val result = calculator.calculateBMI(weightKg, heightCm)

        assertTrue("BMI should indicate overweight", result >= 25 && result < 30)
    }

    @Test
    fun calculateBMI_obese() {
        val weightKg = 100.0
        val heightCm = 175.0
        val result = calculator.calculateBMI(weightKg, heightCm)

        assertTrue("BMI should indicate obesity", result >= 30)
    }

    @Test
    fun calculateBodyFatUSNavy_male() {
        val gender = "Мужской"
        val heightCm = 180.0
        val neckCm = 38.0
        val waistCm = 90.0

        val result = calculator.calculateBodyFatUSNavy(gender, heightCm, neckCm, waistCm)

        assertTrue("Body fat percentage should be reasonable", result > 0 && result < 50)
    }

    @Test
    fun calculateBodyFatUSNavy_female() {
        val gender = "Женский"
        val heightCm = 165.0
        val neckCm = 32.0
        val waistCm = 70.0
        val hipsCm = 95.0

        val result = calculator.calculateBodyFatUSNavy(gender, heightCm, neckCm, waistCm, hipsCm)

        assertTrue("Body fat percentage should be reasonable", result > 0 && result < 50)
    }

    @Test
    fun calculateBodyFatUSNavy_caseInsensitive() {
        val heightCm = 175.0
        val neckCm = 35.0
        val waistCm = 85.0

        val result1 = calculator.calculateBodyFatUSNavy("мужской", heightCm, neckCm, waistCm)
        val result2 = calculator.calculateBodyFatUSNavy("МУЖСКОЙ", heightCm, neckCm, waistCm)

        assertEquals("Gender should be case insensitive", result1, result2, 0.01)
    }

    @Test
    fun calculateMuscleMass_returnsCorrectValue() {
        val weightKg = 80.0
        val bodyFatPercent = 20.0
        val leanBodyMass = 80.0 * (1 - 0.20)
        val expectedMuscleMass = leanBodyMass * 0.55

        val result = calculator.calculateMuscleMass(weightKg, bodyFatPercent)

        assertEquals(expectedMuscleMass, result, 0.1)
    }

    @Test
    fun calculateMuscleMass_highBodyFat() {
        val weightKg = 100.0
        val bodyFatPercent = 30.0

        val result = calculator.calculateMuscleMass(weightKg, bodyFatPercent)

        assertTrue("Muscle mass should be reasonable", result > 20 && result < 70)
    }

    @Test
    fun calculateCalculatedParameters_withWeightAndHeight_calculatesBMI() {
        val input = mapOf(
            BodyParameterType.WEIGHT to 85.0,
            BodyParameterType.HEIGHT to 178.0
        )
        val date = System.currentTimeMillis()

        val result = calculator.calculateCalculatedParameters(input, "Мужской", date)

        val bmiParam = result.find { it.parameterType == BodyParameterType.BODY_MASS_INDEX }
        assertNotNull("BMI should be calculated", bmiParam)
        assertEquals("BMI should be auto-calculated", CalculationMethod.AUTO, bmiParam!!.calculationMethod)
        assertEquals("BMI value should match calculateBMI", 
            calculator.calculateBMI(85.0, 178.0), 
            bmiParam.value, 
            0.1)
    }

    @Test
    fun calculateCalculatedParameters_withAllMaleData_calculatesAll() {
        val input = mapOf(
            BodyParameterType.WEIGHT to 85.0,
            BodyParameterType.HEIGHT to 178.0,
            BodyParameterType.NECK to 38.0,
            BodyParameterType.WAIST to 90.0
        )
        val date = System.currentTimeMillis()

        val result = calculator.calculateCalculatedParameters(input, "Мужской", date)

        assertTrue("Should calculate BMI", result.any { it.parameterType == BodyParameterType.BODY_MASS_INDEX })
        assertTrue("Should calculate Body Fat", result.any { it.parameterType == BodyParameterType.BODY_FAT })
        assertTrue("Should calculate Muscle Mass", result.any { it.parameterType == BodyParameterType.MUSCLE_MASS })
    }

    @Test
    fun calculateCalculatedParameters_withAllFemaleData_calculatesAll() {
        val input = mapOf(
            BodyParameterType.WEIGHT to 65.0,
            BodyParameterType.HEIGHT to 165.0,
            BodyParameterType.NECK to 32.0,
            BodyParameterType.WAIST to 70.0,
            BodyParameterType.HIPS to 95.0
        )
        val date = System.currentTimeMillis()

        val result = calculator.calculateCalculatedParameters(input, "Женский", date)

        assertTrue("Should calculate BMI", result.any { it.parameterType == BodyParameterType.BODY_MASS_INDEX })
        assertTrue("Should calculate Body Fat", result.any { it.parameterType == BodyParameterType.BODY_FAT })
        assertTrue("Should calculate Muscle Mass", result.any { it.parameterType == BodyParameterType.MUSCLE_MASS })
    }

    @Test
    fun calculateCalculatedParameters_insufficientData_returnsEmpty() {
        val input = mapOf(
            BodyParameterType.WEIGHT to 85.0
        )
        val date = System.currentTimeMillis()

        val result = calculator.calculateCalculatedParameters(input, "Мужской", date)

        assertEquals("Should not calculate anything without height", 0, result.size)
    }

    @Test
    fun calculateCalculatedParameters_withoutGender_calculatesBMIOnly() {
        val input = mapOf(
            BodyParameterType.WEIGHT to 85.0,
            BodyParameterType.HEIGHT to 178.0
        )
        val date = System.currentTimeMillis()

        val result = calculator.calculateCalculatedParameters(input, null, date)

        assertEquals("Should only calculate BMI", 1, result.size)
        assertEquals("Should calculate BMI", BodyParameterType.BODY_MASS_INDEX, result[0].parameterType)
    }
}
