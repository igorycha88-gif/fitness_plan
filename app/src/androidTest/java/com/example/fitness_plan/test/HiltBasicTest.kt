package com.example.fitness_plan.test

import android.app.Application
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class HiltBasicTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var app: Application

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun appIsInjected() {
        assertNotNull(app)
    }
}
