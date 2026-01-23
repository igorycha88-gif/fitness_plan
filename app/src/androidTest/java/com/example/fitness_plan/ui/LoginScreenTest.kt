package com.example.fitness_plan.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: ProfileViewModel
    private var loginSuccessCalled = false
    private var registerClickCalled = false

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        loginSuccessCalled = false
        registerClickCalled = false
        every { mockViewModel.getCredentials() } returns null
    }

    @Test
    fun `LoginScreen should display username and password fields`() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        composeTestRule.onNodeWithText("Логин").assertExists()
        composeTestRule.onNodeWithText("Пароль").assertExists()
        composeTestRule.onNodeWithText("Войти").assertExists()
    }

    @Test
    fun `LoginScreen should display admin checkbox`() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        composeTestRule.onNodeWithText("Войти как администратор").assertExists()
    }

    @Test
    fun `LoginScreen should call verifyAdminPassword when admin checkbox is checked`() {
        every { mockViewModel.verifyPassword(any(), any()) } returns false
        every { mockViewModel.verifyAdminPassword(any(), any()) } returns true
        every { mockViewModel.setCurrentUsername(any()) } just Runs
        every { mockViewModel.setIsAdmin(any()) } just Runs

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        val usernameNode = composeTestRule.onNodeWithText("Логин")
        val passwordNode = composeTestRule.onNodeWithText("Пароль")
        val adminCheckboxNode = composeTestRule.onNodeWithText("Войти как администратор")
        val loginButtonNode = composeTestRule.onNodeWithText("Войти")

        usernameNode.performTextInput("admin")
        passwordNode.performTextInput("admin123")
        adminCheckboxNode.performClick()
        loginButtonNode.performClick()

        verify { mockViewModel.verifyAdminPassword("admin", "admin123") }
        verify { mockViewModel.setIsAdmin(true) }
    }

    @Test
    fun `LoginScreen should call verifyPassword when admin checkbox is not checked`() {
        every { mockViewModel.verifyPassword(any(), any()) } returns true
        every { mockViewModel.verifyAdminPassword(any(), any()) } returns false
        every { mockViewModel.setCurrentUsername(any()) } just Runs
        every { mockViewModel.setIsAdmin(any()) } just Runs

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        val usernameNode = composeTestRule.onNodeWithText("Логин")
        val passwordNode = composeTestRule.onNodeWithText("Пароль")
        val loginButtonNode = composeTestRule.onNodeWithText("Войти")

        usernameNode.performTextInput("testuser")
        passwordNode.performTextInput("testpass")
        loginButtonNode.performClick()

        verify { mockViewModel.verifyPassword("testuser", "testpass") }
        verify(exactly = 0) { mockViewModel.verifyAdminPassword(any(), any()) }
    }

    @Test
    fun `LoginScreen should show error when username is empty`() {
        every { mockViewModel.verifyPassword(any(), any()) } returns false
        every { mockViewModel.verifyAdminPassword(any(), any()) } returns false
        every { mockViewModel.setCurrentUsername(any()) } just Runs
        every { mockViewModel.setIsAdmin(any()) } just Runs

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        val passwordNode = composeTestRule.onNodeWithText("Пароль")
        val loginButtonNode = composeTestRule.onNodeWithText("Войти")

        passwordNode.performTextInput("testpass")
        loginButtonNode.performClick()

        composeTestRule.onNodeWithText("Введите логин").assertExists()
    }

    @Test
    fun `LoginScreen should show error when password is empty`() {
        every { mockViewModel.verifyPassword(any(), any()) } returns false
        every { mockViewModel.verifyAdminPassword(any(), any()) } returns false
        every { mockViewModel.setCurrentUsername(any()) } just Runs
        every { mockViewModel.setIsAdmin(any()) } just Runs

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        val usernameNode = composeTestRule.onNodeWithText("Логин")
        val loginButtonNode = composeTestRule.onNodeWithText("Войти")

        usernameNode.performTextInput("testuser")
        loginButtonNode.performClick()

        composeTestRule.onNodeWithText("Введите пароль").assertExists()
    }

    @Test
    fun `LoginScreen should call onLoginSuccess when admin login is successful`() {
        every { mockViewModel.verifyAdminPassword(any(), any()) } returns true
        every { mockViewModel.setCurrentUsername(any()) } just Runs
        every { mockViewModel.setIsAdmin(any()) } just Runs

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        val usernameNode = composeTestRule.onNodeWithText("Логин")
        val passwordNode = composeTestRule.onNodeWithText("Пароль")
        val adminCheckboxNode = composeTestRule.onNodeWithText("Войти как администратор")
        val loginButtonNode = composeTestRule.onNodeWithText("Войти")

        usernameNode.performTextInput("admin")
        passwordNode.performTextInput("admin123")
        adminCheckboxNode.performClick()
        loginButtonNode.performClick()

        assertThat(loginSuccessCalled).isTrue()
    }

    @Test
    fun `LoginScreen should show error when admin login fails`() {
        every { mockViewModel.verifyAdminPassword(any(), any()) } returns false
        every { mockViewModel.setCurrentUsername(any()) } just Runs
        every { mockViewModel.setIsAdmin(any()) } just Runs

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        val usernameNode = composeTestRule.onNodeWithText("Логин")
        val passwordNode = composeTestRule.onNodeWithText("Пароль")
        val adminCheckboxNode = composeTestRule.onNodeWithText("Войти как администратор")
        val loginButtonNode = composeTestRule.onNodeWithText("Войти")

        usernameNode.performTextInput("admin")
        passwordNode.performTextInput("wrongpassword")
        adminCheckboxNode.performClick()
        loginButtonNode.performClick()

        composeTestRule.onNodeWithText("Неверный логин или пароль").assertExists()
        assertThat(loginSuccessCalled).isFalse()
    }

    @Test
    fun `LoginScreen should show welcome message when no existing account`() {
        every { mockViewModel.getCredentials() } returns null

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        composeTestRule.onNodeWithText("Добро пожаловать!").assertExists()
        composeTestRule.onNodeWithText("Создайте аккаунт или войдите").assertExists()
    }

    @Test
    fun `LoginScreen should show welcome back message when existing account`() {
        val mockCredentials = com.example.fitness_plan.domain.repository.Credentials("testuser", "hashed_password")
        every { mockViewModel.getCredentials() } returns mockCredentials

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        composeTestRule.onNodeWithText("С возвращением!").assertExists()
        composeTestRule.onNodeWithText("Войдите, чтобы продолжить тренировки").assertExists()
    }

    @Test
    fun `LoginScreen should call onRegisterClick when register button is clicked`() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessCalled = true },
                onRegisterClick = { registerClickCalled = true },
                viewModel = mockViewModel
            )
        }

        val registerButtonNode = composeTestRule.onNodeWithText("Создать аккаунт")
        registerButtonNode.performClick()

        assertThat(registerClickCalled).isTrue()
    }
}
