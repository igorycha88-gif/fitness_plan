package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.admin.AdminCredentialsRepository
import javax.inject.Inject

class AdminUseCase @Inject constructor(
    private val adminCredentialsRepository: AdminCredentialsRepository
) {
    suspend fun loginAdmin(username: String, password: String): Boolean =
        adminCredentialsRepository.verifyAdminPassword(username, password)

    suspend fun hasAdminCredentials(): Boolean = adminCredentialsRepository.hasAdminCredentials()
}
