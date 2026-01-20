package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notifications")

@Singleton
class NotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LAST_SCHEDULE_CHECK = longPreferencesKey("last_schedule_check")
        val NEXT_MONTH_REMINDER_SENT = longPreferencesKey("next_month_reminder_sent")
        val SCHEDULE_FILLED_REMINDER_SENT = longPreferencesKey("schedule_filled_reminder_sent")
        val LAST_WORKOUT_DATE = longPreferencesKey("last_workout_date")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val notificationsEnabled: Flow<Boolean> = context.notificationDataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    val lastScheduleCheck: Flow<Long> = context.notificationDataStore.data.map { prefs ->
        prefs[Keys.LAST_SCHEDULE_CHECK] ?: 0L
    }

    suspend fun setLastScheduleCheck(timestamp: Long) {
        context.notificationDataStore.edit { prefs ->
            prefs[Keys.LAST_SCHEDULE_CHECK] = timestamp
        }
    }

    val nextMonthReminderSent: Flow<Long> = context.notificationDataStore.data.map { prefs ->
        prefs[Keys.NEXT_MONTH_REMINDER_SENT] ?: 0L
    }

    suspend fun setNextMonthReminderSent(timestamp: Long) {
        context.notificationDataStore.edit { prefs ->
            prefs[Keys.NEXT_MONTH_REMINDER_SENT] = timestamp
        }
    }

    val scheduleFilledReminderSent: Flow<Long> = context.notificationDataStore.data.map { prefs ->
        prefs[Keys.SCHEDULE_FILLED_REMINDER_SENT] ?: 0L
    }

    suspend fun setScheduleFilledReminderSent(timestamp: Long) {
        context.notificationDataStore.edit { prefs ->
            prefs[Keys.SCHEDULE_FILLED_REMINDER_SENT] = timestamp
        }
    }

    val lastWorkoutDate: Flow<Long> = context.notificationDataStore.data.map { prefs ->
        prefs[Keys.LAST_WORKOUT_DATE] ?: 0L
    }

    suspend fun setLastWorkoutDate(timestamp: Long) {
        context.notificationDataStore.edit { prefs ->
            prefs[Keys.LAST_WORKOUT_DATE] = timestamp
        }
    }

    suspend fun shouldSendNextMonthReminder(currentTime: Long, reminderIntervalHours: Int = 24): Boolean {
        var lastSent = 0L
        context.notificationDataStore.data.collect { prefs ->
            lastSent = prefs[Keys.NEXT_MONTH_REMINDER_SENT] ?: 0L
        }
        return currentTime - lastSent > reminderIntervalHours * 60 * 60 * 1000L
    }

    suspend fun shouldSendScheduleFilledReminder(currentTime: Long, reminderIntervalHours: Int = 24): Boolean {
        var lastSent = 0L
        context.notificationDataStore.data.collect { prefs ->
            lastSent = prefs[Keys.SCHEDULE_FILLED_REMINDER_SENT] ?: 0L
        }
        return currentTime - lastSent > reminderIntervalHours * 60 * 60 * 1000L
    }
}
