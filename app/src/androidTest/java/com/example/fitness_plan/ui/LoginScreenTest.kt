package com.example.fitness_plan.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitness_plan.domain.repository.Credentials
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
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

    @Before
    fun setup() {
        coEvery { mockProfileViewModel.getCredentials() } returns null
        coEvery { mockProfileViewModel.verifyPassword(any(), any()) } returns false
        every { mockProfileViewModel.setCurrentUsername(any()) } returns Unit
    }

    @Test
    fun loginScreen_shouldDisplayWelcomeMessage() {
        coEvery { mockProfileViewModel.getCredentials() } returns null

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Добро пожаловать!").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Создайте аккаунт или войдите").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun loginScreen_existingAccount_shouldDisplayWelcomeBackMessage() {
        coEvery { mockProfileViewModel.getCredentials() } returns Credentials("test_user", "hashed_password")

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("С возвращением!").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Войдите, чтобы продолжить тренировки").performScrollTo().assertIsDisplayed()
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

        composeTestRule.onNodeWithText("Логин").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Пароль").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun loginScreen_emptyUsername_shouldShowErrorMessage() {
        coEvery { mockProfileViewModel.verifyPassword(any(), any()) } returns false

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Войти").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Введите логин").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun loginScreen_emptyPassword_shouldShowErrorMessage() {
        coEvery { mockProfileViewModel.verifyPassword(any(), any()) } returns false

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Логин").performScrollTo().performTextInput("testuser")
        composeTestRule.onNodeWithText("Войти").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Введите пароль").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun loginScreen_invalidCredentials_shouldShowErrorMessage() {
        coEvery { mockProfileViewModel.verifyPassword("testuser", "wrongpass") } returns false

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Логин").performScrollTo().performTextInput("testuser")
        composeTestRule.onNodeWithText("Пароль").performScrollTo().performTextInput("wrongpass")
        composeTestRule.onNodeWithText("Войти").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Неверный логин или пароль").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun loginScreen_validCredentials_shouldTriggerLoginSuccess() {
        var loginSuccessTriggered = false
        coEvery { mockProfileViewModel.verifyPassword("testuser", "correctpass") } returns true

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = { loginSuccessTriggered = true },
                onRegisterClick = {},
                onAdminLoginClick = {},
                viewModel = mockProfileViewModel
            )
        }

        composeTestRule.onNodeWithText("Логин").performScrollTo().performTextInput("testuser")
        composeTestRule.onNodeWithText("Пароль").performScrollTo().performTextInput("correctpass")
        composeTestRule.onNodeWithText("Войти").performScrollTo().performClick()

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

        composeTestRule.onNodeWithText("Создать аккаунт").performScrollTo().performClick()
        assertTrue(registerClicked)
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

        composeTestRule.onNodeWithText("Войти как администратор").performScrollTo().performClick()
        assertTrue(adminLoginClicked)
    }
}
