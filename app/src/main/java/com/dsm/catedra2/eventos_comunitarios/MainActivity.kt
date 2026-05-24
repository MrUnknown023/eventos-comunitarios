package com.dsm.catedra2.eventos_comunitarios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dsm.catedra2.eventos_comunitarios.repository.FirebaseAuthRepository
import com.dsm.catedra2.eventos_comunitarios.repository.MockEventRepository
import com.dsm.catedra2.eventos_comunitarios.ui.theme.EventoscomunitariosTheme
import com.dsm.catedra2.eventos_comunitarios.view.EventDetailScreen
import com.dsm.catedra2.eventos_comunitarios.view.LoginScreen
import com.dsm.catedra2.eventos_comunitarios.viewmodel.AuthState
import com.dsm.catedra2.eventos_comunitarios.viewmodel.AuthViewModel
import com.dsm.catedra2.eventos_comunitarios.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val eventRepository = MockEventRepository()
        val eventViewModel = EventViewModel(eventRepository)

        val authRepository = FirebaseAuthRepository()
        val authViewModel = AuthViewModel(authRepository)

        // Verifica si ya hay un token o sesión guardada en el dispositivo
        authViewModel.checkSession()

        setContent {
            EventoscomunitariosTheme {
                val authState by authViewModel.authState.collectAsState()

                when (val state = authState) {
                    is AuthState.Authenticated -> {
                        EventDetailScreen(
                            viewModel = eventViewModel,
                            currentUserId = state.userId,
                            currentUserRole = state.role, // Le pasamos el rol
                            onLogoutClick = { authViewModel.logout() }
                        )
                    }
                    else -> {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { }
                        )
                    }
                }
            }
        }
    }
}