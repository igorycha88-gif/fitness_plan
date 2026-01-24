package com.example.fitness_plan.domain.repository

import kotlinx.coroutines.flow.Flow

// Unified domain contract for credentials management
interface ICredentialsRepository {
    suspend fun saveCredentials(username: String, plainPassword: String)
    suspend fun verifyPassword(username: String, plainPassword: String): Boolean
    suspend fun getCredentials(): Credentials?
    suspend fun getUsername(): String?
    suspend fun clearCredentials()
    fun reloadCredentials()
    fun clearSession()
    fun getCredentialsFlow(): Flow<Credentials?>
}
