package com.example.fitness_plan.data

// Модель для отдельного упражнения в плане
data class Exercise(
    val name: String,         // Название упражнения (напр., "Жим лежа")
    val sets: Int,            // Количество подходов (напр., 3)
    val reps: String,         // Количество повторений или диапазон (напр., "8-12" или "15 мин")
    val description: String? = null // Дополнительные инструкции
)

// Модель для одного дня тренировки
data class WorkoutDay(
    val dayName: String,      // Название дня (напр., "Понедельник" или "День 1: Верх")
    val exercises: List<Exercise> // Список упражнений на этот день
)

// Основная модель для всего плана тренировок
data class WorkoutPlan(
    val id: String,           // Уникальный ID плана (напр., "novice_fat_loss_3x")
    val name: String,         // Отображаемое название (напр., "План: Похудение для новичков")
    val description: String,  // Краткое описание плана
    val frequency: String,    // Целевая частота (напр., "3 раза в неделю")
    val targetGoal: String,   // Целевая цель (напр., "Похудение")
    val targetLevel: String,  // Целевой уровень (напр., "Новичок")
    val days: List<WorkoutDay> // Список тренировочных дней
)

