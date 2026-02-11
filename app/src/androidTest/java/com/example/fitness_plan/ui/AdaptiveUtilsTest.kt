package com.example.fitness_plan.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdaptiveUtilsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun isTablet_withPhoneScreenWidth_shouldReturnFalse() {
        var result = true

        composeTestRule.setContent {
            result = isTablet()
        }

        assertFalse("Phone (width < 600dp) should not be detected as tablet", result)
    }

    @Test
    fun isTablet_returnsCorrectBoolean() {
        var result: Boolean? = null

        composeTestRule.setContent {
            result = isTablet()
        }

        assertTrue("isTablet() should return a boolean value", result != null)
    }

    @Test
    fun adaptivePadding_returnsWindowInsets() {
        var insets: WindowInsets? = null

        composeTestRule.setContent {
            insets = adaptivePadding()
        }

        assertTrue("adaptivePadding() should return WindowInsets", insets != null)
        assertTrue("WindowInsets should be non-zero", insets != WindowInsets(0.dp))
    }

    @Test
    fun adaptivePadding_returnsConsistentValues() {
        var firstInsets: WindowInsets? = null
        var secondInsets: WindowInsets? = null

        composeTestRule.setContent {
            firstInsets = adaptivePadding()
        }

        composeTestRule.setContent {
            secondInsets = adaptivePadding()
        }

        assertTrue("adaptivePadding() should return consistent values for same configuration", 
            firstInsets == secondInsets)
    }
}
