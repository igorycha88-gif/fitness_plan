package com.example.fitness_plan.domain.usecase

import android.util.Log
import com.example.fitness_plan.domain.model.Cycle
import com.example.fitness_plan.domain.model.CycleHistoryEntry
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
    private val exerciseCompletionRepository: ExerciseCompletionRepository
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

        Log.d(TAG, "completedDate=$completedDate, currentCycle=${currentCycle?.cycleNumber}")

        val cycle = when {
            completedDate != null -> {
                Log.d(TAG, "Completing old cycle and starting new one")
                cycleRepository.resetCycle(username)
                exerciseCompletionRepository.clearCompletion(username)
                cycleRepository.startNewCycle(username, now)
            }
            currentCycle == null -> {
                Log.d(TAG, "Starting new cycle")
                cycleRepository.startNewCycle(username, now)
            }
            else -> {
                Log.d(TAG, "Using existing cycle ${currentCycle.cycleNumber}")
                currentCycle
            }
        }

        val plan = loadWorkoutPlan(profile, cycle)
        Log.d(TAG, "Workout plan created: ${plan.name}, days=${plan.days.size}")

        val history = cycleRepository.getCycleHistory(username).first()
        Log.d(TAG, "History size: ${history.size}")

        return CycleState(cycle, plan, history)
    }

    private suspend fun loadWorkoutPlan(profile: UserProfile, cycle: Cycle?): WorkoutPlan {
        val basePlan = workoutRepository.getWorkoutPlanForUser(profile)
        val plan30 = workoutRepository.get30DayWorkoutPlan(basePlan)
        val dates = workoutRepository.generate30DayDates(
            cycle?.startDate ?: System.currentTimeMillis(),
            profile.frequency
        )
        return workoutRepository.getWorkoutPlanWithDates(plan30, dates)
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
        }
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
