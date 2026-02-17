package com.example.fitness_plan.test

import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class HiltGreetingServiceTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var greetingService: GreetingService

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun greetingServiceInjectionWorks() {
        // DI should provide an instance
        assertNotNull(greetingService)
        // And the binding should be the test implementation
        assertEquals("Hello from Test", greetingService.greet())
    }
}
