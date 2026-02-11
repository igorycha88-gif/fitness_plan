package com.example.fitness_plan.domain.model

data class MeasurementInput(
    val date: Long = System.currentTimeMillis(),
    val parameters: Map<BodyParameterType, Double?> = emptyMap()
) {
    fun hasAnyData(): Boolean = parameters.values.any { it != null }
    
    fun getFilledParameters(): Map<BodyParameterType, Double> {
        return parameters.filter { it.value != null }.mapValues { it.value!! }
    }
    
    fun isParameterFilled(type: BodyParameterType): Boolean {
        return parameters[type] != null
    }
}
