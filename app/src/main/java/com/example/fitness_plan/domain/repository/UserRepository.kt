package com.example.fitness_plan.domain.repository

import com.example.fitness_plan.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun getUserProfileForUsername(username: String): UserProfile?
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun clearUserProfile()
}
