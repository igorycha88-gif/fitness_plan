package com.example.fitness_plan.presentation.viewmodel

import com.example.fitness_plan.domain.repository.CredentialsRepository
import com.example.fitness_plan.domain.repository.CycleRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.repository.WeightRepository
import com.example.fitness_plan.domain.usecase.AuthUseCase
import com.example.fitness_plan.domain.usecase.WorkoutUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var mockCredentialsRepository: CredentialsRepository
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockCycleRepository: CycleRepository
    private lateinit var mockWeightRepository: WeightRepository
    private lateinit var mockAuthUseCase: AuthUseCase
    private lateinit var mockWorkoutUseCase: WorkoutUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockCredentialsRepository = mockk(relaxed = true)
        mockUserRepository = mockk(relaxed = true)
        mockCycleRepository = mockk(relaxed = true)
        mockWeightRepository = mockk(relaxed = true)
        mockAuthUseCase = mockk(relaxed = true)
        mockWorkoutUseCase = mockk(relaxed = true)

        viewModel = ProfileViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockAuthUseCase,
            mockWorkoutUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `setCurrentUsername should update currentUsername value`() {
        val username = "testuser"

        viewModel.setCurrentUsername(username)

        assertThat(viewModel.currentUsername.value).isEqualTo(username)
    }

    @Test
    fun `setIsAdmin should update isAdmin value`() {
        viewModel.setIsAdmin(true)
        assertThat(viewModel.isAdmin.value).isTrue()

        viewModel.setIsAdmin(false)
        assertThat(viewModel.isAdmin.value).isFalse()
    }

    @Test
    fun `verifyPassword should delegate to credentialsRepository`() = runTest {
        val username = "testuser"
        val password = "testpass123"
        val expectedResult = true

        coEvery { mockCredentialsRepository.verifyPassword(username, password) } returns expectedResult

        val result = viewModel.verifyPassword(username, password)

        assertThat(result).isEqualTo(expectedResult)
        coVerify { mockCredentialsRepository.verifyPassword(username, password) }
    }

    @Test
    fun `verifyAdminPassword should delegate to credentialsRepository`() = runTest {
        val username = "admin"
        val password = "admin123"
        val expectedResult = true

        coEvery { mockCredentialsRepository.verifyAdminPassword(username, password) } returns expectedResult

        val result = viewModel.verifyAdminPassword(username, password)

        assertThat(result).isEqualTo(expectedResult)
        coVerify { mockCredentialsRepository.verifyAdminPassword(username, password) }
    }

    @Test
    fun `verifyAdminPassword should return false for incorrect admin credentials`() = runTest {
        val username = "admin"
        val password = "wrongpassword"

        coEvery { mockCredentialsRepository.verifyAdminPassword(username, password) } returns false

        val result = viewModel.verifyAdminPassword(username, password)

        assertThat(result).isFalse()
        coVerify { mockCredentialsRepository.verifyAdminPassword(username, password) }
    }

    @Test
    fun `verifyAdminPassword should return false for non-admin username`() = runTest {
        val username = "regularuser"
        val password = "any_password"

        coEvery { mockCredentialsRepository.verifyAdminPassword(username, password) } returns false

        val result = viewModel.verifyAdminPassword(username, password)

        assertThat(result).isFalse()
        coVerify { mockCredentialsRepository.verifyAdminPassword(username, password) }
    }

    @Test
    fun `logout should clear credentials and reset state`() = runTest {
        viewModel.setCurrentUsername("testuser")
        viewModel.setIsAdmin(true)

        viewModel.logout()

        assertThat(viewModel.currentUsername.value).isEmpty()
        assertThat(viewModel.isAdmin.value).isFalse()
        assertThat(viewModel.logoutTrigger.value).isTrue()
        coVerify { mockAuthUseCase.logout() }
    }

    @Test
    fun `clearLogoutTrigger should reset logoutTrigger to false`() {
        viewModel.logout()
        assertThat(viewModel.logoutTrigger.value).isTrue()

        viewModel.clearLogoutTrigger()

        assertThat(viewModel.logoutTrigger.value).isFalse()
    }

    @Test
    fun `saveWorkoutDates should delegate to workoutUseCase`() = runTest {
        val username = "testuser"
        val dates = listOf(1000L, 2000L, 3000L)

        viewModel.setCurrentUsername(username)
        coEvery { mockWorkoutUseCase.updateWorkoutSchedule(any(), any()) } just Runs.Unit

        viewModel.saveWorkoutDates(dates)

        coVerify { mockWorkoutUseCase.updateWorkoutSchedule(username, dates) }
    }
}
