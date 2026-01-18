package com.example.fitness_plan.data

// Модель для отдельного упражнения в плане
data class Exercise(
    val name: String,         // Название упражнения (напр., "Жим лежа")
    val sets: Int,            // Количество подходов (напр., 3)
    val reps: String,         // Количество повторений или диапазон (напр., "8-12" или "15 мин")
    val weight: String = "",  // Рекомендуемый вес (% или описание)
    val rest: String = "90 сек", // Время отдыха
    val description: String? = null, // Дополнительные инструкции
    val imageRes: String? = null, // Ресурс картинки упражнения
    val muscleGroups: List<String> = emptyList(), // Группы мышц
    val alternatives: List<Exercise> = emptyList() // Альтернативные упражнения
)

// Модель для одного дня тренировки
data class WorkoutDay(
    val dayName: String,      // Название дня (напр., "Понедельник" или "День 1: Верх")
    val exercises: List<Exercise>, // Список упражнений на этот день
    val muscleGroups: List<String> = exercises.flatMap { it.muscleGroups }.distinct(), // Группы мышц для дня
    val scheduledDate: Long? = null // Запланированная дата тренировки
)

// Модель для одной недели тренировок
data class Week(
    val weekNumber: Int,      // Номер недели (1, 2, 3, 4)
    val description: String,  // Описание недели
    val focus: String = "",   // Фокус недели (напр., "Адаптация")
    val workoutPlan: WorkoutPlan // План на эту неделю
)

// Основная модель для всего плана тренировок
data class WorkoutPlan(
    val id: String,           // Уникальный ID плана (напр., "novice_fat_loss_3x")
    val name: String,         // Отображаемое название (напр., "План: Похудение для новичков")
    val description: String,  // Краткое описание плана
    val frequency: String,    // Целевая частота (напр., "3 раза в неделю")
    val targetGoal: String,   // Целевая цель (напр., "Похудение")
    val targetLevel: String,  // Целевой уровень (напр., "Новичок")
    val days: List<WorkoutDay> = emptyList(), // Список тренировочных дней
    val muscleGroups: List<String> = emptyList(), // Группы мышц для плана
    val weeks: List<Week> = emptyList() // Список недель (для многонедельных планов)
)

// Статистика выполнения упражнения
data class ExerciseStats(
    val exerciseName: String,
    val date: Long,
    val weight: Double,
    val reps: Int,
    val sets: Int
)

