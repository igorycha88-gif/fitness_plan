package com.example.fitness_plan.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitness_plan.presentation.viewmodel.TimeFilter
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WeightFilterDropdownTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var selectedFilter = TimeFilter.MONTH
    private var filterSelectedCalled = false
    private var lastSelectedFilter: TimeFilter? = null

    @Before
    fun setup() {
        selectedFilter = TimeFilter.MONTH
        filterSelectedCalled = false
        lastSelectedFilter = null
    }

    @Test
    fun weightFilterDropdown_shouldDisplayCurrentFilter() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    WeightFilterDropdown(
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            filterSelectedCalled = true
                            lastSelectedFilter = it
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Период").assertIsDisplayed()
        composeTestRule.onNodeWithText("Месяц").assertIsDisplayed()
    }

    @Test
    fun weightFilterDropdown_shouldDisplayWeekAsDefault() {
        selectedFilter = TimeFilter.WEEK

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    WeightFilterDropdown(
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            filterSelectedCalled = true
                            lastSelectedFilter = it
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Неделя").assertIsDisplayed()
    }

    @Test
    fun weightFilterDropdown_shouldOpenMenuOnFieldClick() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    WeightFilterDropdown(
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            filterSelectedCalled = true
                            lastSelectedFilter = it
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Месяц").performClick()

        composeTestRule.onNodeWithText("Неделя").assertIsDisplayed()
        composeTestRule.onNodeWithText("Месяц").assertIsDisplayed()
        composeTestRule.onNodeWithText("Год").assertIsDisplayed()
        composeTestRule.onNodeWithText("Всё время").assertIsDisplayed()
    }

    @Test
    fun weightFilterDropdown_shouldSelectWeekFilter() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    WeightFilterDropdown(
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            filterSelectedCalled = true
                            lastSelectedFilter = it
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Месяц").performClick()
        composeTestRule.onNodeWithText("Неделя").performClick()

        assertThat(filterSelectedCalled).isTrue()
        assertThat(lastSelectedFilter).isEqualTo(TimeFilter.WEEK)
    }

    @Test
    fun weightFilterDropdown_shouldSelectYearFilter() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    WeightFilterDropdown(
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            filterSelectedCalled = true
                            lastSelectedFilter = it
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Месяц").performClick()
        composeTestRule.onNodeWithText("Год").performClick()

        assertThat(filterSelectedCalled).isTrue()
        assertThat(lastSelectedFilter).isEqualTo(TimeFilter.YEAR)
    }

    @Test
    fun weightFilterDropdown_shouldSelectAllFilter() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    WeightFilterDropdown(
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            filterSelectedCalled = true
                            lastSelectedFilter = it
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Месяц").performClick()
        composeTestRule.onNodeWithText("Всё время").performClick()

        assertThat(filterSelectedCalled).isTrue()
        assertThat(lastSelectedFilter).isEqualTo(TimeFilter.ALL)
    }

    @Test
    fun weightFilterDropdown_shouldCloseMenuAfterSelection() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    WeightFilterDropdown(
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            filterSelectedCalled = true
                            lastSelectedFilter = it
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Месяц").performClick()
        composeTestRule.onNodeWithText("Неделя").performClick()

        composeTestRule.onNodeWithText("Неделя").assertDoesNotExist()
    }

    @Test
    fun weightFilterDropdown_shouldDisplayAllOptions() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    WeightFilterDropdown(
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            filterSelectedCalled = true
                            lastSelectedFilter = it
                        }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Месяц").performClick()

        composeTestRule.onNodeWithText("Неделя").assertIsDisplayed()
        composeTestRule.onNodeWithText("Месяц").assertIsDisplayed()
        composeTestRule.onNodeWithText("Год").assertIsDisplayed()
        composeTestRule.onNodeWithText("Всё время").assertIsDisplayed()
    }
}
