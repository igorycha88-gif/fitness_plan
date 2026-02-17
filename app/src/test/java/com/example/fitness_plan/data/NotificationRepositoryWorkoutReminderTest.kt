package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.google.common.truth.Truth.assertThat

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class NotificationRepositoryWorkoutReminderTest {

    private lateinit var notificationRepository: NotificationRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        notificationRepository = NotificationRepository(context)
    }

    @After
    fun tearDown() = runTest {
        context.notificationDataStore.edit { it.clear() }
    }

    @Test
    fun `workoutReminderEnabled should be false by default`() = runTest {
        val enabled = notificationRepository.workoutReminderEnabled.first()
        assertThat(enabled).isFalse()
    }

    @Test
    fun `setWorkoutReminderEnabled should update the value`() = runTest {
        notificationRepository.setWorkoutReminderEnabled(true)
        val enabled = notificationRepository.workoutReminderEnabled.first()
        assertThat(enabled).isTrue()

        notificationRepository.setWorkoutReminderEnabled(false)
        val disabled = notificationRepository.workoutReminderEnabled.first()
        assertThat(disabled).isFalse()
    }

    @Test
    fun `workoutReminderHours should be 8 by default`() = runTest {
        val hours = notificationRepository.workoutReminderHours.first()
        assertThat(hours).isEqualTo(8)
    }

    @Test
    fun `setWorkoutReminderHours should update the value`() = runTest {
        notificationRepository.setWorkoutReminderHours(4)
        val hours = notificationRepository.workoutReminderHours.first()
        assertThat(hours).isEqualTo(4)

        notificationRepository.setWorkoutReminderHours(12)
        val updatedHours = notificationRepository.workoutReminderHours.first()
        assertThat(updatedHours).isEqualTo(12)
    }

    @Test
    fun `notificationsEnabled should be true by default`() = runTest {
        val enabled = notificationRepository.notificationsEnabled.first()
        assertThat(enabled).isTrue()
    }

    @Test
    fun `setNotificationsEnabled should update the value`() = runTest {
        notificationRepository.setNotificationsEnabled(false)
        val disabled = notificationRepository.notificationsEnabled.first()
        assertThat(disabled).isFalse()

        notificationRepository.setNotificationsEnabled(true)
        val enabled = notificationRepository.notificationsEnabled.first()
        assertThat(enabled).isTrue()
    }
}

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "test_notifications")
