package com.phishware.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phishware.android.data.local.SessionManager
import com.phishware.android.data.model.AuthResponse
import com.phishware.android.data.repository.PhishwareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: PhishwareRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(usernameOrEmail: String, password: String) {
        if (usernameOrEmail.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(usernameOrEmail, password)
                .onSuccess { auth ->
                    sessionManager.saveSession(auth.accessToken, auth.userId, auth.username, auth.email)
                    _authState.value = AuthState.Success(auth)
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(
                        e.message ?: "Error de autenticación"
                    )
                }
        }
    }

    fun register(username: String, email: String, password: String, firstName: String?, lastName: String?) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Por favor completa los campos obligatorios")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.register(username, email, password, firstName, lastName)
                .onSuccess { auth ->
                    sessionManager.saveSession(auth.accessToken, auth.userId, auth.username, auth.email)
                    _authState.value = AuthState.Success(auth)
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Error al registrar")
                }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    sealed class AuthState {
        data object Idle    : AuthState()
        data object Loading : AuthState()
        data class  Success(val auth: AuthResponse) : AuthState()
        data class  Error(val message: String) : AuthState()
    }
}
