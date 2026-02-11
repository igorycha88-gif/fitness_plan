package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.BodyParameter
import com.example.fitness_plan.domain.model.BodyParameterType
import com.example.fitness_plan.domain.model.CalculationMethod
import java.lang.Math.log10
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyParameterCalculator @Inject constructor() {

    fun calculateBMI(weightKg: Double, heightCm: Double): Double {
        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }

    fun calculateBodyFatUSNavy(
        gender: String,
        heightCm: Double,
        neckCm: Double,
        waistCm: Double,
        hipsCm: Double? = null
    ): Double {
        return if (gender.lowercase() == "женский") {
            val sum = waistCm + (hipsCm ?: 0.0) - neckCm
            val value = 1.29579 - (0.35004 * log10(sum)) + (0.22100 * log10(heightCm))
            (495.0 / value) - 450.0
        } else {
            val diff = waistCm - neckCm
            val value = 1.0324 - (0.19077 * log10(diff)) + (0.15456 * log10(heightCm))
            (495.0 / value) - 450.0
        }
    }

    fun calculateMuscleMass(weightKg: Double, bodyFatPercent: Double): Double {
        val leanBodyMass = weightKg * (1 - bodyFatPercent / 100.0)
        return leanBodyMass * 0.55
    }

    fun calculateCalculatedParameters(
        input: Map<BodyParameterType, Double>,
        gender: String?,
        date: Long
    ): List<BodyParameter> {
        val calculatedParameters = mutableListOf<BodyParameter>()

        val weight = input[BodyParameterType.WEIGHT]
        val height = input[BodyParameterType.HEIGHT]
        val neck = input[BodyParameterType.NECK]
        val waist = input[BodyParameterType.WAIST]
        val hips = input[BodyParameterType.HIPS]

        if (weight != null && height != null) {
            val bmi = calculateBMI(weight, height)
            calculatedParameters.add(
                BodyParameter(
                    parameterType = BodyParameterType.BODY_MASS_INDEX,
                    value = bmi,
                    unit = "",
                    date = date,
                    calculationMethod = CalculationMethod.AUTO
                )
            )
        }

        if (gender != null && height != null && neck != null && waist != null) {
            val bodyFat = calculateBodyFatUSNavy(
                gender = gender,
                heightCm = height,
                neckCm = neck,
                waistCm = waist,
                hipsCm = hips
            )
            calculatedParameters.add(
                BodyParameter(
                    parameterType = BodyParameterType.BODY_FAT,
                    value = bodyFat,
                    unit = "%",
                    date = date,
                    calculationMethod = CalculationMethod.AUTO
                )
            )
        }

        val calculatedBodyFat = calculatedParameters.find { it.parameterType == BodyParameterType.BODY_FAT }
        if (weight != null && calculatedBodyFat != null) {
            val muscleMass = calculateMuscleMass(weight, calculatedBodyFat.value)
            calculatedParameters.add(
                BodyParameter(
                    parameterType = BodyParameterType.MUSCLE_MASS,
                    value = muscleMass,
                    unit = "кг",
                    date = date,
                    calculationMethod = CalculationMethod.AUTO
                )
            )
        }

        return calculatedParameters
    }
}
