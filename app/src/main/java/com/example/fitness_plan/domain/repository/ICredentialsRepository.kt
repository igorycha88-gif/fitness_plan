package com.example.fitness_plan.domain.repository

import kotlinx.coroutines.flow.Flow

interface ICredentialsRepository {
    suspend fun saveCredentials(username: String, plainPassword: String)
    suspend fun verifyPassword(username: String, plainPassword: String): Boolean
    suspend fun getCredentials(): Credentials?
    suspend fun getUsername(): String?
    suspend fun clearCredentials()
    fun reloadCredentials()
    fun clearSession()
    fun getCredentialsFlow(): Flow<Credentials?>

    suspend fun getAppVersion(): String?
    suspend fun saveAppVersion(version: String)
    suspend fun clearAppVersion()
    suspend fun isAppVersionMismatch(currentVersion: String): Boolean
}
