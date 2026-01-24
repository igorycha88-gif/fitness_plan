package com.example.fitness_plan.domain.admin

interface AdminCredentialsRepository {
    suspend fun hasAdminCredentials(): Boolean
    suspend fun getAdminUsername(): String?
    suspend fun saveAdminCredentials(username: String, plainPassword: String)
    suspend fun verifyAdminPassword(username: String, plainPassword: String): Boolean
    fun reloadCredentials()
}
