package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.repository.Credentials
import com.example.fitness_plan.domain.repository.ICredentialsRepository
import com.example.fitness_plan.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@org.junit.Ignore("Pending fix for test environment after Gradle configuration")
class AuthUseCaseVersionTest {

    @Test
    fun `login should save app version`() = runTest {
        val mockCredentialsRepo: ICredentialsRepository = mockk(relaxed = true)
        val mockUserRepo: UserRepository = mockk(relaxed = true)
        coEvery { mockUserRepo.getUserProfileForUsername(any()) } returns null

        val authUseCase = AuthUseCase(mockCredentialsRepo, mockUserRepo)
        
        authUseCase.login("testuser", "password123", "2.2")
        
        coVerify { mockCredentialsRepo.saveAppVersion("2.2") }
    }

    @Test
    fun `register should save app version`() = runTest {
        val mockCredentialsRepo: ICredentialsRepository = mockk(relaxed = true)
        val mockUserRepo: UserRepository = mockk(relaxed = true)
        coEvery { mockCredentialsRepo.getCredentials() } returns null
        coEvery { mockUserRepo.getUserProfileForUsername(any()) } returns null

        val authUseCase = AuthUseCase(mockCredentialsRepo, mockUserRepo)
        
        authUseCase.register("testuser", "password123", "2.2")
        
        coVerify { mockCredentialsRepo.saveCredentials(any(), any()) }
        coVerify { mockCredentialsRepo.saveAppVersion("2.2") }
    }

    @Test
    fun `checkAndUpdateSession should return true when versions match`() = runTest {
        val mockCredentialsRepo: ICredentialsRepository = mockk(relaxed = true)
        val mockUserRepo: UserRepository = mockk(relaxed = true)
        coEvery { mockCredentialsRepo.getCredentials() } returns Credentials("testuser", "hashed_password")
        coEvery { mockCredentialsRepo.isAppVersionMismatch(any()) } returns false

        val authUseCase = AuthUseCase(mockCredentialsRepo, mockUserRepo)
        
        val result = authUseCase.checkAndUpdateSession("2.2")
        
        assertTrue(result)
        coVerify(exactly = 0) { mockCredentialsRepo.clearSession() }
    }

    @Test
    fun `checkAndUpdateSession should return false and clear session when versions mismatch`() = runTest {
        val mockCredentialsRepo: ICredentialsRepository = mockk(relaxed = true)
        val mockUserRepo: UserRepository = mockk(relaxed = true)
        coEvery { mockCredentialsRepo.getCredentials() } returns Credentials("testuser", "hashed_password")
        coEvery { mockCredentialsRepo.isAppVersionMismatch("2.2") } returns true

        val authUseCase = AuthUseCase(mockCredentialsRepo, mockUserRepo)
        
        val result = authUseCase.checkAndUpdateSession("2.2")
        
        assertFalse(result)
        coVerify { mockCredentialsRepo.clearSession() }
    }

    @Test
    fun `checkAndUpdateSession should return false when no credentials`() = runTest {
        val mockCredentialsRepo: ICredentialsRepository = mockk(relaxed = true)
        val mockUserRepo: UserRepository = mockk(relaxed = true)
        coEvery { mockCredentialsRepo.getCredentials() } returns null

        val authUseCase = AuthUseCase(mockCredentialsRepo, mockUserRepo)
        
        val result = authUseCase.checkAndUpdateSession("2.2")
        
        assertFalse(result)
        coVerify(exactly = 0) { mockCredentialsRepo.clearSession() }
    }
}
