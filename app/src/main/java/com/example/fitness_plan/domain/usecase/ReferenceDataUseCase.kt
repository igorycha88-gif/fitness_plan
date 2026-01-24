package com.example.fitness_plan.domain.usecase

import com.example.fitness_plan.domain.repository.ReferenceDataRepository
import com.example.fitness_plan.domain.repository.UserRepository
import com.example.fitness_plan.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReferenceDataUseCase @Inject constructor(
    private val referenceDataRepository: ReferenceDataRepository,
    private val userRepository: UserRepository
) {
    suspend fun initializeReferenceData() {
        referenceDataRepository as com.example.fitness_plan.data.ReferenceDataRepositoryImpl
        referenceDataRepository.initializeReferenceData()
    }

    suspend fun toggleFavoriteExercise(exerciseName: String) {
        userRepository.toggleFavoriteExercise(exerciseName)
    }

    fun getFavoriteExercises(): Flow<Set<String>> {
        return userRepository.getFavoriteExercises()
    }

    suspend fun isExerciseFavorite(exerciseName: String): Boolean {
        return userRepository.getUserProfileForUsername("")?.favoriteExercises?.contains(exerciseName) ?: false
    }
}
