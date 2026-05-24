package com.dsm.catedra2.eventos_comunitarios.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.catedra2.eventos_comunitarios.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun checkSession() {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId()
            if (userId != null) {
                _authState.value = AuthState.Loading
                try {
                    // Si hay sesión activa, recuperamos su rol de Firestore
                    val role = repository.getUserRole(userId)
                    // Nota: Aquí podrías obtener el email también si fuera necesario
                    _authState.value = AuthState.Authenticated(userId, role, "")
                } catch (e: Exception) {
                    _authState.value = AuthState.Error("Error al recuperar sesión")
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, pass)
            handleResult(result, email)
        }
    }

    fun register(email: String, pass: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(email, pass, role)
            handleResult(result, email)
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginWithGoogle(idToken)
            handleResult(result)
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }

    private fun handleResult(result: Result<Pair<String, String>>, email: String = "") {
        _authState.value = if (result.isSuccess) {
            val data = result.getOrNull()
            AuthState.Authenticated(data?.first ?: "", data?.second ?: "user", email)
        } else {
            AuthState.Error(result.exceptionOrNull()?.message ?: "Error de autenticación")
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: String, val role: String, val email: String) : AuthState() // Ahora incluye el rol y email
    data class Error(val message: String) : AuthState()
}