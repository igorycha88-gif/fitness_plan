package com.example.fitness_plan.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitness_plan.data.NotificationRepository
import com.example.fitness_plan.data.WorkoutScheduleRepository
import com.example.fitness_plan.domain.usecase.WorkoutUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class WorkoutReminderScheduler @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationRepository: NotificationRepository,
    private val workoutScheduleRepository: WorkoutScheduleRepository,
    private val workoutUseCase: WorkoutUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val reminderEnabled = notificationRepository.workoutReminderEnabled.first()
            if (!reminderEnabled) {
                return Result.success()
            }

            val reminderHours = notificationRepository.workoutReminderHours.first()
            val currentUsername = getCurrentUsername()

            if (currentUsername.isEmpty()) {
                return Result.success()
            }

            val scheduleDates = workoutScheduleRepository.getWorkoutSchedule(currentUsername).first()
            val workoutPlan = workoutUseCase.getWorkoutPlan(currentUsername)

            scheduleWorkoutReminders(scheduleDates, workoutPlan, reminderHours)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun scheduleWorkoutReminders(
        dates: List<Long>,
        workoutPlan: com.example.fitness_plan.domain.model.WorkoutPlan?,
        reminderHours: Int
    ) {
        val currentTime = System.currentTimeMillis()

        dates.forEachIndexed { index, date ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 1)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val reminderTime = calendar.timeInMillis - (reminderHours * 60 * 60 * 1000L)

            if (reminderTime > currentTime) {
                val dayIndex = index % (workoutPlan?.days?.size ?: 1)
                val workoutDay = workoutPlan?.days?.getOrNull(dayIndex)
                val workoutName = workoutDay?.dayName ?: workoutPlan?.name ?: "Тренировка"

                val workRequest = androidx.work.OneTimeWorkRequestBuilder<WorkoutReminderWorker>()
                    .setInitialDelay(reminderTime - currentTime, TimeUnit.MILLISECONDS)
                    .setInputData(
                        androidx.work.Data.Builder()
                            .putString(WorkoutReminderWorker.KEY_WORKOUT_NAME, workoutName)
                            .putLong(WorkoutReminderWorker.KEY_WORKOUT_DATE, date)
                            .putInt(WorkoutReminderWorker.KEY_DAY_INDEX, dayIndex)
                            .build()
                    )
                    .addTag("workout_reminder_$date")
                    .build()

                androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }

    private fun getCurrentUsername(): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("current_username", "") ?: ""
    }

    companion object {
        const val WORK_NAME = "workout_reminder_scheduler"

        fun scheduleImmediate(context: Context) {
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<WorkoutReminderScheduler>()
                .build()
            androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
