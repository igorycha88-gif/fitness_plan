package com.example.fitness_plan.domain.model

data class UserProfile(
    val username: String = "",
    val goal: String,
    val level: String,
    val frequency: String,
    val weight: Double,
    val height: Double,
    val gender: String
) {
    companion object {
        const val GOAL_WEIGHT_LOSS = "Похудение"
        const val GOAL_MUSCLE_GAIN = "Наращивание мышечной массы"
        const val GOAL_MAINTENANCE = "Поддержание формы"

        const val LEVEL_BEGINNER = "Новичок"
        const val LEVEL_INTERMEDIATE = "Любитель"
        const val LEVEL_ADVANCED = "Профессионал"

        const val FREQUENCY_1X = "1 раз в неделю"
        const val FREQUENCY_3X = "3 раза в неделю"
        const val FREQUENCY_5X = "5 раз в неделю"

        const val GENDER_MALE = "Мужской"
        const val GENDER_FEMALE = "Женский"
    }

    val isValid: Boolean
        get() = username.isNotBlank() &&
                goal.isNotBlank() &&
                level.isNotBlank() &&
                frequency.isNotBlank() &&
                weight > 0 &&
                height > 0 &&
                gender.isNotBlank()
}
