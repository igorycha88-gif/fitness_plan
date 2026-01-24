package com.example.fitness_plan.data

import android.content.SharedPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import com.example.fitness_plan.data.CredentialsRepository
import com.example.fitness_plan.data.PasswordHasher
import kotlin.test.assertTrue

@org.junit.Ignore("Pending fix for test environment after Admin modularization")
class CredentialsRepositoryTest {
    @Test
    fun `saveCredentials should write to prefs`() = runTest {
        val mockPrefs: SharedPreferences = mockk(relaxed = true)
        val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.commit() } returns true

        val repo = CredentialsRepository(mockPrefs, PasswordHasher())
        repo.saveCredentials("admin", "admin123")

        coVerify { mockEditor.putString("credentials_username", any()) }
        coVerify { mockEditor.putString("credentials_hashed_password", any()) }
        coVerify { mockEditor.commit() }
    }

    @Test
    fun `verifyPassword returns true for correct credentials`() = runTest {
        val mockPrefs: SharedPreferences = mockk(relaxed = true)
        val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.commit() } returns true

        val hasher = PasswordHasher()
        val repo = CredentialsRepository(mockPrefs, hasher)
        val username = "admin"
        val hashed = hasher.hashPassword("admin123")
        every { mockPrefs.getString("credentials_username", any()) } returns username
        every { mockPrefs.getString("credentials_hashed_password", any()) } returns hashed

        val result = repo.verifyPassword(username, "admin123")
        assertTrue(result)
    }
}
