package com.example.fitness_plan.domain.repository

import kotlinx.coroutines.flow.Flow

interface CredentialsRepository {
    suspend fun saveCredentials(username: String, plainPassword: String)
    suspend fun verifyPassword(username: String, plainPassword: String): Boolean
    fun getCredentialsFlow(): Flow<Credentials?>
    suspend fun getCredentials(): Credentials?
    suspend fun getUsername(): String?
    suspend fun clearCredentials()
    fun reloadCredentials()
    fun clearSession()
}

data class Credentials(
    val username: String,
    val hashedPassword: String
)
