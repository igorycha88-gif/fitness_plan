package com.example.fitness_plan.ui

import androidx.compose.ui.platform.ConfigurationAmbient
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@RunWith(AndroidJUnit4::class)
class AdaptiveUtilsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun isTablet_withSmallWidth_shouldReturnFalse() {
        composeTestRule.setContent {
            var isTablet = false
            isTablet = isTablet()
            assertFalse(isTablet)
        }
    }

    @Test
    fun adaptivePadding_shouldReturnCorrectPadding() {
        composeTestRule.setContent {
            val insets = adaptivePadding()
            val isTablet = isTablet()

            if (isTablet) {
                assertEquals(48.dp, insets.left)
            } else {
                assertEquals(24.dp, insets.left)
            }
        }
    }
}
