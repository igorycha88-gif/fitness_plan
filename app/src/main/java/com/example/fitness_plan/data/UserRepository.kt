package com.example.fitness_plan.data

import android.content.Context
import android.util.Log
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
import java.io.File

private const val TAG = "UserRepository"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserRepository(private val context: Context) : DomainUserRepository {

    private fun getDataStorePath(): String {
        return try {
            val dataDir = context.dataDir?.absolutePath ?: "N/A"
            val datastorePath = File(dataDir, "datastore/user_preferences.preferences_pb").absolutePath
            Log.d(TAG, "DataStore path: $datastorePath")
            Log.d(TAG, "DataStore exists: ${File(datastorePath).exists()}")
            datastorePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get DataStore path", e)
            "Error getting path: ${e.message}"
        }
    }

    init {
        Log.d(TAG, "=== UserRepository initialized ===")
        getDataStorePath()
    }

    override suspend fun saveUserProfile(userProfile: UserProfile) {
        try {
            Log.d(TAG, "saveUserProfile: START for username=${userProfile.username}")
            Log.d(TAG, "saveUserProfile: goal=${userProfile.goal}, level=${userProfile.level}, frequency=${userProfile.frequency}")
            getDataStorePath()

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

            Log.d(TAG, "saveUserProfile: SUCCESS - profile saved for username=${userProfile.username}")

            verifyStoredData(userProfile)
        } catch (e: Exception) {
            Log.e(TAG, "saveUserProfile: FAILED to save profile", e)
            throw e
        }
    }

    private suspend fun verifyStoredData(originalProfile: UserProfile) {
        try {
            val storedProfile = getUserProfile().first()
            if (storedProfile == null) {
                Log.e(TAG, "verifyStoredData: WARNING - No profile found after save!")
            } else if (storedProfile == originalProfile) {
                Log.d(TAG, "verifyStoredData: SUCCESS - Profile matches original")
            } else {
                Log.w(TAG, "verifyStoredData: WARNING - Profile mismatch!")
                Log.d(TAG, "verifyStoredData: original=$originalProfile")
                Log.d(TAG, "verifyStoredData: stored=$storedProfile")
            }
        } catch (e: Exception) {
            Log.e(TAG, "verifyStoredData: FAILED", e)
        }
    }

    override fun getUserProfile(): Flow<UserProfile?> = context.dataStore.data.map { preferences ->
        try {
            val username = preferences[USERNAME_KEY]
            val goal = preferences[GOAL_KEY]
            val level = preferences[LEVEL_KEY]
            val frequency = preferences[FREQUENCY_KEY]
            val weightStr = preferences[WEIGHT_KEY]
            val heightStr = preferences[HEIGHT_KEY]
            val gender = preferences[GENDER_KEY]
            val favoriteExercises = preferences[FAVORITE_EXERCISES_KEY] ?: emptySet()

            Log.d(TAG, "getUserProfile: username=$username, goal=$goal, level=$level")

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
                ).also {
                    Log.d(TAG, "getUserProfile: SUCCESS - loaded profile for username=$username")
                }
            } else {
                Log.w(TAG, "getUserProfile: null - incomplete data (goal=$goal, level=$level, frequency=$frequency, weight=$weightStr, height=$heightStr, gender=$gender)")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUserProfile: FAILED", e)
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