package com.example.fitness_plan.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AdminLoginViewModelTest {

    private lateinit var viewModel: AdminLoginViewModel
    private val mockAdminUseCase = mockk<com.example.fitness_plan.domain.usecase.AdminUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        viewModel = AdminLoginViewModel(mockAdminUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty username and password`() = runTest {
        assertEquals("", viewModel.username.value)
        assertEquals("", viewModel.password.value)
        assertNull(viewModel.loginResult.value)
    }

    @Test
    fun `onUsernameChange should update username`() = runTest {
        viewModel.onUsernameChange("admin")

        assertEquals("admin", viewModel.username.value)
    }

    @Test
    fun `onPasswordChange should update password`() = runTest {
        viewModel.onPasswordChange("admin123")

        assertEquals("admin123", viewModel.password.value)
    }

    @Test
    fun `login should set loginResult to true on successful login`() = runTest {
        coEvery { mockAdminUseCase.loginAdmin("admin", "admin123") } returns true

        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("admin123")
        viewModel.login()

        assertEquals(true, viewModel.loginResult.first())

        coVerify { mockAdminUseCase.loginAdmin("admin", "admin123") }
    }

    @Test
    fun `login should set loginResult to false on failed login`() = runTest {
        coEvery { mockAdminUseCase.loginAdmin("admin", "wrong_password") } returns false

        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("wrong_password")
        viewModel.login()

        assertEquals(false, viewModel.loginResult.first())

        coVerify { mockAdminUseCase.loginAdmin("admin", "wrong_password") }
    }

    @Test
    fun `login should call use case with empty credentials and return false`() = runTest {
        coEvery { mockAdminUseCase.loginAdmin("", "") } returns false

        viewModel.onUsernameChange("")
        viewModel.onPasswordChange("")
        viewModel.login()

        assertEquals(false, viewModel.loginResult.first())
        coVerify { mockAdminUseCase.loginAdmin("", "") }
    }

    @Test
    fun `multiple login calls should update loginResult correctly`() = runTest {
        coEvery { mockAdminUseCase.loginAdmin("admin", "admin123") } returns true
        coEvery { mockAdminUseCase.loginAdmin("admin", "wrong") } returns false

        viewModel.onUsernameChange("admin")
        viewModel.onPasswordChange("wrong")
        viewModel.login()

        assertEquals(false, viewModel.loginResult.first())

        viewModel.onPasswordChange("admin123")
        viewModel.login()

        assertEquals(true, viewModel.loginResult.first())
    }
}
