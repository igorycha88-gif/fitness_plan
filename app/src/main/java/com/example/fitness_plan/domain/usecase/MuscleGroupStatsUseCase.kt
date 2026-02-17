package com.example.fitness_plan.domain.usecase

import android.util.Log
import com.example.fitness_plan.domain.model.*
import com.example.fitness_plan.domain.repository.ExerciseLibraryRepository
import com.example.fitness_plan.domain.repository.MuscleGroupStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

private const val TAG = "MuscleGroupStatsUseCase"

class MuscleGroupStatsUseCase @Inject constructor(
    private val muscleGroupStatsRepository: MuscleGroupStatsRepository,
    private val exerciseLibraryRepository: ExerciseLibraryRepository
) {

    fun getMuscleGroupSummaries(
        username: String,
        filter: MuscleGroupStatsFilter
    ): Flow<List<MuscleGroupSummary>> {
        return muscleGroupStatsRepository.getMuscleGroupStats(username).map { stats ->
            calculateSummaries(stats, filter)
        }
    }

    fun getMuscleGroupSummariesFlow(
        username: String
    ): Flow<List<MuscleGroupSummary>> {
        return muscleGroupStatsRepository.getMuscleGroupStats(username).map { stats ->
            calculateSummaries(stats, MuscleGroupStatsFilter.MONTH)
        }
    }

    private fun calculateSummaries(
        stats: List<MuscleGroupStats>,
        filter: MuscleGroupStatsFilter
    ): List<MuscleGroupSummary> {
        if (stats.isEmpty()) return emptyList()

        val filteredStats = filterByPeriod(stats, filter)

        val groupedByMuscle = filteredStats.groupBy { it.muscleGroup }

        val totalVolume = filteredStats.sumOf { it.volume }

        val now = System.currentTimeMillis()

        return groupedByMuscle.map { (muscleGroup, muscleStats) ->
            val volume = muscleStats.sumOf { it.volume }
            val sets = muscleStats.size
            val exerciseNames = muscleStats.map { it.exerciseName }.distinct()
            val maxWeight = muscleStats.maxOfOrNull { it.weight } ?: 0.0
            val percentage = if (totalVolume > 0L) {
                (volume.toFloat() / totalVolume) * 100f
            } else {
                0f
            }

            val lastWorkoutDate = muscleStats.maxOfOrNull { it.date } ?: 0L
            val daysSinceLastWorkout = if (lastWorkoutDate > 0) {
                ((now - lastWorkoutDate) / (24 * 60 * 60 * 1000L)).toInt()
            } else {
                Int.MAX_VALUE
            }

            val workoutDays = muscleStats.map { stat ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = stat.date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }.distinct()

            val periodDays = if (filter.days > 0) filter.days else {
                val firstDate = stats.minOfOrNull { it.date } ?: now
                ((now - firstDate) / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(1)
            }

            val weeks = periodDays / 7f
            val averageWeeklyFrequency = if (weeks > 0) {
                workoutDays.size / weeks
            } else {
                workoutDays.size.toFloat()
            }

            MuscleGroupSummary(
                muscleGroup = muscleGroup,
                totalVolume = volume,
                totalSets = sets,
                exerciseCount = exerciseNames.size,
                maxWeight = maxWeight,
                percentage = percentage,
                daysSinceLastWorkout = daysSinceLastWorkout,
                averageWeeklyFrequency = averageWeeklyFrequency,
                exerciseNames = exerciseNames
            )
        }.sortedByDescending { it.totalVolume }
    }

    private fun filterByPeriod(
        stats: List<MuscleGroupStats>,
        filter: MuscleGroupStatsFilter
    ): List<MuscleGroupStats> {
        if (filter.days == 0) return stats

        val cutoffTime = System.currentTimeMillis() - (filter.days * 24 * 60 * 60 * 1000L)
        return stats.filter { it.date >= cutoffTime }
    }

    suspend fun getMuscleGroupDetail(
        username: String,
        muscleGroup: MuscleGroup,
        filter: MuscleGroupStatsFilter
    ): MuscleGroupDetail? {
        val stats = muscleGroupStatsRepository.getMuscleGroupStats(username).first()
            .filter { it.muscleGroup == muscleGroup }

        if (stats.isEmpty()) return null

        val filteredStats = filterByPeriod(stats, filter)

        val progress = calculateProgress(filteredStats)
        val recentExercises = calculateRecentExercises(filteredStats)
        val averageWeeklyFrequency = calculateAverageFrequency(filteredStats, filter)
        val totalVolume = filteredStats.sumOf { it.volume }
        val totalSets = filteredStats.size
        val totalExercises = filteredStats.map { it.exerciseName }.distinct().size
        val maxWeight = filteredStats.maxOfOrNull { it.weight } ?: 0.0
        val lastWorkoutDate = filteredStats.maxOfOrNull { it.date } ?: 0L

        return MuscleGroupDetail(
            muscleGroup = muscleGroup,
            progress = progress,
            recentExercises = recentExercises,
            averageWeeklyFrequency = averageWeeklyFrequency,
            totalVolume = totalVolume,
            totalSets = totalSets,
            totalExercises = totalExercises,
            maxWeight = maxWeight,
            lastWorkoutDate = lastWorkoutDate
        )
    }

    private fun calculateProgress(stats: List<MuscleGroupStats>): List<MuscleGroupProgress> {
        return stats.groupBy { stat ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = stat.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.map { (date, dayStats) ->
            MuscleGroupProgress(
                muscleGroup = dayStats.first().muscleGroup,
                date = date,
                volume = dayStats.sumOf { it.volume },
                sets = dayStats.size,
                maxWeight = dayStats.maxOfOrNull { it.weight } ?: 0.0
            )
        }.sortedBy { it.date }
    }

    private fun calculateRecentExercises(
        stats: List<MuscleGroupStats>
    ): List<MuscleGroupExerciseInfo> {
        return stats
            .sortedByDescending { it.date }
            .take(20)
            .map { stat ->
                MuscleGroupExerciseInfo(
                    exerciseName = stat.exerciseName,
                    date = stat.date,
                    weight = stat.weight,
                    reps = stat.reps,
                    volume = stat.volume
                )
            }
    }

    private fun calculateAverageFrequency(
        stats: List<MuscleGroupStats>,
        filter: MuscleGroupStatsFilter
    ): Float {
        val workoutDays = stats.map { stat ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = stat.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.distinct().size

        val periodDays = if (filter.days > 0) filter.days else {
            val now = System.currentTimeMillis()
            val firstDate = stats.minOfOrNull { it.date } ?: now
            ((now - firstDate) / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(1)
        }

        val weeks = periodDays / 7f
        return if (weeks > 0) workoutDays / weeks else workoutDays.toFloat()
    }

    suspend fun saveMuscleGroupStatsForExercise(
        username: String,
        exerciseName: String,
        weight: Double,
        reps: Int,
        setNumber: Int
    ) {
        val exercises = exerciseLibraryRepository.getAllExercises().first()
        val exercise = exercises.find { it.name == exerciseName }

        if (exercise != null) {
            val statsList = exercise.muscleGroups.map { muscleGroup ->
                MuscleGroupStats(
                    muscleGroup = muscleGroup,
                    exerciseName = exerciseName,
                    date = System.currentTimeMillis(),
                    weight = weight,
                    reps = reps,
                    setNumber = setNumber
                )
            }
            muscleGroupStatsRepository.saveMuscleGroupStats(username, statsList)
            Log.d(TAG, "Сохранена статистика для $exerciseName: ${statsList.size} групп мышц")
        } else {
            Log.w(TAG, "Упражнение $exerciseName не найдено в библиотеке")
        }
    }
}