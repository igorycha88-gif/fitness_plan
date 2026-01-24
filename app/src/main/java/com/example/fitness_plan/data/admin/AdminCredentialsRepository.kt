package com.example.fitness_plan.data.admin

import android.content.SharedPreferences
import com.example.fitness_plan.domain.admin.AdminCredentialsRepository as AdminCredentialsDomainContract
import com.example.fitness_plan.data.PasswordHasher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminCredentialsRepository @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) : AdminCredentialsDomainContract {
    
    private fun initializeAdminCredentials() {
        val existingUsername = encryptedPrefs.getString("admin_username", null)
        if (existingUsername == null) {
            val passwordHasher = PasswordHasher()
            val adminHashedPassword = passwordHasher.hashPassword("admin123")
            encryptedPrefs.edit()
                .putString("admin_username", "admin")
                .putString("admin_hashed_password", adminHashedPassword)
                .apply()
        }
    }

    override suspend fun hasAdminCredentials(): Boolean {
        val u = encryptedPrefs.getString("admin_username", null)
        val p = encryptedPrefs.getString("admin_hashed_password", null)
        return !u.isNullOrEmpty() && !p.isNullOrEmpty()
    }

    override suspend fun getAdminUsername(): String? = encryptedPrefs.getString("admin_username", null)

    override suspend fun saveAdminCredentials(username: String, plainPassword: String) {
        val hashed = PasswordHasher().hashPassword(plainPassword)
        encryptedPrefs.edit()
            .putString("admin_username", username)
            .putString("admin_hashed_password", hashed)
            .apply()
    }

    override suspend fun verifyAdminPassword(username: String, plainPassword: String): Boolean {
        val storedUser = encryptedPrefs.getString("admin_username", null) ?: return false
        if (storedUser != username) return false
        val hashed = encryptedPrefs.getString("admin_hashed_password", null) ?: return false
        return PasswordHasher().verifyPassword(plainPassword, hashed)
    }

    override fun reloadCredentials() {
        // no-op for now
    }
}
