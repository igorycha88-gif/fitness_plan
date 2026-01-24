package com.example.fitness_plan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitness_plan.domain.model.UserProfile
import com.example.fitness_plan.domain.repository.UserRepository as DomainUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserRepository(private val context: Context) : DomainUserRepository {

    override suspend fun saveUserProfile(userProfile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = userProfile.username
            preferences[GOAL_KEY] = userProfile.goal
            preferences[LEVEL_KEY] = userProfile.level
            preferences[FREQUENCY_KEY] = userProfile.frequency
            preferences[WEIGHT_KEY] = userProfile.weight.toString()
            preferences[HEIGHT_KEY] = userProfile.height.toString()
            preferences[GENDER_KEY] = userProfile.gender
            preferences[FAVORITE_EXERCISES_KEY] = userProfile.favoriteExercises
        }
    }

    override fun getUserProfile(): Flow<UserProfile?> = context.dataStore.data.map { preferences ->
        val username = preferences[USERNAME_KEY]
        val goal = preferences[GOAL_KEY]
        val level = preferences[LEVEL_KEY]
        val frequency = preferences[FREQUENCY_KEY]
        val weightStr = preferences[WEIGHT_KEY]
        val heightStr = preferences[HEIGHT_KEY]
        val gender = preferences[GENDER_KEY]
        val favoriteExercises = preferences[FAVORITE_EXERCISES_KEY] ?: emptySet()
 
        if (goal != null && level != null && frequency != null && weightStr != null && heightStr != null && gender != null) {
            UserProfile(
                username = username ?: "",
                goal = goal,
                level = level,
                frequency = frequency,
                weight = weightStr.toDouble(),
                height = heightStr.toDouble(),
                gender = gender,
                favoriteExercises = favoriteExercises
            )
        } else {
            null
        }
    }

    override suspend fun getUserProfileForUsername(username: String): UserProfile? {
        return getUserProfile().first().takeIf { it?.username == username }
    }

    override suspend fun clearUserProfile() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    override suspend fun toggleFavoriteExercise(exerciseName: String) {
        val profile = getUserProfile().first() ?: return
        val updatedFavorites = if (exerciseName in profile.favoriteExercises) {
            profile.favoriteExercises - exerciseName
        } else {
            profile.favoriteExercises + exerciseName
        }
        saveUserProfile(profile.copy(favoriteExercises = updatedFavorites))
    }

    override fun getFavoriteExercises(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[FAVORITE_EXERCISES_KEY] ?: emptySet()
        }
    }

    companion object {
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val GOAL_KEY = stringPreferencesKey("goal")
        private val LEVEL_KEY = stringPreferencesKey("level")
        private val FREQUENCY_KEY = stringPreferencesKey("frequency")
        private val WEIGHT_KEY = stringPreferencesKey("weight")
        private val HEIGHT_KEY = stringPreferencesKey("height")
        private val GENDER_KEY = stringPreferencesKey("gender")
        private val FAVORITE_EXERCISES_KEY = stringSetPreferencesKey("favorite_exercises")
    }
}

data class WeightEntry(
    val date: Long,
    val weight: Double
)