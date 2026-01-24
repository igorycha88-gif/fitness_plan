package com.example.fitness_plan.data

import android.content.SharedPreferences
import android.util.Log
import com.example.fitness_plan.domain.repository.Credentials
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CredentialsRepository"

@Singleton
class CredentialsRepository @Inject constructor(
    private val encryptedPrefs: SharedPreferences,
    private val passwordHasher: PasswordHasher
) : ICredentialsRepository {

    private val _credentialsFlow = MutableStateFlow<Credentials?>(null)

    init {
        Log.d(TAG, "CredentialsRepository: init starting")
        Log.d(TAG, "CredentialsRepository: encryptedPrefs class=${encryptedPrefs.javaClass.name}")

        // Force synchronous read
        val username = encryptedPrefs.getString(KEY_USERNAME, null)
        val hashedPassword = encryptedPrefs.getString(KEY_HASHED_PASSWORD, null)

        Log.d(TAG, "CredentialsRepository: init read username=$username")

        if (username != null && hashedPassword != null) {
            _credentialsFlow.value = Credentials(username, hashedPassword)
            Log.d(TAG, "CredentialsRepository: init loaded credentials for user=$username")
        } else {
            Log.d(TAG, "CredentialsRepository: init no credentials found")
        }

        // Initialize admin credentials if not exist
        val adminUsername = encryptedPrefs.getString(KEY_ADMIN_USERNAME, null)
        if (adminUsername == null) {
            Log.d(TAG, "CredentialsRepository: init creating admin credentials")
            try {
                // Create admin credentials: admin / admin123
                val adminHashedPassword = passwordHasher.hashPassword("admin123")
                encryptedPrefs.edit()
                    .putString(KEY_ADMIN_USERNAME, "admin")
                    .putString(KEY_ADMIN_HASHED_PASSWORD, adminHashedPassword)
                    .apply()
                Log.d(TAG, "CredentialsRepository: init admin credentials created")
            } catch (e: Exception) {
                Log.e(TAG, "CredentialsRepository: init failed to create admin credentials", e)
            }
        } else {
            Log.d(TAG, "CredentialsRepository: init admin credentials already exist")
        }

        Log.d(TAG, "CredentialsRepository: init complete, _credentialsFlow=${_credentialsFlow.value}")
    }

    private fun loadCredentials() {
        try {
            Log.d(TAG, "loadCredentials: START")

            val username = encryptedPrefs.getString(KEY_USERNAME, null)
            val hashedPassword = encryptedPrefs.getString(KEY_HASHED_PASSWORD, null)

            Log.d(TAG, "loadCredentials: username=$username, hashedPassword=${hashedPassword?.take(8) ?: "null"}...")

            if (username != null && hashedPassword != null) {
                _credentialsFlow.value = Credentials(username, hashedPassword)
                Log.d(TAG, "loadCredentials: loaded credentials for user=$username")
            } else {
                Log.d(TAG, "loadCredentials: no credentials found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadCredentials: failed", e)
        }
    }

    override fun reloadCredentials() {
        loadCredentials()
    }

    override suspend fun saveCredentials(username: String, plainPassword: String) {
        try {
            Log.d(TAG, "saveCredentials: START for user=$username")
            val hashedPassword = passwordHasher.hashPassword(plainPassword)
            Log.d(TAG, "saveCredentials: hashedPassword length=${hashedPassword.length}")

            // Use commit() for synchronous write to ensure data is persisted before app restarts
            val success = encryptedPrefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_HASHED_PASSWORD, hashedPassword)
                .commit()

            Log.d(TAG, "saveCredentials: saved username=$username, commit success=$success")
            Log.d(TAG, "saveCredentials: saved hashedPassword=${hashedPassword.take(8)}...")

            _credentialsFlow.value = Credentials(username, hashedPassword)
            Log.d(TAG, "saveCredentials: credentials saved for user=$username, flow value updated")

            // Verify the data was actually saved to SharedPreferences
            val savedUsername = encryptedPrefs.getString(KEY_USERNAME, null)
            val savedPassword = encryptedPrefs.getString(KEY_HASHED_PASSWORD, null)
            Log.d(TAG, "saveCredentials: verification - savedUsername=$savedUsername, savedPassword=${savedPassword?.take(8) ?: "null"}...")
        } catch (e: Exception) {
            Log.e(TAG, "saveCredentials: failed", e)
        }
    }

    override suspend fun verifyPassword(username: String, plainPassword: String): Boolean {
        try {
            var currentCredentials = _credentialsFlow.value

            // If credentials not in memory, reload from SharedPreferences
            if (currentCredentials == null) {
                Log.d(TAG, "verifyPassword: credentials not in memory, reloading from SharedPreferences")
                loadCredentials()
                currentCredentials = _credentialsFlow.value
                Log.d(TAG, "verifyPassword: reloaded credentials=$currentCredentials")
            }

            Log.d(TAG, "verifyPassword: _credentialsFlow.value=$currentCredentials")
            Log.d(TAG, "verifyPassword: checking user=$username")

            if (currentCredentials == null) {
                Log.e(TAG, "verifyPassword: no credentials loaded")
                return false
            }

            if (currentCredentials.username != username) {
                Log.e(TAG, "verifyPassword: username mismatch (expected=${currentCredentials.username}, got=$username)")
                return false
            }

            val isValid = passwordHasher.verifyPassword(plainPassword, currentCredentials.hashedPassword)
            Log.d(TAG, "verifyPassword: result=$isValid for user=$username")

            if (isValid && passwordHasher.needsRehash(currentCredentials.hashedPassword)) {
                val hashedPassword = passwordHasher.hashPassword(plainPassword)
                encryptedPrefs.edit().putString(KEY_HASHED_PASSWORD, hashedPassword).apply()
                _credentialsFlow.value = currentCredentials.copy(hashedPassword = hashedPassword)
                Log.d(TAG, "verifyPassword: rehashed password for user=$username")
            }

            return isValid
        } catch (e: Exception) {
            Log.e(TAG, "verifyPassword: failed", e)
            return false
        }
    }

    override fun getCredentialsFlow(): Flow<Credentials?> {
        return _credentialsFlow.asStateFlow()
    }

    override suspend fun getCredentials(): Credentials? {
        return try {
            _credentialsFlow.value
        } catch (e: Exception) {
            Log.e(TAG, "getCredentials: failed", e)
            null
        }
    }

    override suspend fun getUsername(): String? {
        return try {
            encryptedPrefs.getString(KEY_USERNAME, null)
        } catch (e: Exception) {
            Log.e(TAG, "getUsername: failed", e)
            null
        }
    }

    override suspend fun clearCredentials() {
        try {
            Log.d(TAG, "clearCredentials: clearing credentials")
            encryptedPrefs.edit()
                .remove(KEY_USERNAME)
                .remove(KEY_HASHED_PASSWORD)
                .commit()
            _credentialsFlow.value = null
            Log.d(TAG, "clearCredentials: credentials cleared, flow updated to null")
        } catch (e: Exception) {
            Log.e(TAG, "clearCredentials: failed", e)
        }
    }

    override fun clearSession() {
        Log.d(TAG, "clearSession: clearing session (only in-memory), keeping SharedPreferences")
        _credentialsFlow.value = null
        Log.d(TAG, "clearSession: flow updated to null")
    }

    // Admin credentials methods
    suspend fun saveAdminCredentials(username: String, plainPassword: String) {
        try {
            Log.d(TAG, "saveAdminCredentials: START for user=$username")
            val hashedPassword = passwordHasher.hashPassword(plainPassword)
            Log.d(TAG, "saveAdminCredentials: hashedPassword length=${hashedPassword.length}")

            val success = encryptedPrefs.edit()
                .putString(KEY_ADMIN_USERNAME, username)
                .putString(KEY_ADMIN_HASHED_PASSWORD, hashedPassword)
                .commit()

            Log.d(TAG, "saveAdminCredentials: saved username=$username, commit success=$success")
        } catch (e: Exception) {
            Log.e(TAG, "saveAdminCredentials: failed", e)
        }
    }
 
    // Admin credentials methods removed to align with unified domain API.

    companion object {
        private const val KEY_USERNAME = "credentials_username"
        private const val KEY_HASHED_PASSWORD = "credentials_hashed_password"
        private const val KEY_ADMIN_USERNAME = "admin_username"
        private const val KEY_ADMIN_HASHED_PASSWORD = "admin_hashed_password"
    }
}
