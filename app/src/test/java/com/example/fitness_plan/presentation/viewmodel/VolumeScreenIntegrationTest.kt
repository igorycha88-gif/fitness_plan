package com.example.fitness_plan.presentation.viewmodel

import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.repository.WeightRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class VolumeScreenIntegrationTest {

    private lateinit var viewModel: StatisticsViewModel
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockCredentialsRepository: ICredentialsRepository
    private lateinit var mockCycleRepository: CycleRepository
    private lateinit var mockWeightRepository: WeightRepository
    private lateinit var mockExerciseStatsRepository: ExerciseStatsRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockUserRepository = mockk(relaxed = true)
        mockCredentialsRepository = mockk(relaxed = true)
        mockCycleRepository = mockk(relaxed = true)
        mockWeightRepository = mockk(relaxed = true)
        mockExerciseStatsRepository = mockk(relaxed = true)

        coEvery { mockCredentialsRepository.getUsername() } returns "testuser"
        every { mockUserRepository.getUserProfile() } returns flowOf(null)

        viewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `volume filters should be available and selectable`() = runTest {
        VolumeTimeFilter.values().forEach { filter ->
            viewModel.setVolumeFilter(filter)
            assertThat(viewModel.selectedVolumeFilter.first()).isEqualTo(filter)
        }
    }

    @Test
    fun `selectedExercise should be nullable and resettable`() = runTest {
        viewModel.setSelectedExercise("Squats")
        assertThat(viewModel.selectedExercise.first()).isEqualTo("Squats")

        viewModel.setSelectedExercise(null)
        assertThat(viewModel.selectedExercise.first()).isNull()
    }

    @Test
    fun `getFilteredVolumeData should return empty list when no data`() = runTest {
        every { mockExerciseStatsRepository.getExerciseStats(any()) } returns flowOf(emptyList())

        advanceUntilIdle()

        val filteredData = viewModel.getFilteredVolumeData()

        assertThat(filteredData).isEmpty()
    }
}
