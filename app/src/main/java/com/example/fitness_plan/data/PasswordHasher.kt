package com.example.fitness_plan.data

import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordHasher @Inject constructor() {

    companion object {
        private const val COST_FACTOR = 12
    }

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(COST_FACTOR))
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return try {
            BCrypt.checkpw(password, hashedPassword)
        } catch (e: Exception) {
            false
        }
    }

    fun needsRehash(hashedPassword: String): Boolean {
        return try {
            val salt = hashedPassword.substring(0, 29)
            val currentCost = salt.substring(4, 6).toIntOrNull() ?: 0
            currentCost < COST_FACTOR
        } catch (e: Exception) {
            false
        }
    }
}
