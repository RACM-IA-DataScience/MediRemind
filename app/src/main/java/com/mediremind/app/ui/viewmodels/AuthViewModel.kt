package com.mediremind.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediremind.app.data.repository.AuthRepository
import com.mediremind.app.data.repository.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(webClientId: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.signInWithGoogle(webClientId)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Error al iniciar sesión con Google"
                    )
                }
            )
        }
    }

    fun signInAsGuest(name: String = "Paciente") {
        authRepository.signInAsGuest(name)
    }

    fun signInMockGoogle(name: String, email: String) {
        authRepository.signInMockGoogle(name, email)
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
