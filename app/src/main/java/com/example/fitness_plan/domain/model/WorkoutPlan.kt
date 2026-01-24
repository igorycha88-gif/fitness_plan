package com.example.fitness_plan.domain.model

data class WorkoutPlan(
    val id: String = "",
    val name: String,
    val description: String,
    val muscleGroups: List<String>,
    val days: List<WorkoutDay>,
    val goal: String = "",
    val level: String = ""
) {
    val totalExercises: Int
        get() = days.sumOf { it.exercises.size }

    val completedDaysCount: Int
        get() = days.count { day ->
            day.exercises.all { it.isCompleted }
        }

    val progress: Float
        get() = if (days.isEmpty()) 0f else completedDaysCount.toFloat() / days.size
}

data class WorkoutDay(
    val id: Int,
    val dayName: String,
    val exercises: List<Exercise>,
    val muscleGroups: List<String>,
    val scheduledDate: Long? = null
) {
    val isCompleted: Boolean
        get() = exercises.isNotEmpty() && exercises.all { it.isCompleted }

    val completedExercisesCount: Int
        get() = exercises.count { it.isCompleted }
}

 data class Exercise(
    val id: String = "",
    val name: String,
    val sets: Int,
    val reps: String,
    val weight: Float? = null,
    val imageRes: String? = null,
    val isCompleted: Boolean = false,
    val alternatives: List<Exercise> = emptyList(),
    val description: String? = null,
    val recommendedWeight: Float? = null,
    val recommendedRepsPerSet: String? = null,
    val equipment: List<EquipmentType> = emptyList(),
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val exerciseType: ExerciseType = ExerciseType.STRENGTH,
    val stepByStepInstructions: String? = null,
    val animationUrl: String? = null
)

data class ExerciseWithStats(
    val exercise: Exercise,
    val maxWeight: Double = 0.0,
    val averageWeight: Double = 0.0,
    val totalVolume: Long = 0,
    val completionPercentage: Float = 0f
)
