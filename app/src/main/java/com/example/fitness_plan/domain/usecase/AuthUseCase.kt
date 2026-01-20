package com.example.fitness_plan.domain.usecase

import android.util.Log
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.repository.CredentialsRepository
import com.example.fitness_plan.domain.repository.UserRepository
import javax.inject.Inject

private const val TAG = "AuthUseCase"

class AuthUseCase @Inject constructor(
    private val credentialsRepository: CredentialsRepository,
    private val userRepository: UserRepository
) {
    sealed class AuthResult {
        data class Success(val username: String, val profile: UserProfile?) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    suspend fun login(username: String, plainPassword: String): AuthResult {
        Log.d(TAG, "login: attempting login for user=$username")

        if (username.isBlank()) {
            Log.e(TAG, "login: username is blank")
            return AuthResult.Error("Введите логин")
        }

        if (plainPassword.isBlank()) {
            Log.e(TAG, "login: password is blank")
            return AuthResult.Error("Введите пароль")
        }

        val isPasswordValid = credentialsRepository.verifyPassword(username, plainPassword)
        Log.d(TAG, "login: password valid=$isPasswordValid for user=$username")

        if (!isPasswordValid) {
            return AuthResult.Error("Неверный логин или пароль")
        }

        val profile = userRepository.getUserProfileForUsername(username)
        Log.d(TAG, "login: login successful for user=$username, profile=${profile?.username}")

        return AuthResult.Success(username, profile)
    }

    suspend fun register(username: String, plainPassword: String): AuthResult {
        Log.d(TAG, "register: attempting registration for user=$username")

        if (username.isBlank()) {
            Log.e(TAG, "register: username is blank")
            return AuthResult.Error("Введите логин")
        }

        if (plainPassword.isBlank()) {
            Log.e(TAG, "register: password is blank")
            return AuthResult.Error("Введите пароль")
        }

        if (plainPassword.length < 6) {
            Log.e(TAG, "register: password too short")
            return AuthResult.Error("Пароль должен быть не менее 6 символов")
        }

        val existingCredentials = credentialsRepository.getCredentials()
        if (existingCredentials != null) {
            Log.e(TAG, "register: user already exists")
            return AuthResult.Error("Пользователь уже существует")
        }

        credentialsRepository.saveCredentials(username, plainPassword)
        Log.d(TAG, "register: registration successful for user=$username")

        return AuthResult.Success(username, null)
    }

    suspend fun logout() {
        Log.d(TAG, "logout: logging out")
        credentialsRepository.clearSession()
    }

    suspend fun hasActiveSession(): Boolean {
        val hasSession = credentialsRepository.getCredentials() != null
        Log.d(TAG, "hasActiveSession: $hasSession")
        return hasSession
    }

    suspend fun getCurrentUsername(): String? {
        return credentialsRepository.getUsername()
    }
}
