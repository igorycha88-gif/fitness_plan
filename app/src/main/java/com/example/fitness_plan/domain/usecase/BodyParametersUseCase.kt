package com.example.fitness_plan.domain.usecase

import android.util.Log
import com.example.fitness_plan.domain.model.BodyParameter
import com.example.fitness_plan.domain.model.BodyParameterType
import com.example.fitness_plan.domain.model.MeasurementInput
import com.example.fitness_plan.domain.repository.BodyParametersRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BodyParametersUseCase"

@Singleton
class BodyParametersUseCase @Inject constructor(
    private val repository: BodyParametersRepository,
    private val calculator: BodyParameterCalculator,
    private val validator: MeasurementValidator
) {

    suspend fun saveMeasurement(
        username: String,
        input: MeasurementInput,
        gender: String?,
        age: Int?,
        calculateAuto: Boolean = true
    ): Result<Unit> {
        return try {
            Log.d(TAG, "saveMeasurement: START for username=$username")

            if (!input.hasAnyData()) {
                Log.d(TAG, "saveMeasurement: FAILED - no data provided")
                return Result.failure(Exception("Минимум 1 параметр должен быть заполнен"))
            }

            val validationResult = validator.validate(input.parameters)

            when (validationResult) {
                is MeasurementValidator.ValidationResult.Error -> {
                    Log.d(TAG, "saveMeasurement: FAILED - validation error: ${validationResult.errorMessage}")
                    return Result.failure(Exception(validationResult.errorMessage))
                }
                is MeasurementValidator.ValidationResult.Success -> {
                    val parametersToSave = mutableListOf<BodyParameter>()

                    validationResult.validatedData.forEach { (type, value) ->
                        parametersToSave.add(
                            BodyParameter(
                                parameterType = type,
                                value = value,
                                unit = type.unit,
                                date = input.date,
                                calculationMethod = com.example.fitness_plan.domain.model.CalculationMethod.MANUAL
                            )
                        )
                    }

                    if (calculateAuto && gender != null) {
                        val calculatedParams = calculator.calculateCalculatedParameters(
                            input = validationResult.validatedData,
                            gender = gender,
                            date = input.date
                        )
                        parametersToSave.addAll(calculatedParams)
                    }

                    repository.saveMeasurement(username, parametersToSave)
                    Log.d(TAG, "saveMeasurement: SUCCESS - saved ${parametersToSave.size} parameters")
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveMeasurement: FAILED", e)
            Result.failure(e)
        }
    }

    fun getMeasurements(username: String): Flow<List<BodyParameter>> {
        return repository.getMeasurements(username)
    }

    fun getLatestMeasurements(username: String): Flow<Map<BodyParameterType, BodyParameter>> {
        return repository.getLatestMeasurements(username)
    }

    suspend fun deleteMeasurement(username: String, measurementId: String): Result<Unit> {
        return try {
            Log.d(TAG, "deleteMeasurement: START for username=$username, measurementId=$measurementId")
            repository.deleteMeasurement(username, measurementId)
            Log.d(TAG, "deleteMeasurement: SUCCESS")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteMeasurement: FAILED", e)
            Result.failure(e)
        }
    }

    suspend fun clearAllMeasurements(username: String): Result<Unit> {
        return try {
            Log.d(TAG, "clearAllMeasurements: START for username=$username")
            repository.clearAllMeasurements(username)
            Log.d(TAG, "clearAllMeasurements: SUCCESS")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "clearAllMeasurements: FAILED", e)
            Result.failure(e)
        }
    }

    fun getMeasurementsByType(
        username: String,
        type: BodyParameterType
    ): Flow<List<BodyParameter>> {
        return repository.getMeasurementsByType(username, type)
    }

    fun getMeasurementsByDateRange(
        username: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<BodyParameter>> {
        return repository.getMeasurementsByDateRange(username, startDate, endDate)
    }
}
