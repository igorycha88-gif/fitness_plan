package com.example.fitness_plan.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check all keys in prefs
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

        Log.d(TAG, "=== SharedPreferences Info ===")
        Log.d(TAG, "File: $PREFS_NAME")
        Log.d(TAG, "All keys: $allKeys")
        Log.d(TAG, "hasUsername=$hasUsername, hasPassword=$hasPassword, hasMasterPassword=$hasMasterPassword")
        Log.d(TAG, "username=$username, password=${password?.take(8) ?: "null"}...")

        return prefs
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
