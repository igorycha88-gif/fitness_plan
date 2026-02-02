package com.example.fitness_plan.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.fitness_plan.notification.NotificationHelper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.google.common.truth.Truth.assertThat

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class WorkoutReminderWorkerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun `worker should return success when showing workout reminder`() = runTest {
        mockkObject(NotificationHelper)
        every { NotificationHelper.showWorkoutReminder(any(), any(), any()) } just Runs

        val inputData = androidx.work.Data.Builder()
            .putString(WorkoutReminderWorker.KEY_WORKOUT_NAME, "Leg Day")
            .putLong(WorkoutReminderWorker.KEY_WORKOUT_DATE, System.currentTimeMillis())
            .putInt(WorkoutReminderWorker.KEY_DAY_INDEX, 0)
            .build()

        val worker = TestListenableWorkerBuilder<WorkoutReminderWorker>(context)
            .setInputData(inputData)
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        verify { NotificationHelper.showWorkoutReminder(any(), any(), any()) }
    }

    @Test
    fun `worker should return retry on exception`() = runTest {
        mockkObject(NotificationHelper)
        every { NotificationHelper.showWorkoutReminder(any(), any(), any()) } throws RuntimeException("Test error")

        val inputData = androidx.work.Data.Builder()
            .putString(WorkoutReminderWorker.KEY_WORKOUT_NAME, "Leg Day")
            .putLong(WorkoutReminderWorker.KEY_WORKOUT_DATE, System.currentTimeMillis())
            .build()

        val worker = TestListenableWorkerBuilder<WorkoutReminderWorker>(context)
            .setInputData(inputData)
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
