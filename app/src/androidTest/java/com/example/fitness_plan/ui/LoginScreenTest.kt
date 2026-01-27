package com.example.fitness_plan.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockProfileViewModel = mockk<com.example.fitness_plan.presentation.viewmodel.ProfileViewModel>()

    private val credentialsFlow = MutableStateFlow<String?>(null)

    @Before
    fun setup() {
        every { mockProfileViewModel.getCredentials() } returns credentialsFlow.value
        every { mockProfileViewModel.verifyPassword(any(), any()) } returns false
        every { mockProfileViewModel.setCurrentUsername(any()) } returns Unit
    }

    @Test
    fun loginScreen_shouldDisplayWelcomeMessage() {
        credentialsFlow.value = null

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Добро пожаловать!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Создайте аккаунт или войдите").assertIsDisplayed()
    }

    @Test
    fun loginScreen_existingAccount_shouldDisplayWelcomeBackMessage() {
        credentialsFlow.value = "test_user"

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("С возвращением!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Войдите, чтобы продолжить тренировки").assertIsDisplayed()
    }

    @Test
    fun loginScreen_shouldDisplayUsernameAndPasswordFields() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Логин").assertIsDisplayed()
        composeTestRule.onNodeWithText("Пароль").assertIsDisplayed()
    }

    @Test
    fun loginScreen_emptyUsername_shouldShowErrorMessage() {
        every { mockProfileViewModel.verifyPassword(any(), any()) } returns false

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Войти").performClick()
        composeTestRule.onNodeWithText("Введите логин").assertIsDisplayed()
    }

    @Test
    fun loginScreen_emptyPassword_shouldShowErrorMessage() {
        every { mockProfileViewModel.verifyPassword(any(), any()) } returns false

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Логин").performTextInput("testuser")
        composeTestRule.onNodeWithText("Войти").performClick()
        composeTestRule.onNodeWithText("Введите пароль").assertIsDisplayed()
    }

    @Test
    fun loginScreen_invalidCredentials_shouldShowErrorMessage() {
        every { mockProfileViewModel.verifyPassword("testuser", "wrongpass") } returns false

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Логин").performTextInput("testuser")
        composeTestRule.onNodeWithText("Пароль").performTextInput("wrongpass")
        composeTestRule.onNodeWithText("Войти").performClick()
        composeTestRule.onNodeWithText("Неверный логин или пароль").assertIsDisplayed()
    }

    @Test
    fun loginScreen_validCredentials_shouldTriggerLoginSuccess() {
        var loginSuccessTriggered = false
        every { mockProfileViewModel.verifyPassword("testuser", "correctpass") } returns true

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessTriggered = true },
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Логин").performTextInput("testuser")
        composeTestRule.onNodeWithText("Пароль").performTextInput("correctpass")
        composeTestRule.onNodeWithText("Войти").performClick()

        composeTestRule.waitUntil(5000) { loginSuccessTriggered }
    }

    @Test
    fun loginScreen_onRegisterClick_shouldTriggerCallback() {
        var registerClicked = false

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = { registerClicked = true },
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Создать аккаунт").performClick()
        kotlin.test.assertTrue(registerClicked)
    }

    @Test
    fun loginScreen_onAdminLoginClick_shouldTriggerCallback() {
        var adminLoginClicked = false

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = { adminLoginClicked = true },
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Войти как администратор").performClick()
        kotlin.test.assertTrue(adminLoginClicked)
    }
}
