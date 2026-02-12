package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.model.Exercise
import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.ExerciseSummary
import com.example.fitness_plan.domain.model.WorkoutDay
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseStatsRepository: ExerciseStatsRepository,
    private val exerciseCompletionRepository: ExerciseCompletionRepository,
    private val muscleGroupStatsUseCase: MuscleGroupStatsUseCase
) {
    data class WorkoutState(
        val workoutPlan: WorkoutPlan,
        val completedExercises: Set<String>,
        val completedDays: Set<Int>
    )

    data class DayCompletionStatus(
        val fullyCompletedDays: Set<Int>,
        val partiallyCompletedDays: Set<Int>
    )

    suspend fun getWorkoutPlan(username: String): WorkoutPlan? {
        return workoutRepository.getWorkoutPlan(username).first()
    }

    fun getCompletedExercises(username: String): Flow<Set<String>> {
        return exerciseCompletionRepository.getAllCompletedExercises(username)
    }

    fun getExerciseStats(username: String): Flow<List<ExerciseStats>> {
        return exerciseStatsRepository.getExerciseStats(username)
    }

    suspend fun toggleExerciseCompletion(
        username: String,
        exerciseKey: String,
        completed: Boolean,
        currentPlan: WorkoutPlan?
    ): Set<Int> {
        val exerciseName = if (exerciseKey.contains("_")) {
            exerciseKey.substringAfter("_")
        } else {
            exerciseKey
        }

        if (exerciseKey.contains("_")) {
            exerciseCompletionRepository.setExerciseCompleted(username, exerciseKey, completed)
            if (completed) {
                saveExerciseStatsForCompletedExercise(username, exerciseName, currentPlan)
            }
        } else {
            val exerciseKeysToToggle = mutableSetOf<String>()
            currentPlan?.days?.forEachIndexed { dayIndex, day ->
                if (day.exercises.any { it.name == exerciseName }) {
                    exerciseKeysToToggle.add("${dayIndex}_${exerciseName}")
                }
            }

            exerciseKeysToToggle.forEach { key ->
                exerciseCompletionRepository.setExerciseCompleted(username, key, completed)
            }
            if (completed) {
                saveExerciseStatsForCompletedExercise(username, exerciseName, currentPlan)
            }
        }

        val allCompleted = exerciseCompletionRepository.getAllCompletedExercises(username).first()
        val status = calculateCompletedDays(allCompleted, currentPlan)
        return status.fullyCompletedDays
    }

    private suspend fun saveExerciseStatsForCompletedExercise(
        username: String,
        exerciseName: String,
        currentPlan: WorkoutPlan?
    ) {
        val existingStats = exerciseStatsRepository.getExerciseStats(username).first()
            .filter { it.exerciseName == exerciseName }
            .sortedByDescending { it.date }

        if (existingStats.isNotEmpty()) {
            return
        }

        val exercise = currentPlan?.days?.flatMap { it.exercises }
            ?.find { it.name == exerciseName }

        if (exercise != null) {
            val totalSets = exercise.sets
            val repsList = parseRepsString(exercise.reps)
            val defaultWeight = exercise.recommendedWeight?.toDouble() ?: 1.0

            for (setNumber in 1..totalSets) {
                val reps = repsList.getOrElse(setNumber - 1) { repsList.lastOrNull() ?: 1 }
                val stats = ExerciseStats(
                    exerciseName = exerciseName,
                    date = System.currentTimeMillis(),
                    weight = defaultWeight,
                    reps = reps,
                    setNumber = setNumber,
                    sets = totalSets
                )
                exerciseStatsRepository.saveExerciseStats(username, stats)
            }
            android.util.Log.d("WorkoutUseCase", "✅ Автоматически сохранена статистика для '$exerciseName': $totalSets подходов")
        }
    }

    private fun parseRepsString(repsString: String): List<Int> {
        return try {
            if (repsString.contains("-")) {
                repsString.split("-").map { it.trim().toInt() }
            } else if (repsString.contains(",")) {
                repsString.split(",").map { it.trim().toInt() }
            } else {
                listOf(repsString.trim().toInt())
            }
        } catch (e: Exception) {
            android.util.Log.w("WorkoutUseCase", "Ошибка парсинга повторений '$repsString': ${e.message}")
            listOf(1)
        }
    }

    private fun calculateCompletedDays(
        completedExercises: Set<String>,
        plan: WorkoutPlan?
    ): DayCompletionStatus {
        val fullyCompleted = mutableSetOf<Int>()
        val partiallyCompleted = mutableSetOf<Int>()
        plan?.days?.forEachIndexed { dayIndex, day ->
            val dayExerciseKeys = day.exercises.map { "${dayIndex}_${it.name}" }.toSet()
            val completedCount = dayExerciseKeys.count { it in completedExercises }
            val totalCount = dayExerciseKeys.size
            when {
                completedCount == 0 -> {}
                completedCount == totalCount -> fullyCompleted.add(dayIndex)
                else -> partiallyCompleted.add(dayIndex)
            }
        }
        return DayCompletionStatus(fullyCompleted, partiallyCompleted)
    }

    suspend fun saveExerciseStats(
        username: String,
        exerciseName: String,
        weight: Double,
        reps: Int,
        setNumber: Int,
        sets: Int,
        duration: Int = 0
    ) {
        android.util.Log.d("WorkoutUseCase", "=== Сохранение статистики упражнения (UseCase уровень) ===")
        android.util.Log.d("WorkoutUseCase", "Пользователь: $username")
        android.util.Log.d("WorkoutUseCase", "Упражнение: $exerciseName")
        android.util.Log.d("WorkoutUseCase", "Вес: $weight кг")
        android.util.Log.d("WorkoutUseCase", "Повторения: $reps")
        android.util.Log.d("WorkoutUseCase", "Номер подхода: $setNumber")
        android.util.Log.d("WorkoutUseCase", "Количество подходов: $sets")
        android.util.Log.d("WorkoutUseCase", "Длительность: $duration мин")

        val stats = ExerciseStats(
            exerciseName = exerciseName,
            date = System.currentTimeMillis(),
            weight = weight,
            reps = reps,
            setNumber = setNumber,
            sets = sets,
            duration = duration
        )

        android.util.Log.d("WorkoutUseCase", "Создан объект ExerciseStats: $stats")
        android.util.Log.d("WorkoutUseCase", "Вызов repository.saveExerciseStats...")

        exerciseStatsRepository.saveExerciseStats(username, stats)

        android.util.Log.d("WorkoutUseCase", "✅ Статистика упражнения успешно сохранена через UseCase")

        muscleGroupStatsUseCase.saveMuscleGroupStatsForExercise(
            username,
            exerciseName,
            weight,
            reps,
            setNumber
        )
    }

    fun getExerciseSummaries(
        username: String,
        exerciseNames: List<String>
    ): Flow<List<ExerciseSummary>> {
        return exerciseStatsRepository.getExerciseStats(username).map { allStats ->
            exerciseNames.map { name ->
                val exerciseStats = allStats.filter { it.exerciseName == name }
                ExerciseSummary(
                    exerciseName = name,
                    maxWeight = exerciseStats.maxOfOrNull { it.weight } ?: 0.0,
                    averageWeight = if (exerciseStats.isNotEmpty())
                        exerciseStats.map { it.weight }.average()
                    else 0.0,
                    totalVolume = exerciseStats.sumOf { (it.weight * it.reps).toLong() },
                    totalSets = exerciseStats.size,
                    completionPercentage = 0f
                )
            }
        }
    }

    suspend fun updateWorkoutSchedule(username: String, dates: List<Long>) {
        workoutRepository.saveWorkoutSchedule(username, dates)
    }

    suspend fun saveAdminWorkoutPlan(plan: WorkoutPlan) {
        workoutRepository.saveAdminWorkoutPlan(plan)
    }

    fun getAdminWorkoutPlan(): Flow<WorkoutPlan?> {
        return workoutRepository.getAdminWorkoutPlan()
    }

    suspend fun createUserPlan(
        username: String,
        name: String,
        description: String
    ) {
        val plan = WorkoutPlan(
            id = "${username}_user_plan",
            name = name,
            description = description,
            muscleGroups = listOf(),
            goal = "Custom",
            level = "Custom",
            days = emptyList(),
            planType = com.example.fitness_plan.domain.model.PlanType.USER
        )
        workoutRepository.saveUserWorkoutPlan(username, plan)
    }

    suspend fun addDayToUserPlan(
        username: String,
        dayName: String
    ) {
        val currentPlan = getUserWorkoutPlan(username).first()
        if (currentPlan == null) return

        if (currentPlan.days.size >= MAX_DAYS_IN_PLAN) {
            throw MaxDaysLimitExceededException("Максимальное количество дней: $MAX_DAYS_IN_PLAN")
        }

        val newDay = WorkoutDay(
            id = currentPlan.days.size,
            dayName = dayName,
            exercises = emptyList(),
            muscleGroups = listOf()
        )
        val updatedDays = currentPlan.days.toMutableList().apply { add(newDay) }
        val updatedPlan = currentPlan.copy(days = updatedDays)
        workoutRepository.saveUserWorkoutPlan(username, updatedPlan)
    }

    suspend fun addExerciseToUserDay(
        username: String,
        dayIndex: Int,
        exercise: Exercise
    ) {
        val currentPlan = getUserWorkoutPlan(username).first()
        if (currentPlan == null) return

        val updatedDays = currentPlan.days.toMutableList()
        if (dayIndex in updatedDays.indices) {
            val day = updatedDays[dayIndex]
            val updatedExercises = day.exercises.toMutableList().apply { add(exercise) }
            
            val existingMuscleGroups = day.muscleGroups.toMutableList()
            val newMuscleGroups = exercise.muscleGroups.map { it.displayName }
            val allMuscleGroups = (existingMuscleGroups + newMuscleGroups).distinct()
            
            updatedDays[dayIndex] = day.copy(
                exercises = updatedExercises,
                muscleGroups = allMuscleGroups
            )
        }
        val updatedPlan = currentPlan.copy(days = updatedDays)
        workoutRepository.saveUserWorkoutPlan(username, updatedPlan)
    }

    suspend fun removeExerciseFromUserDay(
        username: String,
        dayIndex: Int,
        exerciseName: String
    ) {
        val currentPlan = getUserWorkoutPlan(username).first()
        if (currentPlan == null) return

        val updatedDays = currentPlan.days.toMutableList()
        if (dayIndex in updatedDays.indices) {
            val day = updatedDays[dayIndex]
            val updatedExercises = day.exercises.filterNot { it.name == exerciseName }
            
            val remainingMuscleGroups = updatedExercises
                .flatMap { it.muscleGroups }
                .map { it.displayName }
                .distinct()
            
            updatedDays[dayIndex] = day.copy(
                exercises = updatedExercises,
                muscleGroups = remainingMuscleGroups
            )
        }
        val updatedPlan = currentPlan.copy(days = updatedDays)
        workoutRepository.saveUserWorkoutPlan(username, updatedPlan)
    }

    suspend fun deleteUserPlan(username: String) {
        workoutRepository.deleteUserWorkoutPlan(username)
        setSelectedPlanType(username, com.example.fitness_plan.domain.repository.SelectedPlanType.AUTO)
    }

    suspend fun updateUserPlan(username: String, name: String, description: String) {
        val currentPlan = getUserWorkoutPlan(username).first()
        if (currentPlan == null) return

        val updatedPlan = currentPlan.copy(
            name = name,
            description = description
        )
        workoutRepository.updateUserPlan(username, updatedPlan)
    }

    suspend fun updateUserWorkoutDayDate(username: String, dayIndex: Int, date: Long?) {
        val currentPlan = getUserWorkoutPlan(username).first()
        if (currentPlan == null) return

        val updatedDays = currentPlan.days.toMutableList().apply {
            if (dayIndex in indices) {
                this[dayIndex] = this[dayIndex].copy(scheduledDate = date)
            }
        }
        val updatedPlan = currentPlan.copy(days = updatedDays)
        workoutRepository.updateUserPlan(username, updatedPlan)
    }

    fun getUserWorkoutPlan(username: String): Flow<WorkoutPlan?> {
        return workoutRepository.getUserWorkoutPlan(username)
    }

    suspend fun setSelectedPlanType(username: String, planType: com.example.fitness_plan.domain.repository.SelectedPlanType) {
        workoutRepository.setSelectedPlanType(username, planType)
    }

    fun getSelectedPlanType(username: String): Flow<com.example.fitness_plan.domain.repository.SelectedPlanType> {
        return workoutRepository.getSelectedPlanType(username)
    }
}
