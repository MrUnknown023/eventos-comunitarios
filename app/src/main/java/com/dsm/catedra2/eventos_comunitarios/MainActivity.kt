package com.dsm.catedra2.eventos_comunitarios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dsm.catedra2.eventos_comunitarios.navigation.AppNavGraph
import com.dsm.catedra2.eventos_comunitarios.repository.FirebaseAuthRepository
import com.dsm.catedra2.eventos_comunitarios.repository.MockCommentRepository
import com.dsm.catedra2.eventos_comunitarios.repository.MockEventRepository
import com.dsm.catedra2.eventos_comunitarios.ui.permissions.RequestNotificationPermission
import com.dsm.catedra2.eventos_comunitarios.ui.theme.EventoscomunitariosTheme
import com.dsm.catedra2.eventos_comunitarios.view.LoginScreen
import com.dsm.catedra2.eventos_comunitarios.viewmodel.AuthState
import com.dsm.catedra2.eventos_comunitarios.viewmodel.AuthViewModel
import com.dsm.catedra2.eventos_comunitarios.viewmodel.CommentViewModel
import com.dsm.catedra2.eventos_comunitarios.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val eventViewModel   = EventViewModel(MockEventRepository(), applicationContext)
        val commentViewModel = CommentViewModel(MockCommentRepository())

        val authRepository = FirebaseAuthRepository()
        val authViewModel  = AuthViewModel(authRepository)

        authViewModel.checkSession()

        setContent {
            EventoscomunitariosTheme {

                RequestNotificationPermission()

                val authState by authViewModel.authState.collectAsState()

                when (val state = authState) {
                    is AuthState.Authenticated -> {
                        AppNavGraph(
                            eventViewModel   = eventViewModel,
                            commentViewModel = commentViewModel,
                            currentUserId    = state.userId,
                            currentUserEmail = state.email,
                            currentUserRole  = state.role,
                            onLogoutClick    = { authViewModel.logout() }
                        )
                    }
                    else -> {
                        LoginScreen(
                            viewModel      = authViewModel,
                            onLoginSuccess = { }
                        )
                    }
                }
            }
        }
    }
}