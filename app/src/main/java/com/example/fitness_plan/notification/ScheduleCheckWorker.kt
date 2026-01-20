package com.example.fitness_plan.notification

import android.content.Context
import androidx.work.*
import com.example.fitness_plan.data.NotificationRepository
import com.example.fitness_plan.data.WorkoutScheduleRepository
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

class ScheduleCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notificationRepository by lazy { NotificationRepository(context) }
    private val workoutScheduleRepository by lazy { WorkoutScheduleRepository(context) }

    override suspend fun doWork(): Result {
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        try {
            val username = getCurrentUsername()
            if (username.isNotEmpty()) {
                val scheduleDates = workoutScheduleRepository.getWorkoutSchedule(username).first()

                // Check if we need to send "Next Month Reminder"
                val needsScheduleUpdate = checkIfNeedsScheduleUpdate(scheduleDates, currentTime)

                if (needsScheduleUpdate) {
                    val shouldSendReminder = shouldSendReminder(
                        notificationRepository.nextMonthReminderSent.first(),
                        currentTime
                    )

                    if (shouldSendReminder) {
                        NotificationHelper.showNextMonthReminder(context)
                        notificationRepository.setNextMonthReminderSent(currentTime)
                    }
                }

                // Check if next day workout is scheduled for tomorrow but not filled
                val nextDayHasWorkout = scheduleDates.any { date ->
                    val tomorrow = Calendar.getInstance().apply {
                        timeInMillis = currentTime + oneDayMillis
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val workoutDay = Calendar.getInstance().apply { timeInMillis = date }
                    workoutDay.set(Calendar.HOUR_OF_DAY, 0)
                    workoutDay.set(Calendar.MINUTE, 0)
                    workoutDay.set(Calendar.SECOND, 0)
                    workoutDay.set(Calendar.MILLISECOND, 0)

                    tomorrow.timeInMillis == workoutDay.timeInMillis
                }

                if (!nextDayHasWorkout) {
                    val shouldSendFilledReminder = shouldSendReminder(
                        notificationRepository.scheduleFilledReminderSent.first(),
                        currentTime
                    )

                    if (shouldSendFilledReminder) {
                        NotificationHelper.showScheduleFilledReminder(context)
                        notificationRepository.setScheduleFilledReminderSent(currentTime)
                    }
                }

                // Update last check timestamp
                notificationRepository.setLastScheduleCheck(currentTime)
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun getCurrentUsername(): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("current_username", "") ?: ""
    }

    private fun checkIfNeedsScheduleUpdate(scheduleDates: List<Long>, currentTime: Long): Boolean {
        if (scheduleDates.isEmpty()) return true

        val hasUpcomingDates = scheduleDates.any { date ->
            val daysUntil = (date - currentTime) / (24 * 60 * 60 * 1000)
            daysUntil in 0..14
        }

        return !hasUpcomingDates
    }

    private fun shouldSendReminder(lastSent: Long, currentTime: Long): Boolean {
        val minimumInterval = 24 * 60 * 60 * 1000L // 24 hours
        return currentTime - lastSent >= minimumInterval
    }

    companion object {
        const val WORK_NAME = "schedule_check_worker"

        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ScheduleCheckWorker>(
                6, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun runImmediateCheck(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<ScheduleCheckWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
