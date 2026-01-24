package com.example.fitness_plan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.fitness_plan.domain.usecase.AdminUseCase

@HiltViewModel
class AdminLoginViewModel @Inject constructor(private val adminUseCase: AdminUseCase) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()
    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()
    private val _loginResult = MutableStateFlow<Boolean?>(null)
    val loginResult = _loginResult.asStateFlow()

    fun onUsernameChange(u: String) { _username.value = u }
    fun onPasswordChange(p: String) { _password.value = p }

    fun login() {
        viewModelScope.launch {
            val ok = adminUseCase.loginAdmin(_username.value, _password.value)
            _loginResult.value = ok
        }
    }
}
