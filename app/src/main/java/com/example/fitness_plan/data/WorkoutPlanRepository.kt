package com.example.fitness_plan.data

import javax.inject.Inject
import javax.inject.Singleton

// Аннотация @Singleton и @Inject позволяет Hilt управлять этим классом
@Singleton
class WorkoutPlanRepository @Inject constructor() {

    fun getWorkoutPlanForUser(userProfile: UserProfile): WorkoutPlan {
        // Простая логика выбора: сейчас просто возвращаем один предопределенный план
        // В реальном приложении здесь будет сложная логика сопоставления
        // параметров (цель, уровень, частота) с шаблонами планов.

        // Временно возвращаем план для новичков, пока не реализуем полную логику
        return noviceFatLossPlan
    }

    // Здесь хранятся шаблоны планов тренировок
    private val noviceFatLossPlan = WorkoutPlan(
        id = "novice_fat_loss_3x",
        name = "План: Похудение для новичков",
        description = "Тренировка всего тела 3 раза в неделю с акцентом на базовые движения и кардио.",
        frequency = "3 раза в неделю",
        targetGoal = "Похудение",
        targetLevel = "Новичок",
        days = listOf(
            WorkoutDay(
                dayName = "Тренировка 1 (Фулбоди)",
                exercises = listOf(
                    Exercise(name = "Приседания со штангой", sets = 3, reps = "10-12"),
                    Exercise(name = "Жим гантелей лежа", sets = 3, reps = "8-10"),
                    Exercise(name = "Тяга верхнего блока", sets = 3, reps = "10-12"),
                    Exercise(name = "Жим гантелей стоя", sets = 2, reps = "10-12"),
                    Exercise(name = "Планка", sets = 3, reps = "До отказа"),
                    Exercise(name = "Беговая дорожка", sets = 1, reps = "15 мин")
                )
            ),
            // ... (Второй и третий день плана можно добавить позже) ...
        )
    )
}
