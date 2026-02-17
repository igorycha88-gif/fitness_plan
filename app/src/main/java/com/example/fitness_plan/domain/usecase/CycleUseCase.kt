package com.example.fitness_plan.domain.usecase

import android.util.Log
import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleExerciseHistory
import com.example.fitness_plan.domain.model.CycleHistoryEntry
import com.example.fitness_plan.domain.model.MuscleGroupSequence
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WorkoutPlan
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseCompletionRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

private const val TAG = "CycleUseCase"

class CycleUseCase @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository,
    private val exerciseCompletionRepository: ExerciseCompletionRepository,
    private val weightProgressionUseCase: WeightProgressionUseCase,
    private val exercisePoolManager: ExercisePoolManager
) {
    data class CycleState(
        val cycle: Cycle?,
        val workoutPlan: WorkoutPlan?,
        val history: List<CycleHistoryEntry>
    )

    suspend fun initializeCycleForUser(username: String, profile: UserProfile): CycleState {
        Log.d(TAG, "initializeCycleForUser: username=$username, goal=${profile.goal}, level=${profile.level}")

        val completedDate = cycleRepository.getCompletedDate(username)
        val currentCycle = cycleRepository.getCurrentCycleSync(username)
        val now = System.currentTimeMillis()

        Log.d(TAG, "completedDate=$completedDate, currentCycle=${currentCycle?.cycleNumber}, totalDays=${currentCycle?.totalDays}")

        var shouldCreateNewPlan = false

        val cycle = when {
            completedDate != null -> {
                Log.d(TAG, "Completing old cycle and starting new one")
                cycleRepository.resetCycle(username)
                exerciseCompletionRepository.clearCompletion(username)
                shouldCreateNewPlan = true
                cycleRepository.startNewCycle(username, now)
            }
            currentCycle == null -> {
                Log.d(TAG, "Starting new cycle")
                shouldCreateNewPlan = true
                cycleRepository.startNewCycle(username, now)
            }
            currentCycle.totalDays != Cycle.DAYS_IN_CYCLE -> {
                Log.d(TAG, "Migrating old ${currentCycle.totalDays}-day cycle to new ${Cycle.DAYS_IN_CYCLE}-day cycle")
                cycleRepository.markCycleCompleted(username, now)
                cycleRepository.resetCycle(username)
                exerciseCompletionRepository.clearCompletion(username)
                shouldCreateNewPlan = true
                cycleRepository.startNewCycle(username, now)
            }
            else -> {
                Log.d(TAG, "Using existing cycle ${currentCycle.cycleNumber}")
                shouldCreateNewPlan = false
                currentCycle
            }
        }

        val plan = loadWorkoutPlan(username, profile, cycle, shouldCreateNewPlan)
        Log.d(TAG, "Workout plan loaded: ${plan.name}, days=${plan.days.size}")

        val history = cycleRepository.getCycleHistory(username).first()
        Log.d(TAG, "History size: ${history.size}")

        return CycleState(cycle, plan, history)
    }

    private suspend fun loadWorkoutPlan(
        username: String,
        profile: UserProfile,
        cycle: Cycle?,
        shouldCreateNewPlan: Boolean
    ): WorkoutPlan {
        if (!shouldCreateNewPlan) {
            val savedPlan = workoutRepository.getWorkoutPlan(username).first()
            if (savedPlan != null && savedPlan.days.isNotEmpty()) {
                Log.d(TAG, "Loaded existing plan from DataStore for user $username")
                return savedPlan
            }
        }

        Log.d(TAG, "Creating new plan with sequence for user $username")
        val exerciseHistory = cycleRepository.getExerciseHistory(username).first()
        val excludedExercises = buildExcludedExercisesMap(exerciseHistory)

        val planWithSequence = workoutRepository.getWorkoutPlanWithSequence(profile, excludedExercises)

        val dates = workoutRepository.generateCycleDates(
            cycle?.startDate ?: System.currentTimeMillis(),
            profile.frequency
        )
        val finalPlan = workoutRepository.getWorkoutPlanWithDates(planWithSequence, dates)

        val cycleNumber = cycle?.cycleNumber ?: 1
        val planWithProgression = applyWeightProgression(finalPlan, cycleNumber)

        workoutRepository.saveWorkoutPlan(username, planWithProgression)
        Log.d(TAG, "New plan saved to DataStore for user $username")

        saveCycleExerciseHistory(username, cycleNumber, planWithProgression)

        return planWithProgression
    }

    private fun buildExcludedExercisesMap(history: List<CycleExerciseHistory>): Map<String, Set<String>> {
        val result = mutableMapOf<String, Set<String>>()
        
        val lastHistory = history.lastOrNull()
        if (lastHistory != null) {
            for (sequence in MuscleGroupSequence.FULL_SEQUENCE) {
                val usedExercises = lastHistory.getUsedExercisesForGroup(sequence.displayName)
                if (usedExercises.isNotEmpty()) {
                    result[sequence.displayName] = usedExercises
                }
            }
        }
        
        return result
    }

    private suspend fun saveCycleExerciseHistory(username: String, cycleNumber: Int, plan: WorkoutPlan) {
        val usedExercises = mutableMapOf<String, Set<String>>()
        
        for (sequence in MuscleGroupSequence.FULL_SEQUENCE) {
            val exercisesForGroup = plan.days
                .filter { it.dayName.contains(sequence.displayName) }
                .flatMap { day -> day.exercises.map { it.name } }
                .toSet()
            
            if (exercisesForGroup.isNotEmpty()) {
                usedExercises[sequence.displayName] = exercisesForGroup
            }
        }

        val poolId = exercisePoolManager.determinePoolId(cycleNumber)
        
        val history = CycleExerciseHistory(
            cycleNumber = cycleNumber,
            startDate = System.currentTimeMillis(),
            usedExercises = usedExercises,
            poolId = poolId
        )

        cycleRepository.saveExerciseHistory(username, history)
        Log.d(TAG, "Saved exercise history for cycle $cycleNumber")
    }

    private fun applyWeightProgression(plan: WorkoutPlan, cycleNumber: Int): WorkoutPlan {
        val cycleGroup = (cycleNumber - 1) / 3
        val weightIncrement = cycleGroup * 2.0f
        
        val updatedDays = plan.days.map { day ->
            val updatedExercises = day.exercises.map { exercise ->
                exercise.copy(
                    recommendedWeight = exercise.recommendedWeight?.let { weight -> weight + weightIncrement }
                )
            }
            day.copy(exercises = updatedExercises)
        }
        
        return plan.copy(days = updatedDays)
    }

    suspend fun getCompletedCyclesCount(username: String): Int {
        val history = cycleRepository.getCycleHistory(username).first()
        return history.size
    }

    suspend fun getFullCycleGroups(username: String): Int {
        val completedCount = getCompletedCyclesCount(username)
        val currentCycle = cycleRepository.getCurrentCycleSync(username)
        val currentCycleNumber = currentCycle?.cycleNumber ?: 0
        return (currentCycleNumber - 1) / 3
    }

    fun getCurrentCycle(username: String): Flow<Cycle?> {
        return cycleRepository.getCurrentCycle(username)
    }

    fun getCycleHistory(username: String): Flow<List<CycleHistoryEntry>> {
        return cycleRepository.getCycleHistory(username)
    }

    suspend fun updateProgress(username: String, completedDaysCount: Int) {
        cycleRepository.updateDaysCompleted(username, completedDaysCount)

        if (completedDaysCount >= Cycle.DAYS_IN_CYCLE) {
            cycleRepository.markCycleCompleted(username, System.currentTimeMillis())
        } else {
            checkAndApplyMicrocycleProgression(username, completedDaysCount)
        }
    }
    
    suspend fun checkAndApplyMicrocycleProgression(username: String, completedDaysCount: Int): WeightProgressionUseCase.WeightProgressionSummary? {
        val currentCycle = cycleRepository.getCurrentCycleSync(username) ?: return null
        val newMicrocycleCount = completedDaysCount / Cycle.DAYS_IN_MICROCYCLE
        
        if (newMicrocycleCount > currentCycle.completedMicrocycles) {
            Log.d(TAG, "Microcycle completed: $newMicrocycleCount (was ${currentCycle.completedMicrocycles})")
            
            cycleRepository.updateCompletedMicrocycles(username, newMicrocycleCount)
            
            val progressionResult = weightProgressionUseCase.applyAdaptiveProgression(username)
            if (progressionResult.isSuccess) {
                val summary = progressionResult.getOrNull()
                Log.d(TAG, "Adaptive progression applied: increased=${summary?.totalIncreased}, decreased=${summary?.totalDecreased}")
                return summary
            }
        }
        return null
    }

    suspend fun checkAndStartNewCycleIfNeeded() {
        val profile = userRepository.getUserProfile().first() ?: return
        val completedDate = cycleRepository.getCompletedDate(profile.username)

        if (completedDate != null) {
            val now = System.currentTimeMillis()
            val nextDayAfterCompletion = completedDate + 24 * 60 * 60 * 1000

            if (now >= nextDayAfterCompletion) {
                cycleRepository.resetCycle(profile.username)
                exerciseCompletionRepository.clearCompletion(profile.username)
                val startDate = findNextMonday(now)
                cycleRepository.startNewCycle(profile.username, startDate)
            }
        }
    }

    private fun findNextMonday(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
