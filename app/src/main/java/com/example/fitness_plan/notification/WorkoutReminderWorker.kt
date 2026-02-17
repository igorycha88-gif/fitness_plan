package com.example.fitness_plan.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitness_plan.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@HiltWorker
class WorkoutReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val workoutName = inputData.getString(KEY_WORKOUT_NAME) ?: "Тренировка"
            val workoutDate = inputData.getLong(KEY_WORKOUT_DATE, System.currentTimeMillis())

            val formattedDate = formatDate(workoutDate)
            
            NotificationHelper.showWorkoutReminder(
                context = context,
                workoutName = workoutName,
                date = formattedDate
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "${dateFormat.format(calendar.time)}, ${timeFormat.format(calendar.time)}"
    }

    companion object {
        const val KEY_WORKOUT_NAME = "workout_name"
        const val KEY_WORKOUT_DATE = "workout_date"
        const val KEY_DAY_INDEX = "day_index"
    }
}
