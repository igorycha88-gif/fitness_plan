package com.example.fitness_plan.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.fitness_plan.MainActivity
import com.example.fitness_plan.R

object NotificationHelper {
    const val CHANNEL_ID_WORKOUT = "workout_reminders"
    const val CHANNEL_ID_SCHEDULE = "schedule_reminders"
    const val CHANNEL_ID_WEIGHT_PROGRESSION = "weight_progression"
    const val NOTIFICATION_NEXT_MONTH_REMINDER = 1001
    const val NOTIFICATION_SCHEDULE_FILLED_REMINDER = 1002
    const val NOTIFICATION_WORKOUT_REMINDER = 1003
    const val NOTIFICATION_WEIGHT_PROGRESSION = 1004

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val workoutChannel = NotificationChannel(
                CHANNEL_ID_WORKOUT,
                "Тренировки",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминания о тренировках"
            }

            val scheduleChannel = NotificationChannel(
                CHANNEL_ID_SCHEDULE,
                "Расписание",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Напоминания о заполнении расписания"
            }

            val weightProgressionChannel = NotificationChannel(
                CHANNEL_ID_WEIGHT_PROGRESSION,
                "Прогрессия весов",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления об адаптивном изменении весов"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(workoutChannel)
            notificationManager.createNotificationChannel(scheduleChannel)
            notificationManager.createNotificationChannel(weightProgressionChannel)
        }
    }

    @SuppressLint("MissingPermission", "NotificationPermission")
    fun showNextMonthReminder(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SCHEDULE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Заполните расписание")
            .setContentText("На ближайший месяц необходимо выбрать тренировки")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_NEXT_MONTH_REMINDER, notification)
    }

    @SuppressLint("MissingPermission", "NotificationPermission")
    fun showScheduleFilledReminder(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SCHEDULE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Вы забыли заполнить даты тренировок")
            .setContentText("Заполните расписание на следующий месяц")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_SCHEDULE_FILLED_REMINDER, notification)
    }

    @SuppressLint("MissingPermission", "NotificationPermission")
    fun showWorkoutReminder(context: Context, workoutName: String, date: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WORKOUT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Тренировка сегодня")
            .setContentText("$workoutName - $date")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_WORKOUT_REMINDER, notification)
    }

    @SuppressLint("MissingPermission", "NotificationPermission")
    fun showWeightProgressionNotification(
        context: Context,
        totalIncreased: Int,
        totalDecreased: Int,
        totalUnchanged: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 3, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "Прогрессия весов обновлена"
        val message = when {
            totalIncreased > 0 && totalDecreased > 0 -> 
                "Веса адаптированы: +$totalIncreased увеличено, -$totalDecreased уменьшено"
            totalIncreased > 0 -> 
                "Увеличено $totalIncreased упражнени${if (totalIncreased == 1) "е" else "й"} на основе ваших результатов"
            totalDecreased > 0 -> 
                "Уменьшено $totalDecreased упражнени${if (totalDecreased == 1) "е" else "й"} для оптимизации прогресса"
            else -> 
                "Веса адаптированы к вашим результатам"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WEIGHT_PROGRESSION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$message\n$totalUnchanged упражнени${if (totalUnchanged == 1) "е" else "й"} без изменений"
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_WEIGHT_PROGRESSION, notification)
    }
}
