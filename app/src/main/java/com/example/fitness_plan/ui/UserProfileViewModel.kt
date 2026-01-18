package com.example.fitness_plan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness_plan.data.UserRepository
import com.example.fitness_plan.data.UserProfile
import kotlinx.coroutines.launch

class UserProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun saveUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            userRepository.saveUserProfile(userProfile)
        }
    }
}
