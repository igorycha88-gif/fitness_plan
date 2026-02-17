package com.example.fitness_plan.presentation.viewmodel

import com.example.fitness_plan.domain.model.*
import com.example.fitness_plan.domain.repository.BodyParametersRepository
import com.example.fitness_plan.domain.usecase.BodyParametersUseCase
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class BodyParametersStatsViewModelTest {

    private lateinit var viewModel: BodyParametersStatsViewModel
    private lateinit var mockUseCase: BodyParametersUseCase
    private lateinit var mockCredentialsRepository: ICredentialsRepository
    private lateinit var mockBodyParametersRepository: BodyParametersRepository

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val mutableMeasurementsFlow = MutableStateFlow<List<BodyParameter>>(emptyList())

    private val testMeasurement1 = BodyParameter(
        parameterType = BodyParameterType.WEIGHT,
        value = 85.5,
        unit = "кг",
        date = System.currentTimeMillis(),
        calculationMethod = CalculationMethod.MANUAL
    )

    private val testMeasurement2 = BodyParameter(
        parameterType = BodyParameterType.WAIST,
        value = 85.0,
        unit = "см",
        date = System.currentTimeMillis() - 24 * 60 * 60 * 1000L,
        calculationMethod = CalculationMethod.MANUAL
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockUseCase = mockk(relaxed = true)
        mockCredentialsRepository = mockk(relaxed = true)
        mockBodyParametersRepository = mockk(relaxed = true)

        coEvery { mockCredentialsRepository.getUsername() } returns "testuser"
        every { mockUseCase.getMeasurements("testuser") } returns mutableMeasurementsFlow

        viewModel = BodyParametersStatsViewModel(
            mockUseCase,
            mockCredentialsRepository
        )

        testScope.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state should have correct default values`() = runTest {
        assertThat(viewModel.currentUsername.value).isEqualTo("testuser")
        assertThat(viewModel.selectedTypes.value).isEmpty()
        assertThat(viewModel.selectedTimeFilter.value).isEqualTo(TimeFilter.ALL)
        assertThat(viewModel.viewMode.value).isEqualTo(ViewMode.CHART)
        assertThat(viewModel.showParameterSelector.value).isFalse()
    }

    @Test
    fun `setSelectedTypes should update selected types`() {
        val types = setOf(BodyParameterType.WEIGHT, BodyParameterType.WAIST)

        viewModel.setSelectedTypes(types)

        assertThat(viewModel.selectedTypes.value).isEqualTo(types)
    }

    @Test
    fun `setTimeFilter should update selected filter`() {
        viewModel.setTimeFilter(TimeFilter.WEEK)

        assertThat(viewModel.selectedTimeFilter.value).isEqualTo(TimeFilter.WEEK)
    }

    @Test
    fun `setViewMode should update view mode`() {
        viewModel.setViewMode(ViewMode.TABLE)

        assertThat(viewModel.viewMode.value).isEqualTo(ViewMode.TABLE)
    }

    @Test
    fun `setShowParameterSelector should update dialog state`() {
        viewModel.setShowParameterSelector(true)

        assertThat(viewModel.showParameterSelector.value).isTrue()

        viewModel.setShowParameterSelector(false)

        assertThat(viewModel.showParameterSelector.value).isFalse()
    }

    @Test
    fun `getFilteredMeasurements should return filtered measurements when types and time filter are set`() = runTest {
        val types = setOf(BodyParameterType.WEIGHT)
        mutableMeasurementsFlow.value = listOf(testMeasurement1, testMeasurement2)

        viewModel.setSelectedTypes(types)
        viewModel.setTimeFilter(TimeFilter.ALL)

        val filtered = viewModel.getFilteredMeasurements()

        assertThat(filtered).hasSize(1)
        assertThat(filtered[0].parameterType).isEqualTo(BodyParameterType.WEIGHT)
    }

    @Test
    fun `getFilteredMeasurements should return all measurements when types is empty`() = runTest {
        mutableMeasurementsFlow.value = listOf(testMeasurement1, testMeasurement2)
        testScheduler.runCurrent()
        testScope.advanceUntilIdle()

        viewModel.setTimeFilter(TimeFilter.ALL)

        val filtered = viewModel.getFilteredMeasurements()

        assertThat(filtered).hasSize(2)
    }

    @Test
    fun `getFilteredMeasurements should filter by time`() = runTest {
        val types = setOf(BodyParameterType.WEIGHT)
        mutableMeasurementsFlow.value = listOf(testMeasurement1)
        testScheduler.runCurrent()
        testScope.advanceUntilIdle()

        viewModel.setSelectedTypes(types)
        viewModel.setTimeFilter(TimeFilter.WEEK)

        val filtered = viewModel.getFilteredMeasurements()

        assertThat(filtered).hasSize(1)
    }

    @Test
    fun `getFilteredMeasurements should return empty when no measurements match criteria`() = runTest {
        val types = setOf(BodyParameterType.CHEST)
        mutableMeasurementsFlow.value = listOf(testMeasurement1, testMeasurement2)
        testScheduler.runCurrent()
        testScope.advanceUntilIdle()

        viewModel.setSelectedTypes(types)

        val filtered = viewModel.getFilteredMeasurements()

        assertThat(filtered).isEmpty()
    }

    @Test
    fun `availableForChart should be empty when no measurements exist`() = runTest {
        mutableMeasurementsFlow.value = emptyList()
        testScheduler.runCurrent()
        testScope.advanceUntilIdle()

        val available = viewModel.getAvailableParameters()

        assertThat(available).isEmpty()
    }

    @Test
    fun `availableForChart should contain parameters with data`() = runTest {
        mutableMeasurementsFlow.value = listOf(testMeasurement1, testMeasurement2)

        testScheduler.advanceUntilIdle()

        val available = viewModel.getAvailableParameters()

        assertThat(available).hasSize(2)
        assertThat(available.any { it.type == BodyParameterType.WEIGHT }).isTrue()
        assertThat(available.any { it.type == BodyParameterType.WAIST }).isTrue()
    }

    @Test
    fun `filteredChartSeries should be empty when no types selected`() = runTest {
        mutableMeasurementsFlow.value = listOf(testMeasurement1, testMeasurement2)

        testScheduler.advanceUntilIdle()

        val seriesList = viewModel.filteredChartSeries.value

        assertThat(seriesList).isEmpty()
    }

    @Test
    fun `filteredChartSeries should contain series when types selected`() = runTest {
        val types = setOf(BodyParameterType.WEIGHT, BodyParameterType.WAIST)
        mutableMeasurementsFlow.value = listOf(testMeasurement1, testMeasurement2)

        testScheduler.advanceUntilIdle()

        viewModel.setSelectedTypes(types)

        testScheduler.advanceUntilIdle()

        val seriesList = viewModel.filteredChartSeries.value

        assertThat(seriesList).hasSize(2)
        assertThat(seriesList[0].parameterType).isEqualTo(BodyParameterType.WEIGHT)
        assertThat(seriesList[0].data).hasSize(1)
        assertThat(seriesList[0].isVisible).isTrue()
    }
}
