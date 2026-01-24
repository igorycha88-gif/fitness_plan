package com.example.fitness_plan.data.admin

import android.content.SharedPreferences
import com.example.fitness_plan.data.PasswordHasher
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AdminCredentialsRepositoryTest {

    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var repository: AdminCredentialsRepository
    private val passwordHasher = PasswordHasher()

    @Before
    fun setup() {
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        every { mockPrefs.edit() } returns mockEditor
        repository = AdminCredentialsRepository(mockPrefs)
    }

    @Test
    fun `hasAdminCredentials should return true when both username and password exist`() = runTest {
        every { mockPrefs.getString("admin_username", null) } returns "admin"
        every { mockPrefs.getString("admin_hashed_password", null) } returns "hashed_password"

        val result = repository.hasAdminCredentials()

        assertTrue(result)
    }

    @Test
    fun `hasAdminCredentials should return false when username is null`() = runTest {
        every { mockPrefs.getString("admin_username", null) } returns null
        every { mockPrefs.getString("admin_hashed_password", null) } returns "hashed_password"

        val result = repository.hasAdminCredentials()

        assertFalse(result)
    }

    @Test
    fun `hasAdminCredentials should return false when password is null`() = runTest {
        every { mockPrefs.getString("admin_username", null) } returns "admin"
        every { mockPrefs.getString("admin_hashed_password", null) } returns null

        val result = repository.hasAdminCredentials()

        assertFalse(result)
    }

    @Test
    fun `hasAdminCredentials should return false when both are null`() = runTest {
        every { mockPrefs.getString("admin_username", null) } returns null
        every { mockPrefs.getString("admin_hashed_password", null) } returns null

        val result = repository.hasAdminCredentials()

        assertFalse(result)
    }

    @Test
    fun `getAdminUsername should return stored username`() = runTest {
        every { mockPrefs.getString("admin_username", null) } returns "admin"

        val result = repository.getAdminUsername()

        assertEquals("admin", result)
    }

    @Test
    fun `getAdminUsername should return null when not set`() = runTest {
        every { mockPrefs.getString("admin_username", null) } returns null

        val result = repository.getAdminUsername()

        assertNull(result)
    }

    @Test
    fun `saveAdminCredentials should store username and hashed password`() = runTest {
        val password = "admin123"

        every { mockEditor.putString("admin_username", "admin") } returns mockEditor
        every { mockEditor.putString("admin_hashed_password", any()) } returns mockEditor
        every { mockEditor.apply() } just runs
        every { mockPrefs.edit() } returns mockEditor

        repository.saveAdminCredentials("admin", password)

        verify { mockEditor.putString("admin_username", "admin") }
        verify(exactly = 1) { mockEditor.putString("admin_hashed_password", any()) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `verifyAdminPassword should return true for correct credentials`() = runTest {
        val password = "admin123"
        val hashedPassword = passwordHasher.hashPassword(password)

        every { mockPrefs.getString("admin_username", null) } returns "admin"
        every { mockPrefs.getString("admin_hashed_password", null) } returns hashedPassword

        val result = repository.verifyAdminPassword("admin", password)

        assertTrue(result)
    }

    @Test
    fun `verifyAdminPassword should return false for wrong username`() = runTest {
        val password = "admin123"
        val hashedPassword = passwordHasher.hashPassword(password)

        every { mockPrefs.getString("admin_username", null) } returns "admin"
        every { mockPrefs.getString("admin_hashed_password", null) } returns hashedPassword

        val result = repository.verifyAdminPassword("wrong_user", password)

        assertFalse(result)
    }

    @Test
    fun `verifyAdminPassword should return false for wrong password`() = runTest {
        val correctPassword = "admin123"
        val hashedPassword = passwordHasher.hashPassword(correctPassword)

        every { mockPrefs.getString("admin_username", null) } returns "admin"
        every { mockPrefs.getString("admin_hashed_password", null) } returns hashedPassword

        val result = repository.verifyAdminPassword("admin", "wrong_password")

        assertFalse(result)
    }

    @Test
    fun `verifyAdminPassword should return false when username is null`() = runTest {
        every { mockPrefs.getString("admin_username", null) } returns null

        val result = repository.verifyAdminPassword("admin", "admin123")

        assertFalse(result)
    }

    @Test
    fun `verifyAdminPassword should return false when password is null`() = runTest {
        every { mockPrefs.getString("admin_username", null) } returns "admin"
        every { mockPrefs.getString("admin_hashed_password", null) } returns null

        val result = repository.verifyAdminPassword("admin", "admin123")

        assertFalse(result)
    }

    @Test
    fun `reloadCredentials should be no-op`() {
        repository.reloadCredentials()
    }
}
