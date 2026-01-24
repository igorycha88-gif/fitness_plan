package com.example.fitness_plan.presentation.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import com.example.fitness_plan.domain.admin.AdminCredentialsRepository
import com.example.fitness_plan.domain.usecase.AdminUseCase

class AdminUseCaseTest {
    @Test
    fun `loginAdmin should succeed with correct credentials`() = runTest {
        val mockRepo = mockk<AdminCredentialsRepository>(relaxed = true)
        val useCase = AdminUseCase(mockRepo)

        coEvery { mockRepo.verifyAdminPassword("admin", "admin123") } returns true

        val result = useCase.loginAdmin("admin", "admin123")
        assertTrue(result)

        coVerify { mockRepo.verifyAdminPassword("admin", "admin123") }
    }

    @Test
    fun `hasAdminCredentials should reflect repo state`() = runTest {
        val mockRepo = mockk<AdminCredentialsRepository>(relaxed = true)
        val useCase = AdminUseCase(mockRepo)

        coEvery { mockRepo.hasAdminCredentials() } returns true

        val has = useCase.hasAdminCredentials()
        assertTrue(has)
        coVerify { mockRepo.hasAdminCredentials() }
    }
}
