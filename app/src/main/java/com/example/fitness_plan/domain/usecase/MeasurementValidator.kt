package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.BodyParameterType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementValidator @Inject constructor() {

    sealed class ValidationResult {
        data class Success(val validatedData: Map<BodyParameterType, Double>) : ValidationResult()
        data class Error(val errorMessage: String) : ValidationResult()
    }

    private val validationRules = mapOf(
        BodyParameterType.WEIGHT to ValidationRule(20.0, 300.0, "Вес должен быть от 20 до 300 кг"),
        BodyParameterType.HEIGHT to ValidationRule(50.0, 250.0, "Рост должен быть от 50 до 250 см"),
        BodyParameterType.CHEST to ValidationRule(10.0, 200.0, "Параметр должен быть от 10 до 200 см"),
        BodyParameterType.WAIST to ValidationRule(10.0, 200.0, "Параметр должен быть от 10 до 200 см"),
        BodyParameterType.HIPS to ValidationRule(10.0, 200.0, "Параметр должен быть от 10 до 200 см"),
        BodyParameterType.BICEPS to ValidationRule(10.0, 200.0, "Параметр должен быть от 10 до 200 см"),
        BodyParameterType.THIGH to ValidationRule(10.0, 200.0, "Параметр должен быть от 10 до 200 см"),
        BodyParameterType.CALF to ValidationRule(10.0, 200.0, "Параметр должен быть от 10 до 200 см"),
        BodyParameterType.NECK to ValidationRule(10.0, 200.0, "Параметр должен быть от 10 до 200 см"),
        BodyParameterType.SHOULDERS to ValidationRule(10.0, 200.0, "Параметр должен быть от 10 до 200 см"),
        BodyParameterType.BODY_FAT to ValidationRule(1.0, 60.0, "% жира должен быть от 1 до 60%"),
        BodyParameterType.BODY_MASS_INDEX to ValidationRule(10.0, 60.0, "ИМТ должен быть от 10 до 60"),
        BodyParameterType.MUSCLE_MASS to ValidationRule(10.0, 150.0, "Мышечная масса должна быть от 10 до 150 кг")
    )

    data class ValidationRule(
        val min: Double,
        val max: Double,
        val errorMessage: String
    )

    fun validate(input: Map<BodyParameterType, Double?>): ValidationResult {
        val filledParameters = input.filter { it.value != null }.mapValues { it.value!! }

        if (filledParameters.isEmpty()) {
            return ValidationResult.Error("Минимум 1 параметр должен быть заполнен")
        }

        for ((type, value) in filledParameters) {
            val rule = validationRules[type]
            if (rule != null) {
                if (value < rule.min || value > rule.max) {
                    return ValidationResult.Error(rule.errorMessage)
                }
            }
        }

        return ValidationResult.Success(filledParameters)
    }

    fun isParameterValid(type: BodyParameterType, value: Double): Boolean {
        val rule = validationRules[type] ?: return true
        return value >= rule.min && value <= rule.max
    }
}
