package com.example.fitness_plan.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.fitness_plan.domain.repository.Credentials
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.google.common.truth.Truth.assertThat

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CredentialsRepositoryTest {

    private lateinit var mockContext: Context
    private lateinit var mockEncryptedPrefs: SharedPreferences
    private lateinit var mockPasswordHasher: PasswordHasher
    private lateinit var repository: CredentialsRepository

    @Before
    fun setup() {
        mockkConstructor(PasswordHasher::class)
        mockContext = mockk(relaxed = true)
        mockEncryptedPrefs = mockk(relaxed = true)
        mockPasswordHasher = mockk()

        every {
            EncryptedSharedPreferences.create(
                any(),
                "encrypted_prefs",
                any(),
                any()
            )
        } returns mockEncryptedPrefs

        repository = CredentialsRepository(mockEncryptedPrefs, mockPasswordHasher)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `saveCredentials should hash password and save to encrypted prefs`() = runTest {
        val username = "testuser"
        val plainPassword = "testpass123"
        val hashedPassword = "hashed_test_password"

        every { mockPasswordHasher.hashPassword(plainPassword) } returns hashedPassword
        every { mockEncryptedPrefs.edit() } returns mockk(relaxed = true)

        repository.saveCredentials(username, plainPassword)

        verify { mockPasswordHasher.hashPassword(plainPassword) }
        verify { mockEncryptedPrefs.edit() }
    }

    @Test
    fun `verifyPassword should return true when password matches`() = runTest {
        val username = "testuser"
        val plainPassword = "testpass123"
        val storedHashedPassword = "hashed_test_password"

        every { mockEncryptedPrefs.getString("credentials_username", null) } returns username
        every { mockEncryptedPrefs.getString("credentials_hashed_password", null) } returns storedHashedPassword
        every { mockPasswordHasher.verifyPassword(plainPassword, storedHashedPassword) } returns true

        repository.reloadCredentials()
        val result = repository.verifyPassword(username, plainPassword)

        assertThat(result).isTrue()
        verify { mockPasswordHasher.verifyPassword(plainPassword, storedHashedPassword) }
    }

    @Test
    fun `verifyPassword should return false when password does not match`() = runTest {
        val username = "testuser"
        val plainPassword = "wrongpass"
        val storedHashedPassword = "hashed_test_password"

        every { mockEncryptedPrefs.getString("credentials_username", null) } returns username
        every { mockEncryptedPrefs.getString("credentials_hashed_password", null) } returns storedHashedPassword
        every { mockPasswordHasher.verifyPassword(plainPassword, storedHashedPassword) } returns false

        repository.reloadCredentials()
        val result = repository.verifyPassword(username, plainPassword)

        assertThat(result).isFalse()
        verify { mockPasswordHasher.verifyPassword(plainPassword, storedHashedPassword) }
    }

    @Test
    fun `verifyPassword should return false when username does not match`() = runTest {
        val username = "wronguser"
        val plainPassword = "testpass123"
        val storedUsername = "testuser"
        val storedHashedPassword = "hashed_test_password"

        every { mockEncryptedPrefs.getString("credentials_username", null) } returns storedUsername
        every { mockEncryptedPrefs.getString("credentials_hashed_password", null) } returns storedHashedPassword

        repository.reloadCredentials()
        val result = repository.verifyPassword(username, plainPassword)

        assertThat(result).isFalse()
        verify(exactly = 0) { mockPasswordHasher.verifyPassword(any(), any()) }
    }

    @Test
    fun `saveAdminCredentials should hash password and save to encrypted prefs`() = runTest {
        val username = "admin"
        val plainPassword = "admin123"
        val hashedPassword = "hashed_admin_password"

        every { mockPasswordHasher.hashPassword(plainPassword) } returns hashedPassword
        every { mockEncryptedPrefs.edit() } returns mockk(relaxed = true)

        repository.saveAdminCredentials(username, plainPassword)

        verify { mockPasswordHasher.hashPassword(plainPassword) }
        verify { mockEncryptedPrefs.edit() }
    }

    @Test
    fun `verifyAdminPassword should return true for correct admin credentials`() = runTest {
        val username = "admin"
        val plainPassword = "admin123"
        val storedHashedPassword = "hashed_admin_password"

        every { mockEncryptedPrefs.getString("admin_username", null) } returns username
        every { mockEncryptedPrefs.getString("admin_hashed_password", null) } returns storedHashedPassword
        every { mockPasswordHasher.verifyPassword(plainPassword, storedHashedPassword) } returns true

        val result = repository.verifyAdminPassword(username, plainPassword)

        assertThat(result).isTrue()
        verify { mockPasswordHasher.verifyPassword(plainPassword, storedHashedPassword) }
    }

    @Test
    fun `verifyAdminPassword should return false for incorrect admin password`() = runTest {
        val username = "admin"
        val plainPassword = "wrongpassword"
        val storedHashedPassword = "hashed_admin_password"

        every { mockEncryptedPrefs.getString("admin_username", null) } returns username
        every { mockEncryptedPrefs.getString("admin_hashed_password", null) } returns storedHashedPassword
        every { mockPasswordHasher.verifyPassword(plainPassword, storedHashedPassword) } returns false

        val result = repository.verifyAdminPassword(username, plainPassword)

        assertThat(result).isFalse()
        verify { mockPasswordHasher.verifyPassword(plainPassword, storedHashedPassword) }
    }

    @Test
    fun `clearCredentials should remove credentials from encrypted prefs`() = runTest {
        val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { mockEncryptedPrefs.edit() } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.commit() } returns true

        repository.clearCredentials()

        verify { mockEditor.remove("credentials_username") }
        verify { mockEditor.remove("credentials_hashed_password") }
        verify { mockEditor.commit() }
    }

    @Test
    fun `getCredentials should return credentials when they exist`() {
        val username = "testuser"
        val hashedPassword = "hashed_password"

        every { mockEncryptedPrefs.getString("credentials_username", null) } returns username
        every { mockEncryptedPrefs.getString("credentials_hashed_password", null) } returns hashedPassword

        repository.reloadCredentials()
        val credentials = repository.getCredentials()

        assertThat(credentials).isNotNull()
        assertThat(credentials?.username).isEqualTo(username)
        assertThat(credentials?.hashedPassword).isEqualTo(hashedPassword)
    }

    @Test
    fun `getCredentials should return null when credentials do not exist`() {
        every { mockEncryptedPrefs.getString("credentials_username", null) } returns null
        every { mockEncryptedPrefs.getString("credentials_hashed_password", null) } returns null

        repository.reloadCredentials()
        val credentials = repository.getCredentials()

        assertThat(credentials).isNull()
    }
}
