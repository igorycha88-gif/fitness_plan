package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.BodyParameterType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MeasurementValidatorTest {

    private lateinit var validator: MeasurementValidator

    @Before
    fun setup() {
        validator = MeasurementValidator()
    }

    @Test
    fun validate_withValidData_returnsSuccess() {
        val input = mapOf(
            BodyParameterType.WEIGHT to 85.0,
            BodyParameterType.HEIGHT to 178.0
        )

        val result = validator.validate(input)

        assertTrue("Should return Success", result is MeasurementValidator.ValidationResult.Success)
    }

    @Test
    fun validate_withEmptyData_returnsError() {
        val input = emptyMap<BodyParameterType, Double?>()

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
        assertEquals("Error message should indicate empty data", 
            "Минимум 1 параметр должен быть заполнен", 
            (result as MeasurementValidator.ValidationResult.Error).errorMessage)
    }

    @Test
    fun validate_withAllNullValues_returnsError() {
        val input = mapOf(
            BodyParameterType.WEIGHT to null,
            BodyParameterType.HEIGHT to null
        )

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withValidWeight_returnsSuccess() {
        val input = mapOf(BodyParameterType.WEIGHT to 85.0)

        val result = validator.validate(input)

        assertTrue("Should return Success", result is MeasurementValidator.ValidationResult.Success)
    }

    @Test
    fun validate_withWeightBelowMin_returnsError() {
        val input = mapOf(BodyParameterType.WEIGHT to 15.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
        assertTrue("Error message should contain range info", 
            (result as MeasurementValidator.ValidationResult.Error).errorMessage.contains("от 20 до 300"))
    }

    @Test
    fun validate_withWeightAboveMax_returnsError() {
        val input = mapOf(BodyParameterType.WEIGHT to 350.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withWeightAtMinBoundary_returnsSuccess() {
        val input = mapOf(BodyParameterType.WEIGHT to 20.0)

        val result = validator.validate(input)

        assertTrue("Should return Success at min boundary", result is MeasurementValidator.ValidationResult.Success)
    }

    @Test
    fun validate_withWeightAtMaxBoundary_returnsSuccess() {
        val input = mapOf(BodyParameterType.WEIGHT to 300.0)

        val result = validator.validate(input)

        assertTrue("Should return Success at max boundary", result is MeasurementValidator.ValidationResult.Success)
    }

    @Test
    fun validate_withValidHeight_returnsSuccess() {
        val input = mapOf(BodyParameterType.HEIGHT to 175.0)

        val result = validator.validate(input)

        assertTrue("Should return Success", result is MeasurementValidator.ValidationResult.Success)
    }

    @Test
    fun validate_withHeightBelowMin_returnsError() {
        val input = mapOf(BodyParameterType.HEIGHT to 40.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withHeightAboveMax_returnsError() {
        val input = mapOf(BodyParameterType.HEIGHT to 300.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withCircumferenceBelowMin_returnsError() {
        val input = mapOf(BodyParameterType.WAIST to 5.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withCircumferenceAboveMax_returnsError() {
        val input = mapOf(BodyParameterType.WAIST to 250.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withValidCircumference_returnsSuccess() {
        val input = mapOf(BodyParameterType.WAIST to 85.0)

        val result = validator.validate(input)

        assertTrue("Should return Success", result is MeasurementValidator.ValidationResult.Success)
    }

    @Test
    fun validate_withBodyFatBelowMin_returnsError() {
        val input = mapOf(BodyParameterType.BODY_FAT to 0.5)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withBodyFatAboveMax_returnsError() {
        val input = mapOf(BodyParameterType.BODY_FAT to 70.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withBMIBelowMin_returnsError() {
        val input = mapOf(BodyParameterType.BODY_MASS_INDEX to 5.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withBMIAboveMax_returnsError() {
        val input = mapOf(BodyParameterType.BODY_MASS_INDEX to 70.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withMuscleMassBelowMin_returnsError() {
        val input = mapOf(BodyParameterType.MUSCLE_MASS to 5.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withMuscleMassAboveMax_returnsError() {
        val input = mapOf(BodyParameterType.MUSCLE_MASS to 200.0)

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_returnsValidatedData() {
        val input = mapOf(
            BodyParameterType.WEIGHT to 85.0,
            BodyParameterType.HEIGHT to 178.0,
            BodyParameterType.WAIST to 90.0
        )

        val result = validator.validate(input)

        assertTrue("Should return Success", result is MeasurementValidator.ValidationResult.Success)
        val validatedData = (result as MeasurementValidator.ValidationResult.Success).validatedData
        assertEquals("Should return 3 validated parameters", 3, validatedData.size)
        assertEquals(85.0, validatedData[BodyParameterType.WEIGHT]!!, 0.01)
        assertEquals(178.0, validatedData[BodyParameterType.HEIGHT]!!, 0.01)
        assertEquals(90.0, validatedData[BodyParameterType.WAIST]!!, 0.01)
    }

    @Test
    fun isParameterValid_withValidValue_returnsTrue() {
        val result = validator.isParameterValid(BodyParameterType.WEIGHT, 85.0)
        assertTrue("Should return true for valid weight", result)
    }

    @Test
    fun isParameterValid_withInvalidValue_returnsFalse() {
        val result = validator.isParameterValid(BodyParameterType.WEIGHT, 350.0)
        assertFalse("Should return false for invalid weight", result)
    }

    @Test
    fun isParameterValid_withBoundaryValues() {
        assertTrue("Min boundary should be valid", 
            validator.isParameterValid(BodyParameterType.WEIGHT, 20.0))
        assertTrue("Max boundary should be valid", 
            validator.isParameterValid(BodyParameterType.WEIGHT, 300.0))
        assertFalse("Below min should be invalid", 
            validator.isParameterValid(BodyParameterType.WEIGHT, 19.99))
        assertFalse("Above max should be invalid", 
            validator.isParameterValid(BodyParameterType.WEIGHT, 300.01))
    }

    @Test
    fun validate_withMixedValidAndInvalid_returnsErrorOnFirstInvalid() {
        val input = mapOf(
            BodyParameterType.WEIGHT to 85.0,
            BodyParameterType.WAIST to 5.0
        )

        val result = validator.validate(input)

        assertTrue("Should return Error", result is MeasurementValidator.ValidationResult.Error)
    }

    @Test
    fun validate_withAllCircumferences_returnsSuccess() {
        val input = mapOf(
            BodyParameterType.CHEST to 100.0,
            BodyParameterType.WAIST to 85.0,
            BodyParameterType.HIPS to 95.0,
            BodyParameterType.BICEPS to 35.0,
            BodyParameterType.THIGH to 55.0,
            BodyParameterType.CALF to 38.0,
            BodyParameterType.NECK to 38.0,
            BodyParameterType.SHOULDERS to 115.0
        )

        val result = validator.validate(input)

        assertTrue("Should return Success for all circumferences", 
            result is MeasurementValidator.ValidationResult.Success)
    }
}
