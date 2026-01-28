package com.example.fitness_plan.presentation.viewmodel

import com.example.fitness_plan.domain.model.ExerciseStats
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.model.WeightEntry
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.ExerciseStatsRepository
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.repository.WeightRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class StatisticsViewModelTest {

    private lateinit var viewModel: StatisticsViewModel
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockCredentialsRepository: ICredentialsRepository
    private lateinit var mockCycleRepository: CycleRepository
    private lateinit var mockWeightRepository: WeightRepository
    private lateinit var mockExerciseStatsRepository: ExerciseStatsRepository

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val mutableStatsFlow = MutableStateFlow<List<ExerciseStats>>(emptyList())

    private val testUser = UserProfile(
        username = "testuser",
        goal = "Похудение",
        level = "Новичок",
        frequency = "3 раза в неделю",
        weight = 90.0,
        height = 180.0,
        gender = "Мужской"
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockUserRepository = mockk(relaxed = true)
        mockCredentialsRepository = mockk(relaxed = true)
        mockCycleRepository = mockk(relaxed = true)
        mockWeightRepository = mockk(relaxed = true)
        mockExerciseStatsRepository = mockk(relaxed = true)

        coEvery { mockCredentialsRepository.getUsername() } returns "testuser"
        every { mockUserRepository.getUserProfile() } returns flowOf(testUser)
        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(emptyList())
        every { mockExerciseStatsRepository.getExerciseStats(any()) } returns mutableStatsFlow
        coEvery { mockCycleRepository.getCurrentCycle(any()) } returns flowOf(null)
        coEvery { mockCycleRepository.getCycleHistory(any()) } returns flowOf(emptyList())

        viewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state should have correct default values`() = runTest {
        assertThat(viewModel.currentUsername.value).isEqualTo("testuser")
        assertThat(viewModel.selectedTimeFilter.value).isEqualTo(TimeFilter.MONTH)
        assertThat(viewModel.showWeightDialog.value).isFalse()
    }

    @Test
    fun `setTimeFilter should update selected filter`() {
        viewModel.setTimeFilter(TimeFilter.WEEK)

        assertThat(viewModel.selectedTimeFilter.value).isEqualTo(TimeFilter.WEEK)
    }

    @Test
    fun `setShowWeightDialog should update dialog state`() {
        viewModel.setShowWeightDialog(true)

        assertThat(viewModel.showWeightDialog.value).isTrue()

        viewModel.setShowWeightDialog(false)

        assertThat(viewModel.showWeightDialog.value).isFalse()
    }

    @Test
    fun `saveWeight should delegate to repository`() = runTest {
        coEvery { mockWeightRepository.saveWeight(any(), any(), any()) } just runs

        viewModel.saveWeight(88.5)

        coVerify {
            mockWeightRepository.saveWeight(
                username = "testuser",
                weight = 88.5,
                date = any()
            )
        }
    }

    @Test
    fun `getFilteredWeightHistory with MONTH filter should return entries within 30 days`() = runTest {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        val entries = listOf(
            WeightEntry(now - 5 * dayInMillis, 90.0),
            WeightEntry(now - 15 * dayInMillis, 89.5),
            WeightEntry(now - 25 * dayInMillis, 89.0),
            WeightEntry(now - 35 * dayInMillis, 88.5),
            WeightEntry(now - 45 * dayInMillis, 88.0)
        )

        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(entries)

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        testViewModel.setTimeFilter(TimeFilter.MONTH)

        val filtered = testViewModel.getFilteredWeightHistory()

        assertThat(filtered).hasSize(3)
        assertThat(filtered.map { it.weight }).containsExactly(90.0, 89.5, 89.0)
    }

    @Test
    fun `getFilteredWeightHistory with ALL filter should return all entries`() = runTest {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        val entries = listOf(
            WeightEntry(now - 5 * dayInMillis, 90.0),
            WeightEntry(now - 15 * dayInMillis, 89.5),
            WeightEntry(now - 25 * dayInMillis, 89.0),
            WeightEntry(now - 35 * dayInMillis, 88.5),
            WeightEntry(now - 45 * dayInMillis, 88.0)
        )

        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(entries)

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        testViewModel.setTimeFilter(TimeFilter.ALL)

        val filtered = testViewModel.getFilteredWeightHistory()

        assertThat(filtered).hasSize(5)
        assertThat(filtered.map { it.weight }).containsExactly(90.0, 89.5, 89.0, 88.5, 88.0)
    }

    @Test
    fun `getCurrentWeight should return latest weight entry`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            WeightEntry(now - 15 * 24 * 60 * 60 * 1000L, 89.5),
            WeightEntry(now - 5 * 24 * 60 * 60 * 1000L, 90.0)
        )

        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(entries)

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        testViewModel.setTimeFilter(TimeFilter.ALL)

        val currentWeight = testViewModel.getCurrentWeight()

        assertThat(currentWeight).isEqualTo(90.0)
    }

    @Test
    fun `getStartWeight should return first weight entry`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            WeightEntry(now - 15 * 24 * 60 * 60 * 1000L, 89.5),
            WeightEntry(now - 5 * 24 * 60 * 60 * 1000L, 90.0)
        )

        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(entries)

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        testViewModel.setTimeFilter(TimeFilter.ALL)

        val startWeight = testViewModel.getStartWeight()

        assertThat(startWeight).isEqualTo(89.5)
    }

    @Test
    fun `getCurrentWeight should return 0 when history is empty`() = runTest {
        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(emptyList())

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        val currentWeight = testViewModel.getCurrentWeight()

        assertThat(currentWeight).isEqualTo(0.0)
    }

    @Test
    fun `getDaysFromStart should calculate days correctly`() = runTest {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        val entries = listOf(
            WeightEntry(now - 10 * dayInMillis, 90.0),
            WeightEntry(now - 5 * dayInMillis, 89.5)
        )

        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(entries)

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        testViewModel.setTimeFilter(TimeFilter.ALL)

        val days = testViewModel.getDaysFromStart()

        assertThat(days).isAtLeast(10)
    }

    @Test
    fun `getDaysFromStart should return 0 when history is empty`() = runTest {
        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(emptyList())

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        val days = testViewModel.getDaysFromStart()

        assertThat(days).isEqualTo(0)
    }

    @Test
    fun `getWeightChange should calculate correct change`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            WeightEntry(now - 15 * 24 * 60 * 60 * 1000L, 90.0),
            WeightEntry(now - 5 * 24 * 60 * 60 * 1000L, 88.5)
        )

        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(entries)

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        testViewModel.setTimeFilter(TimeFilter.ALL)

        val change = testViewModel.getWeightChange()

        assertThat(change).isEqualTo(-1.5)
    }

    @Test
    fun `getWeightChangeText should format positive change correctly`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            WeightEntry(now - 15 * 24 * 60 * 60 * 1000L, 88.5),
            WeightEntry(now - 5 * 24 * 60 * 60 * 1000L, 90.0)
        )

        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(entries)

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        testViewModel.setTimeFilter(TimeFilter.ALL)

        val text = testViewModel.getWeightChangeText()

        assertThat(text).contains("+")
        assertThat(text).contains("кг")
    }

    @Test
    fun `getWeightChangeText should format negative change correctly`() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            WeightEntry(now - 15 * 24 * 60 * 60 * 1000L, 90.0),
            WeightEntry(now - 5 * 24 * 60 * 60 * 1000L, 88.5)
        )

        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(entries)

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        testViewModel.setTimeFilter(TimeFilter.ALL)

        val text = testViewModel.getWeightChangeText()

        assertThat(text).contains("-")
        assertThat(text).contains("кг")
    }

    @Test
    fun `getWeightChange should return 0 when history has less than 2 entries`() = runTest {
        val entries = listOf(
            WeightEntry(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L, 90.0)
        )

        val mutableHistory = mutableListOf<WeightEntry>()
        every { mockWeightRepository.getWeightHistory(any()) } returns flowOf(mutableHistory)

        val testViewModel = StatisticsViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockExerciseStatsRepository
        )

        mutableHistory.addAll(entries)

        val change = testViewModel.getWeightChange()

        assertThat(change).isEqualTo(0.0)
    }

    @Test
    fun `getExerciseStatsSummary should return correct summary`() = runTest {
        val now = System.currentTimeMillis()

        val stats = listOf(
            ExerciseStats("Жим лёжа", now, 100.0, 10, 1, 1),
            ExerciseStats("Приседания", now, 80.0, 8, 1, 1),
            ExerciseStats("Жим лёжа", now + 1000, 110.0, 10, 1, 1)
        )

        mutableStatsFlow.value = stats

        val summary = viewModel.getExerciseStatsSummary()

        assertThat(summary.totalCount).isEqualTo(3)
        assertThat(summary.uniqueCount).isEqualTo(2)
        assertThat(summary.exerciseNames).containsExactly("Жим лёжа", "Приседания")
        assertThat(summary.totalVolume).isEqualTo(1640L + 1100L)
    }
}