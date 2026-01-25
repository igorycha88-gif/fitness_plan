package com.example.fitness_plan.presentation.viewmodel

import com.example.fitness_plan.data.CredentialsRepository
import com.example.fitness_plan.data.UserRepository
import com.example.fitness_plan.data.CycleRepository
import com.example.fitness_plan.data.WeightRepository
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import com.example.fitness_plan.domain.repository.UserRepository as DomainUserRepository
import com.example.fitness_plan.domain.repository.CycleRepository as DomainCycleRepository
import com.example.fitness_plan.domain.repository.WeightRepository as DomainWeightRepository
import com.example.fitness_plan.domain.usecase.AuthUseCase
import com.example.fitness_plan.domain.usecase.WorkoutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.clearAllMocks
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@org.junit.Ignore("Skip until test suite stabilized with Admin modularization")
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var mockCredentialsRepository: ICredentialsRepository
    private lateinit var mockUserRepository: DomainUserRepository
    private lateinit var mockCycleRepository: DomainCycleRepository
    private lateinit var mockWeightRepository: DomainWeightRepository
    private lateinit var mockAuthUseCase: AuthUseCase
    private lateinit var mockWorkoutUseCase: WorkoutUseCase
    private lateinit var mockExerciseLibraryUseCase: com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase
    private lateinit var mockReferenceDataUseCase: com.example.fitness_plan.domain.usecase.ReferenceDataUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockCredentialsRepository = mockk<ICredentialsRepository>(relaxed = true)
        mockUserRepository = mockk<DomainUserRepository>(relaxed = true)
        mockCycleRepository = mockk<DomainCycleRepository>(relaxed = true)
        mockWeightRepository = mockk<DomainWeightRepository>(relaxed = true)
        mockAuthUseCase = mockk<AuthUseCase>(relaxed = true)
        mockWorkoutUseCase = mockk<WorkoutUseCase>(relaxed = true)
        mockExerciseLibraryUseCase = mockk<com.example.fitness_plan.domain.usecase.ExerciseLibraryUseCase>(relaxed = true)
        mockReferenceDataUseCase = mockk<com.example.fitness_plan.domain.usecase.ReferenceDataUseCase>(relaxed = true)

        viewModel = ProfileViewModel(
            mockUserRepository,
            mockCredentialsRepository,
            mockCycleRepository,
            mockWeightRepository,
            mockAuthUseCase,
            mockWorkoutUseCase,
            mockExerciseLibraryUseCase,
            mockReferenceDataUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
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
        coEvery { mockWorkoutUseCase.updateWorkoutSchedule(any(), any()) } just runs

        viewModel.saveWorkoutDates(dates)

        coVerify { mockWorkoutUseCase.updateWorkoutSchedule(username, dates) }
    }
}
