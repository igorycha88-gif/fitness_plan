package com.example.fitness_plan

import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HiltBasicTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        // Initialize dependencies if needed
        hiltRule.inject()
    }

    @Test
    fun appContextIsNotNull() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertNotNull(ctx)
    }
}
