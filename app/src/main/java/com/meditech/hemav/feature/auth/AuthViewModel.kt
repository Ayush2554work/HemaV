package com.meditech.hemav.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditech.hemav.data.model.UserRole
import com.meditech.hemav.data.model.DoctorProfile
import com.meditech.hemav.data.repository.AuthRepository
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userRole: UserRole? = null,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    var uiState by mutableStateOf(AuthUiState())
        private set

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val user = repository.currentUser
        if (user != null) {
            viewModelScope.launch {
                val role = repository.getUserRole(user.uid)
                uiState = uiState.copy(isLoggedIn = true, userRole = role)
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Please fill in all fields")
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            val result = repository.login(email, password)
            result.onSuccess { user ->
                val role = repository.getUserRole(user.uid)
                uiState = uiState.copy(isLoading = false, isLoggedIn = true, userRole = role)
            }.onFailure { e ->
                uiState = uiState.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun register(name: String, email: String, password: String, phone: String, role: UserRole) {
        registerDoctor(name, email, password, phone, role, null)
    }

    fun registerDoctor(
        name: String, email: String, password: String, phone: String, role: UserRole,
        doctorProfile: DoctorProfile?
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Please fill in all fields")
            return
        }
        if (password.length < 6) {
            uiState = uiState.copy(errorMessage = "Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            val result = repository.register(name, email, password, phone, role)
            result.onSuccess { user ->
                // If doctor, save doctor profile
                if (role == UserRole.DOCTOR && doctorProfile != null) {
                    repository.saveDoctorProfile(user.uid, doctorProfile.copy(uid = user.uid, name = name))
                }
                uiState = uiState.copy(isLoading = false, isLoggedIn = true, userRole = role)
            }.onFailure { e ->
                uiState = uiState.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun logout() {
        repository.logout()
        uiState = AuthUiState()
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}
