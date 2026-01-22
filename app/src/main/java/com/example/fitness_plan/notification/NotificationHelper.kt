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
    const val NOTIFICATION_NEXT_MONTH_REMINDER = 1001
    const val NOTIFICATION_SCHEDULE_FILLED_REMINDER = 1002
    const val NOTIFICATION_WORKOUT_REMINDER = 1003

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

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(workoutChannel)
            notificationManager.createNotificationChannel(scheduleChannel)
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
}
