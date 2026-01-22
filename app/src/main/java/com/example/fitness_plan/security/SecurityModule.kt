package com.example.fitness_plan.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val TAG = "SecurityModule"

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    private const val PREFS_NAME = "fitness_plan_prefs"
    private const val KEY_MASTER_PASSWORD = "master_password"

    @Provides
    @Singleton
    fun provideEncryptedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return try {
            // Create or get existing MasterKey for encryption/decryption
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Create EncryptedSharedPreferences
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).also { prefs ->
                Log.d(TAG, "=== EncryptedSharedPreferences created successfully ===")
                Log.d(TAG, "File: $PREFS_NAME")

                // Check all keys in encrypted prefs
                val allKeys = try {
                    prefs.all?.keys?.joinToString(", ") ?: "empty"
                } catch (e: Exception) {
                    "error: ${e.message}"
                }

                val hasUsername = prefs.contains("credentials_username")
                val hasPassword = prefs.contains("credentials_hashed_password")
                val hasMasterPassword = prefs.contains("master_password")

                val username = prefs.getString("credentials_username", "null")
                val password = prefs.getString("credentials_hashed_password", "null")

                Log.d(TAG, "All keys: $allKeys")
                Log.d(TAG, "hasUsername=$hasUsername, hasPassword=$hasPassword, hasMasterPassword=$hasMasterPassword")
                Log.d(TAG, "username=$username, password=${password?.take(8) ?: "null"}...")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create EncryptedSharedPreferences, falling back to regular SharedPreferences", e)
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    @Provides
    @Singleton
    fun provideMasterPassword(encryptedPrefs: SharedPreferences): String {
        return try {
            var password = encryptedPrefs.getString(KEY_MASTER_PASSWORD, null)
            if (password == null) {
                password = generateMasterPassword()
                encryptedPrefs.edit().putString(KEY_MASTER_PASSWORD, password).apply()
            }
            password
        } catch (e: Exception) {
            Log.e(TAG, "provideMasterPassword: failed to get master password", e)
            generateMasterPassword()
        }
    }

    private fun generateMasterPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..32).map { chars[kotlin.random.Random.nextInt(chars.length)] }.joinToString("")
    }
}
