package com.example.fitness_plan.domain.model

enum class BodyParameterType(
    val displayName: String,
    val unit: String,
    val isRequired: Boolean = false,
    val canCalculate: Boolean = false,
    val requiredForCalculation: List<BodyParameterType> = emptyList()
) {
    WEIGHT("Вес", "кг"),
    HEIGHT("Рост", "см"),
    CHEST("Обхват груди", "см"),
    WAIST("Обхват талии", "см"),
    HIPS("Обхват бёдер", "см"),
    BICEPS("Бицепс", "см"),
    THIGH("Бедро", "см"),
    CALF("Икра", "см"),
    NECK("Шея", "см"),
    SHOULDERS("Плечи", "см"),
    
    BODY_FAT("Жир в организме", "%", canCalculate = true),
    BODY_MASS_INDEX("ИМТ", "", canCalculate = true),
    MUSCLE_MASS("Мышечная масса", "кг", canCalculate = true)
}
